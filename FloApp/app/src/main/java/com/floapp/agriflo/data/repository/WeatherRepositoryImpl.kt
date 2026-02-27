package com.floapp.agriflo.data.repository

import com.floapp.agriflo.data.local.dao.WeatherCacheDao
import com.floapp.agriflo.data.local.entity.WeatherCacheEntity
import com.floapp.agriflo.data.local.mapper.toDomain
import com.floapp.agriflo.data.remote.api.HistoricalWeatherApiService
import com.floapp.agriflo.data.remote.api.WeatherApiService
import com.floapp.agriflo.data.remote.mock.WeatherMockDataGenerator
import com.floapp.agriflo.domain.engine.WeatherInterpreter
import com.floapp.agriflo.domain.model.WeatherAdvisory
import com.floapp.agriflo.domain.model.WeatherData
import com.floapp.agriflo.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [WeatherRepository].
 *
 * ## Offline-First Strategy
 * [get7DayForecast] and [getClimatology] return Room Flows that emit immediately
 * from cache — the UI always has data within the first frame.
 *
 * On init the ViewModel calls [seedMockDataIfEmpty] which inserts realistic Philippine
 * seasonal mock data if both caches are empty. Then [refreshWeather] /
 * [refreshClimatology] are called concurrently — if the device is online the real
 * Open-Meteo data overwrites the mock via REPLACE, and the Flows re-emit.
 * If offline, the mock/cache remains and no crash occurs (all API calls are
 * wrapped in try/catch).
 *
 * ## Climatology Algorithm
 * Fetches the upcoming 30-day window from Open-Meteo Archive for each of the
 * past 5 years, accumulates values per day-index, and averages them.
 */
@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherCacheDao:    WeatherCacheDao,
    private val weatherApiService:  WeatherApiService,
    private val historicalApiService: HistoricalWeatherApiService,
    private val weatherInterpreter: WeatherInterpreter
) : WeatherRepository {

    // ── 7-Day Forecast ────────────────────────────────────────────────────────

    override fun get7DayForecast(): Flow<List<WeatherData>> =
        weatherCacheDao.get7DayForecast().map { it.map { e -> e.toDomain() } }

    override suspend fun getWeatherForDate(date: String): WeatherData? =
        weatherCacheDao.getWeatherForDate(date)?.toDomain()

    override suspend fun getWeatherRange(from: String, to: String): List<WeatherData> =
        weatherCacheDao.getWeatherRange(from, to).map { it.toDomain() }

    override suspend fun getLastFetchTimestamp(): Long? =
        weatherCacheDao.getLastFetchTimestamp()

    /**
     * Fetches the live 7-day forecast from Open-Meteo.
     * If offline or the call fails, returns Result.failure — the cached/mock data
     * remains visible to the user.
     */
    override suspend fun refreshWeather(latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val response = weatherApiService.getForecast(latitude, longitude)
            val daily    = response.daily ?: return Result.success(Unit)
            val entities = buildEntities(
                daily.time, daily.tempMax, daily.tempMin,
                daily.precipitation, daily.windSpeed, daily.uvIndex,
                "DAILY_FORECAST"
            )
            weatherCacheDao.insertOrReplaceAll(entities)
            weatherCacheDao.deleteExpiredForecasts(LocalDate.now().minusDays(1).toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)   // caller (ViewModel) surfaces error to UI if needed
        }
    }

    // ── 30-Day Climatology ────────────────────────────────────────────────────

    override fun getClimatology(): Flow<List<WeatherData>> =
        weatherCacheDao.getClimatology().map { it.map { e -> e.toDomain() } }

    /**
     * Fetches the upcoming 30-day window from the Open-Meteo Archive API for each
     * of the past 5 years, averages the values, and writes CLIMATOLOGY rows to Room.
     *
     * No isOnline() guard — if the network is down the try/catch returns
     * Result.failure cleanly and the existing cache (mock or last good fetch) stays.
     */
    override suspend fun refreshClimatology(latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val today       = LocalDate.now()
            val windowStart = today.plusDays(1)
            val windowEnd   = today.plusDays(30)

            val tempMaxBuckets = Array(30) { mutableListOf<Double>() }
            val tempMinBuckets = Array(30) { mutableListOf<Double>() }
            val rainBuckets    = Array(30) { mutableListOf<Double>() }
            val windBuckets    = Array(30) { mutableListOf<Double>() }
            val uvBuckets      = Array(30) { mutableListOf<Double>() }
            val dateLabels     = Array(30) { "" }

            for (yearsBack in 1..5) {
                val histStart = windowStart.minusYears(yearsBack.toLong())
                val histEnd   = windowEnd.minusYears(yearsBack.toLong())
                val response  = historicalApiService.getHistorical(
                    latitude  = latitude,
                    longitude = longitude,
                    startDate = histStart.toString(),
                    endDate   = histEnd.toString()
                )
                val daily = response.daily ?: continue
                daily.time.forEachIndexed { i, _ ->
                    if (i >= 30) return@forEachIndexed
                    if (yearsBack == 1) dateLabels[i] = windowStart.plusDays(i.toLong()).toString()
                    tempMaxBuckets[i].add(daily.tempMax.getOrElse(i) { 32.0 })
                    tempMinBuckets[i].add(daily.tempMin.getOrElse(i) { 24.0 })
                    rainBuckets[i].add(daily.precipitation.getOrElse(i) { 0.0 })
                    windBuckets[i].add(daily.windSpeed.getOrElse(i) { 10.0 })
                    uvBuckets[i].add(daily.uvIndex.getOrElse(i) { 6.0 })
                }
            }

            val entities = (0 until 30)
                .filter { dateLabels[it].isNotEmpty() }
                .map { i ->
                    val tempMax = tempMaxBuckets[i].averageOrDefault(32.0)
                    val tempMin = tempMinBuckets[i].averageOrDefault(24.0)
                    val rain    = rainBuckets[i].averageOrDefault(0.0)
                    val wind    = windBuckets[i].averageOrDefault(10.0)
                    val uv      = uvBuckets[i].averageOrDefault(6.0)
                    val date    = dateLabels[i]
                    val stub    = WeatherData(date, tempMin, tempMax, rain, 75, wind, uv,
                        WeatherAdvisory.OPTIMAL_CONDITIONS, "")
                    val advisory = weatherInterpreter.interpret(stub)
                    val detail   = weatherInterpreter.generateAdvisoryDetail(advisory, stub)
                    WeatherCacheEntity(
                        id            = UUID.randomUUID().toString(),
                        date          = date,
                        tempMinC      = tempMin,
                        tempMaxC      = tempMax,
                        rainfallMm    = rain,
                        humidityPct   = 75,
                        windSpeedKph  = wind,
                        uvIndex       = uv,
                        advisory      = advisory.name,
                        advisoryDetail = detail,
                        forecastType  = "CLIMATOLOGY"
                    )
                }

            if (entities.isNotEmpty()) {
                weatherCacheDao.deleteAllClimatology()
                weatherCacheDao.insertOrReplaceAll(entities)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Mock seeding ──────────────────────────────────────────────────────────

    /**
     * Seeds both caches with realistic mock data if they are currently empty.
     * This runs BEFORE the network calls so the UI has data immediately.
     * Real API data overwrites the mocks via REPLACE when it arrives.
     */
    override suspend fun seedMockDataIfEmpty() {
        // 7-day forecast
        if (weatherCacheDao.getForecastCount() == 0) {
            val mockForecast = WeatherMockDataGenerator.generate7DayForecast(weatherInterpreter)
            weatherCacheDao.insertOrReplaceAll(mockForecast)
        }
        // 30-day climatology
        if (weatherCacheDao.getClimatologyLastFetchTimestamp() == null) {
            val mockClimatology = WeatherMockDataGenerator.generateClimatology(
                days = 30,
                weatherInterpreter = weatherInterpreter
            )
            weatherCacheDao.insertOrReplaceAll(mockClimatology)
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun List<Double>.averageOrDefault(default: Double) =
        if (isEmpty()) default else average()

    private fun buildEntities(
        times:        List<String>,
        maxTemps:     List<Double>,
        minTemps:     List<Double>,
        precipations: List<Double>,
        winds:        List<Double>,
        uvs:          List<Double>,
        forecastType: String
    ): List<WeatherCacheEntity> = times.mapIndexed { i, date ->
        val tempMax  = maxTemps.getOrElse(i) { 30.0 }
        val tempMin  = minTemps.getOrElse(i) { 24.0 }
        val rain     = precipations.getOrElse(i) { 0.0 }
        val wind     = winds.getOrElse(i) { 0.0 }
        val uv       = uvs.getOrElse(i) { 5.0 }
        val stub     = WeatherData(date, tempMin, tempMax, rain, 75, wind, uv,
            WeatherAdvisory.OPTIMAL_CONDITIONS, "")
        val advisory = weatherInterpreter.interpret(stub)
        val detail   = weatherInterpreter.generateAdvisoryDetail(advisory, stub)
        WeatherCacheEntity(
            id            = UUID.randomUUID().toString(),
            date          = date,
            tempMinC      = tempMin,
            tempMaxC      = tempMax,
            rainfallMm    = rain,
            humidityPct   = 75,
            windSpeedKph  = wind,
            uvIndex       = uv,
            advisory      = advisory.name,
            advisoryDetail = detail,
            forecastType  = forecastType
        )
    }
}
