package com.floapp.agriflo.data.remote.mock

import com.floapp.agriflo.data.local.entity.WeatherCacheEntity
import com.floapp.agriflo.domain.engine.WeatherInterpreter
import com.floapp.agriflo.domain.model.WeatherAdvisory
import com.floapp.agriflo.domain.model.WeatherData
import java.time.LocalDate
import java.util.UUID
import kotlin.math.sin

/**
 * Generates realistic Philippine-climate mock weather data for the upcoming N days.
 *
 * Used to seed the local Room cache IMMEDIATELY (before any network call completes),
 * so the UI is never empty. When the real Open-Meteo API responds, it will overwrite
 * these rows and the Room Flow will re-emit updated data automatically.
 *
 * Climate model used:
 *  - Philippines has two main seasons:
 *    • Dry  (Nov–May): 24–36°C, 0–3 mm/day rainfall, UV 9–12
 *    • Wet  (Jun–Oct): 23–31°C, 5–20 mm/day rainfall, UV 6–9
 *  - Temperature and rainfall modulated by a sine wave to avoid flat data.
 */
object WeatherMockDataGenerator {

    /**
     * Produce [days] mock WeatherCacheEntity rows starting from tomorrow,
     * interpreted through [weatherInterpreter] so advisories are consistent
     * with the real advisory engine.
     */
    fun generateClimatology(
        days:               Int = 30,
        weatherInterpreter: WeatherInterpreter
    ): List<WeatherCacheEntity> {
        val today = LocalDate.now()
        return (1..days).map { offset ->
            val date      = today.plusDays(offset.toLong())
            val isWet     = date.monthValue in 6..10          // Jun–Oct
            val phase     = offset * 0.35                      // oscillation phase
            val rain      = if (isWet) (8.0 + sin(phase) * 7.0).coerceAtLeast(0.0)
                            else       (1.5 + sin(phase) * 1.5).coerceAtLeast(0.0)
            val tempMin   = if (isWet) 23.5 + sin(phase * 0.6) * 1.5
                            else       25.0 + sin(phase * 0.6) * 1.5
            val tempMax   = if (isWet) 30.5 + sin(phase * 0.4) * 1.5
                            else       33.5 + sin(phase * 0.4) * 2.5
            val wind      = (12.0 + sin(phase * 0.8) * 6.0).coerceAtLeast(5.0)
            val uv        = if (isWet) 7.0 + sin(phase * 0.5) * 2.0
                            else      10.0 + sin(phase * 0.5) * 2.0
            val humidity  = if (isWet) 82 else 68

            val stub = WeatherData(
                date          = date.toString(),
                tempMinC      = tempMin,
                tempMaxC      = tempMax,
                rainfallMm    = rain,
                humidityPct   = humidity,
                windSpeedKph  = wind,
                uvIndex       = uv,
                advisory      = WeatherAdvisory.OPTIMAL_CONDITIONS,
                advisoryDetail = ""
            )
            val advisory = weatherInterpreter.interpret(stub)
            val detail   = weatherInterpreter.generateAdvisoryDetail(advisory, stub)

            WeatherCacheEntity(
                id             = UUID.randomUUID().toString(),
                date           = date.toString(),
                tempMinC       = String.format("%.1f", tempMin).toDouble(),
                tempMaxC       = String.format("%.1f", tempMax).toDouble(),
                rainfallMm     = String.format("%.1f", rain).toDouble(),
                humidityPct    = humidity,
                windSpeedKph   = String.format("%.1f", wind).toDouble(),
                uvIndex        = String.format("%.1f", uv).toDouble(),
                advisory       = advisory.name,
                advisoryDetail = detail,
                forecastType   = "CLIMATOLOGY"
            )
        }
    }

    /**
     * Produce 7 mock WeatherCacheEntity rows for the 7-day forecast,
     * seeded when the real API hasn't responded yet.
     */
    fun generate7DayForecast(
        weatherInterpreter: WeatherInterpreter
    ): List<WeatherCacheEntity> {
        val today  = LocalDate.now()
        return (0 until 7).map { offset ->
            val date     = today.plusDays(offset.toLong())
            val isWet    = date.monthValue in 6..10
            val phase    = offset * 0.6
            val rain     = if (isWet) (10.0 + sin(phase) * 8.0).coerceAtLeast(0.0)
                           else       (1.0  + sin(phase) * 1.0).coerceAtLeast(0.0)
            val tempMin  = if (isWet) 24.0 + sin(phase * 0.5) * 1.0
                           else       25.5 + sin(phase * 0.5) * 1.0
            val tempMax  = if (isWet) 30.0 + sin(phase * 0.4) * 2.0
                           else       34.0 + sin(phase * 0.4) * 2.0
            val wind     = (10.0 + sin(phase) * 5.0).coerceAtLeast(4.0)
            val uv       = if (isWet) 7.0 + sin(phase) * 2.0
                           else       10.0 + sin(phase) * 2.0

            val stub = WeatherData(
                date           = date.toString(),
                tempMinC       = tempMin,
                tempMaxC       = tempMax,
                rainfallMm     = rain,
                humidityPct    = if (isWet) 80 else 65,
                windSpeedKph   = wind,
                uvIndex        = uv,
                advisory       = WeatherAdvisory.OPTIMAL_CONDITIONS,
                advisoryDetail = ""
            )
            val advisory = weatherInterpreter.interpret(stub)
            val detail   = weatherInterpreter.generateAdvisoryDetail(advisory, stub)

            WeatherCacheEntity(
                id             = UUID.randomUUID().toString(),
                date           = date.toString(),
                tempMinC       = String.format("%.1f", tempMin).toDouble(),
                tempMaxC       = String.format("%.1f", tempMax).toDouble(),
                rainfallMm     = String.format("%.1f", rain).toDouble(),
                humidityPct    = if (isWet) 80 else 65,
                windSpeedKph   = String.format("%.1f", wind).toDouble(),
                uvIndex        = String.format("%.1f", uv).toDouble(),
                advisory       = advisory.name,
                advisoryDetail = detail,
                forecastType   = "DAILY_FORECAST"
            )
        }
    }
}
