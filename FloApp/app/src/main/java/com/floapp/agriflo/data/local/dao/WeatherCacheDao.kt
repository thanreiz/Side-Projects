package com.floapp.agriflo.data.local.dao

import androidx.room.*
import com.floapp.agriflo.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(weather: WeatherCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(forecasts: List<WeatherCacheEntity>)

    // ── 7-Day Forecast ────────────────────────────────────────────────────────

    @Query("SELECT * FROM weather_cache WHERE forecastType = 'DAILY_FORECAST' ORDER BY date ASC LIMIT 7")
    fun get7DayForecast(): Flow<List<WeatherCacheEntity>>

    @Query("SELECT * FROM weather_cache WHERE date = :date AND forecastType = 'DAILY_FORECAST' LIMIT 1")
    suspend fun getWeatherForDate(date: String): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE date >= :from AND date <= :to ORDER BY date ASC")
    suspend fun getWeatherRange(from: String, to: String): List<WeatherCacheEntity>

    @Query("SELECT fetchedAt FROM weather_cache WHERE forecastType = 'DAILY_FORECAST' ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getLastFetchTimestamp(): Long?

    @Query("DELETE FROM weather_cache WHERE forecastType = 'DAILY_FORECAST' AND date < :beforeDate")
    suspend fun deleteExpiredForecasts(beforeDate: String)

    @Query("SELECT COUNT(*) FROM weather_cache WHERE forecastType = 'DAILY_FORECAST'")
    suspend fun getForecastCount(): Int

    // ── 30-Day Climatology (5-year historical averages) ───────────────────────

    /**
     * Returns cached climatology rows as a live Flow.
     * These are the averaged historical rows stored with forecastType = 'CLIMATOLOGY'.
     * UI collects this immediately on screen load (cache-first / offline-first).
     */
    @Query("SELECT * FROM weather_cache WHERE forecastType = 'CLIMATOLOGY' ORDER BY date ASC LIMIT 30")
    fun getClimatology(): Flow<List<WeatherCacheEntity>>

    /** Wipe old climatology before inserting a fresh batch. */
    @Query("DELETE FROM weather_cache WHERE forecastType = 'CLIMATOLOGY'")
    suspend fun deleteAllClimatology()

    @Query("SELECT fetchedAt FROM weather_cache WHERE forecastType = 'CLIMATOLOGY' ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getClimatologyLastFetchTimestamp(): Long?
}
