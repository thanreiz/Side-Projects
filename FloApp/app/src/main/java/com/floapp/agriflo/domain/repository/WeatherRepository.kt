package com.floapp.agriflo.domain.repository

import com.floapp.agriflo.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun get7DayForecast(): Flow<List<WeatherData>>
    suspend fun getWeatherForDate(date: String): WeatherData?
    suspend fun getWeatherRange(from: String, to: String): List<WeatherData>
    suspend fun getLastFetchTimestamp(): Long?
    suspend fun refreshWeather(latitude: Double, longitude: Double): Result<Unit>
}
