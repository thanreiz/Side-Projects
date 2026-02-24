package com.floapp.agriflo.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo weather API (free, no API key required for basic forecast).
 * Docs: https://open-meteo.com/en/docs
 */
interface WeatherApiService {

    @GET("forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max,uv_index_max",
        @Query("hourly") hourly: String = "relativehumidity_2m",
        @Query("forecast_days") forecastDays: Int = 7,
        @Query("timezone") timezone: String = "Asia/Manila"
    ): WeatherForecastResponse
}

@JsonClass(generateAdapter = true)
data class WeatherForecastResponse(
    val daily: DailyWeather?,
    val latitude: Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time: List<String>,                          // ISO dates ["2026-02-24", ...]
    @Json(name = "temperature_2m_max") val tempMax: List<Double>,
    @Json(name = "temperature_2m_min") val tempMin: List<Double>,
    @Json(name = "precipitation_sum") val precipitation: List<Double>,
    @Json(name = "windspeed_10m_max") val windSpeed: List<Double>,
    @Json(name = "uv_index_max") val uvIndex: List<Double>
)
