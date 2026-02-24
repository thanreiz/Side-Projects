package com.floapp.agriflo.data.repository

import com.floapp.agriflo.data.local.dao.WeatherCacheDao
import com.floapp.agriflo.data.local.entity.WeatherCacheEntity
import com.floapp.agriflo.data.local.mapper.toDomain
import com.floapp.agriflo.data.remote.api.WeatherApiService
import com.floapp.agriflo.domain.engine.WeatherInterpreter
import com.floapp.agriflo.domain.model.WeatherData
import com.floapp.agriflo.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherCacheDao: WeatherCacheDao,
    private val weatherApiService: WeatherApiService,
    private val weatherInterpreter: WeatherInterpreter
) : WeatherRepository {

    override fun get7DayForecast(): Flow<List<WeatherData>> =
        weatherCacheDao.get7DayForecast().map { it.map { e -> e.toDomain() } }

    override suspend fun getWeatherForDate(date: String): WeatherData? =
        weatherCacheDao.getWeatherForDate(date)?.toDomain()

    override suspend fun getWeatherRange(from: String, to: String): List<WeatherData> =
        weatherCacheDao.getWeatherRange(from, to).map { it.toDomain() }

    override suspend fun getLastFetchTimestamp(): Long? =
        weatherCacheDao.getLastFetchTimestamp()

    override suspend fun refreshWeather(latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val response = weatherApiService.getForecast(latitude, longitude)
            val daily = response.daily ?: return Result.success(Unit)

            val entities = daily.time.mapIndexed { index, date ->
                val tempMax = daily.tempMax.getOrElse(index) { 30.0 }
                val tempMin = daily.tempMin.getOrElse(index) { 24.0 }
                val rainfall = daily.precipitation.getOrElse(index) { 0.0 }
                val wind = daily.windSpeed.getOrElse(index) { 0.0 }
                val uv = daily.uvIndex.getOrElse(index) { 5.0 }

                val weatherData = WeatherData(
                    date = date,
                    tempMinC = tempMin,
                    tempMaxC = tempMax,
                    rainfallMm = rainfall,
                    humidityPct = 75, // Open-Meteo daily doesn't include hourly avg; use default 75%
                    windSpeedKph = wind,
                    uvIndex = uv,
                    advisory = weatherInterpreter.interpret(
                        WeatherData(date, tempMin, tempMax, rainfall, 75, wind, uv,
                            com.floapp.agriflo.domain.model.WeatherAdvisory.OPTIMAL_CONDITIONS, "")
                    ),
                    advisoryDetail = ""
                )
                val advisory = weatherInterpreter.interpret(weatherData)
                val detail = weatherInterpreter.generateAdvisoryDetail(advisory, weatherData)

                WeatherCacheEntity(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    tempMinC = tempMin,
                    tempMaxC = tempMax,
                    rainfallMm = rainfall,
                    humidityPct = 75,
                    windSpeedKph = wind,
                    uvIndex = uv,
                    advisory = advisory.name,
                    advisoryDetail = detail,
                    forecastType = "DAILY_FORECAST"
                )
            }
            weatherCacheDao.insertOrReplaceAll(entities)
            // Cleanup stale forecasts older than yesterday
            val yesterday = LocalDate.now().minusDays(1).toString()
            weatherCacheDao.deleteExpiredForecasts(yesterday)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
