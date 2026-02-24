package com.floapp.agriflo.domain.model

/**
 * Domain model for weather data as stored in the local cache.
 */
data class WeatherData(
    val date: String,                  // ISO date "YYYY-MM-DD"
    val tempMinC: Double,
    val tempMaxC: Double,
    val rainfallMm: Double,
    val humidityPct: Int,
    val windSpeedKph: Double,
    val uvIndex: Double,
    val advisory: WeatherAdvisory,
    val advisoryDetail: String
)

/**
 * Agronomic advisories produced by [WeatherInterpreter].
 * These are the ONLY values shown to farmers — raw weather values are hidden in the UI.
 */
enum class WeatherAdvisory(
    val displayNameEn: String,
    val displayNameTl: String,
    val actionSuggestion: String
) {
    OPTIMAL_CONDITIONS(
        "Optimal Growing Conditions",
        "Magandang Panahon para sa Pananim",
        "Continue regular farming activities"
    ),
    HIGH_RAIN_RISK(
        "High Rain Risk",
        "Mataas na Tsansa ng Ulan",
        "Delay fertilizer application. Check drainage"
    ),
    DELAY_FERTILIZATION(
        "Delay Fertilization",
        "Huwag Muna Mag-abono",
        "Postpone fertilizer application by 2–3 days"
    ),
    OPTIMAL_PLANTING_WINDOW(
        "Optimal Planting Window",
        "Tamang Panahon para Magtanim",
        "Ideal conditions for planting this week"
    ),
    DROUGHT_RISK(
        "Drought Risk — Irrigate",
        "Panganib ng Tagtuyot",
        "Check water source and irrigate if possible"
    ),
    PEST_RISK_HIGH(
        "High Pest Risk",
        "Mataas na Panganib ng Peste",
        "Inspect crops for pests and apply treatment early"
    ),
    STRONG_WIND_WARNING(
        "Strong Wind Warning",
        "Babala: Malakas na Hangin",
        "Secure young plants and delay pesticide spraying"
    ),
    FROST_RISK(
        "Cold Stress Risk",
        "Panganib ng Lamig",
        "Protect seedlings from cold stress"
    )
}
