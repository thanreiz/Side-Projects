package com.floapp.agriflo.domain.engine

import com.floapp.agriflo.domain.model.WeatherAdvisory
import com.floapp.agriflo.domain.model.WeatherData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interprets raw weather values into agronomic advisories.
 *
 * Raw numbers (temperature, rainfall mm) should NEVER be surfaced directly
 * in the farmer UI. This engine always returns a meaningful advisory string
 * that guides farming decisions.
 *
 * Pure Kotlin — zero Android framework dependencies.
 */
@Singleton
class WeatherInterpreter @Inject constructor() {

    /**
     * Computes the most critical [WeatherAdvisory] for a given [WeatherData] entry.
     * Advisory is selected by priority (most severe applicable risk first).
     */
    fun interpret(weather: WeatherData): WeatherAdvisory {
        return when {
            weather.windSpeedKph > 60.0 -> WeatherAdvisory.STRONG_WIND_WARNING
            weather.rainfallMm > 20.0 -> WeatherAdvisory.HIGH_RAIN_RISK
            weather.rainfallMm > 10.0 -> WeatherAdvisory.DELAY_FERTILIZATION
            weather.tempMaxC < 15.0 -> WeatherAdvisory.FROST_RISK
            weather.tempMaxC > 38.0 && weather.humidityPct > 80 -> WeatherAdvisory.PEST_RISK_HIGH
            weather.rainfallMm < 1.0 && weather.tempMaxC > 34.0 -> WeatherAdvisory.DROUGHT_RISK
            weather.rainfallMm in 2.0..8.0 && weather.tempMaxC in 24.0..32.0 -> WeatherAdvisory.OPTIMAL_PLANTING_WINDOW
            else -> WeatherAdvisory.OPTIMAL_CONDITIONS
        }
    }

    /**
     * Generates a human-readable English+Tagalog advisory detail for a single day.
     */
    fun generateAdvisoryDetail(advisory: WeatherAdvisory, weather: WeatherData): String {
        return when (advisory) {
            WeatherAdvisory.HIGH_RAIN_RISK ->
                "Expected rainfall: ${weather.rainfallMm.toInt()}mm. Delay fertilizer. Check drainage. " +
                "Inaasahang ulan: ${weather.rainfallMm.toInt()}mm. Huwag mag-abono. Suriin ang drainage."
            WeatherAdvisory.DELAY_FERTILIZATION ->
                "Light rain expected (${weather.rainfallMm.toInt()}mm). Wait 2–3 days before applying fertilizer. " +
                "Mahinang ulan inaasahan. Maghintay ng 2–3 araw bago mag-abono."
            WeatherAdvisory.DROUGHT_RISK ->
                "Very little rain expected. High temperature ${weather.tempMaxC.toInt()}°C. Check irrigation. " +
                "Kaunting ulan lamang. Mataas na init. Tingnan ang patubig."
            WeatherAdvisory.PEST_RISK_HIGH ->
                "Hot and humid conditions favour pest spread. Inspect your crops today. " +
                "Mainit at mahalumigmig — pabor sa pagkalat ng peste. Siyasatin ang pananim."
            WeatherAdvisory.OPTIMAL_PLANTING_WINDOW ->
                "Good rain and temperature for planting this week. " +
                "Magandang ulan at init para sa pagtatanim ngayong linggo."
            WeatherAdvisory.STRONG_WIND_WARNING ->
                "Strong winds (${weather.windSpeedKph.toInt()} km/h). Secure plants and avoid spraying. " +
                "Malakas na hangin. I-secure ang mga pananim. Huwag mag-spray."
            WeatherAdvisory.FROST_RISK ->
                "Cold temperatures (${weather.tempMinC.toInt()}°C min). Protect seedlings from cold stress. " +
                "Malamig (${weather.tempMinC.toInt()}°C). Protektahan ang mga punla."
            WeatherAdvisory.OPTIMAL_CONDITIONS ->
                "Good weather for farming activities. Continue regular schedule. " +
                "Magandang panahon para sa pagsasaka. Ituloy ang regular na gawain."
        }
    }

    /**
     * Computes a weather risk factor (0.0–1.0) for the [HarvestPredictionEngine].
     * Higher = more weather-related risk to yield.
     */
    fun computeWeatherRiskFactor(forecasts: List<WeatherData>): Double {
        if (forecasts.isEmpty()) return 0.3 // Default moderate risk when no data

        val riskScores = forecasts.map { weather ->
            when (interpret(weather)) {
                WeatherAdvisory.STRONG_WIND_WARNING -> 0.9
                WeatherAdvisory.HIGH_RAIN_RISK -> 0.7
                WeatherAdvisory.DROUGHT_RISK -> 0.7
                WeatherAdvisory.PEST_RISK_HIGH -> 0.6
                WeatherAdvisory.DELAY_FERTILIZATION -> 0.4
                WeatherAdvisory.FROST_RISK -> 0.5
                WeatherAdvisory.OPTIMAL_PLANTING_WINDOW -> 0.1
                WeatherAdvisory.OPTIMAL_CONDITIONS -> 0.1
            }
        }
        return riskScores.average().coerceIn(0.0, 1.0)
    }

    /**
     * Returns the most actionable single advisory from a 7-day forecast.
     */
    fun getMostCriticalAdvisory(weekForecasts: List<WeatherData>): WeatherAdvisory {
        return weekForecasts
            .map { interpret(it) }
            .maxByOrNull { advisoryPriority(it) }
            ?: WeatherAdvisory.OPTIMAL_CONDITIONS
    }

    private fun advisoryPriority(advisory: WeatherAdvisory): Int = when (advisory) {
        WeatherAdvisory.STRONG_WIND_WARNING -> 10
        WeatherAdvisory.HIGH_RAIN_RISK -> 9
        WeatherAdvisory.DROUGHT_RISK -> 8
        WeatherAdvisory.PEST_RISK_HIGH -> 7
        WeatherAdvisory.FROST_RISK -> 6
        WeatherAdvisory.DELAY_FERTILIZATION -> 5
        WeatherAdvisory.OPTIMAL_PLANTING_WINDOW -> 2
        WeatherAdvisory.OPTIMAL_CONDITIONS -> 1
    }
}
