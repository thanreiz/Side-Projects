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

    @Query("SELECT * FROM weather_cache WHERE forecastType = 'DAILY_FORECAST' ORDER BY date ASC LIMIT 7")
    fun get7DayForecast(): Flow<List<WeatherCacheEntity>>

    @Query("SELECT * FROM weather_cache WHERE date = :date AND forecastType = 'DAILY_FORECAST' LIMIT 1")
    suspend fun getWeatherForDate(date: String): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE forecastType = 'SEASONAL' ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getLatestSeasonalOutlook(): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache WHERE date >= :from AND date <= :to ORDER BY date ASC")
    suspend fun getWeatherRange(from: String, to: String): List<WeatherCacheEntity>

    @Query("SELECT fetchedAt FROM weather_cache WHERE forecastType = 'DAILY_FORECAST' ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getLastFetchTimestamp(): Long?

    @Query("DELETE FROM weather_cache WHERE forecastType = 'DAILY_FORECAST' AND date < :beforeDate")
    suspend fun deleteExpiredForecasts(beforeDate: String)

    @Query("SELECT COUNT(*) FROM weather_cache WHERE forecastType = 'DAILY_FORECAST'")
    suspend fun getForecastCount(): Int
}
