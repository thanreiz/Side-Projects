package com.floapp.agriflo.domain.repository

import com.floapp.agriflo.domain.model.WeatherData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    // ── 7-Day Forecast ────────────────────────────────────────────────────────
    fun get7DayForecast(): Flow<List<WeatherData>>
    suspend fun getWeatherForDate(date: String): WeatherData?
    suspend fun getWeatherRange(from: String, to: String): List<WeatherData>
    suspend fun getLastFetchTimestamp(): Long?
    suspend fun refreshWeather(latitude: Double, longitude: Double): Result<Unit>

    // ── 30-Day Climatology (5-year historical averages) ───────────────────────
    /**
     * Returns a live Flow of cached 30-day climatology rows (forecastType = CLIMATOLOGY).
     * Emits immediately from Room, then the ViewModel triggers [refreshClimatology]
     * in the background when online. Classic cache-first / offline-first pattern.
     */
    fun getClimatology(): Flow<List<WeatherData>>

    /**
     * Fetch the upcoming 30 days' historical averages from the last 5 years via the
     * Open-Meteo Archive API, average them, store in Room, and return success/failure.
     * Safe to call offline — returns [Result.failure] without crashing.
     */
    suspend fun refreshClimatology(latitude: Double, longitude: Double): Result<Unit>

    /**
     * Immediately seeds both the 7-day forecast and 30-day climatology caches with
     * realistic Philippine seasonal mock data if those caches are currently empty.
     *
     * Call this on app start BEFORE the network refresh so the UI always has
     * something to show. Real API data will overwrite the mock rows automatically
     * via Room's REPLACE conflict strategy.
     */
    suspend fun seedMockDataIfEmpty()
}
