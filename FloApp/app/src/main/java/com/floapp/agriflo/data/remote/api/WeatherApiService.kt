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
        @Query("latitude")     latitude:    Double,
        @Query("longitude")    longitude:   Double,
        @Query("daily")        daily:       String = "temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max,uv_index_max",
        @Query("hourly")       hourly:      String = "relativehumidity_2m",
        @Query("forecast_days") forecastDays: Int   = 7,
        @Query("timezone")     timezone:    String = "Asia/Manila"
    ): WeatherForecastResponse
}

/**
 * Open-Meteo Archive API — used to fetch historical daily weather.
 * Base URL: https://archive-api.open-meteo.com/v1/
 * Docs: https://open-meteo.com/en/docs/historical-weather-api
 *
 * We call this FIVE times per 30-day window (one call per past year) and then
 * average the results to create a climatology / seasonal outlook.
 */
interface HistoricalWeatherApiService {

    @GET("archive")
    suspend fun getHistorical(
        @Query("latitude")   latitude:  Double,
        @Query("longitude")  longitude: Double,
        @Query("start_date") startDate: String,   // "YYYY-MM-DD"
        @Query("end_date")   endDate:   String,   // "YYYY-MM-DD"
        @Query("daily")      daily:     String = "temperature_2m_max,temperature_2m_min,precipitation_sum,windspeed_10m_max,uv_index_max",
        @Query("timezone")   timezone:  String = "Asia/Manila"
    ): WeatherForecastResponse            // Same response shape as the forecast endpoint
}

@JsonClass(generateAdapter = true)
data class WeatherForecastResponse(
    val daily:     DailyWeather?,
    val latitude:  Double,
    val longitude: Double
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    val time:                       List<String>,
    @Json(name = "temperature_2m_max") val tempMax:       List<Double>,
    @Json(name = "temperature_2m_min") val tempMin:       List<Double>,
    @Json(name = "precipitation_sum") val precipitation:  List<Double>,
    @Json(name = "windspeed_10m_max") val windSpeed:      List<Double>,
    @Json(name = "uv_index_max")      val uvIndex:        List<Double>
)
