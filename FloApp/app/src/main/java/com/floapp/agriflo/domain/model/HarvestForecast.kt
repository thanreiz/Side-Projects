package com.floapp.agriflo.domain.model

/**
 * Output of [HarvestPredictionEngine].
 * All financial values are in Philippine Pesos (PHP).
 */
data class HarvestForecast(
    val id: String,
    val cropId: String,
    val projectedYieldKg: Double,
    val projectedRevenuePhp: Double,
    val projectedCostPhp: Double,
    val netProfitPhp: Double,
    val riskScore: Double,             // 0.0 (safe) to 1.0 (very risky)
    val riskLabel: RiskLabel,
    val assumptions: ForecastAssumptions,
    val generatedAt: Long
)

enum class RiskLabel(val displayNameEn: String) {
    LOW("Low Risk"),
    MEDIUM("Medium Risk"),
    HIGH("High Risk")
}

/**
 * Transparent audit trail of inputs used to compute the forecast.
 * This enables the farmer to understand why a particular forecast was generated.
 */
data class ForecastAssumptions(
    val landAreaHa: Double,
    val cropType: CropType,
    val averageYieldKgPerHa: Double,    // Baseline yield for crop type in PH
    val farmGatePricePhpPerKg: Double,  // Current or estimated farm gate price
    val fertilizerCostPhp: Double,       // Sum of fertilizer receipt logs
    val otherInputCostPhp: Double,       // Estimated labor + misc
    val weatherRiskFactor: Double,       // 0.0–1.0 from WeatherInterpreter
    val fertilizerComplianceScore: Double // 0.0–1.0 based on log completeness
)
