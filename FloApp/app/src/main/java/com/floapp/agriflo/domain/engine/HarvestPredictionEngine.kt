package com.floapp.agriflo.domain.engine

import com.floapp.agriflo.domain.model.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline harvest prediction engine.
 *
 * Computes projected yield, revenue, cost, and risk score based on:
 * - Land area
 * - Crop type (with Philippine baseline yield data)
 * - Fertilizer usage logs (cost aggregated from receipts)
 * - Weather risk factor (from WeatherInterpreter)
 *
 * All computation is deterministic and runs entirely on-device.
 * No internet required.
 */
@Singleton
class HarvestPredictionEngine @Inject constructor(
    private val weatherInterpreter: WeatherInterpreter
) {

    /**
     * Computes a [HarvestForecast] for a given crop.
     *
     * @param crop The active crop domain model
     * @param fertilizerCostPhp Total fertilizer cost from receipt logs
     * @param recentWeatherData Recent 7-day weather data (may be empty if cached data expired)
     * @param farmGatePriceOverride Optional price override; if null → uses crop-type-based estimate
     */
    fun computeForecast(
        crop: Crop,
        fertilizerCostPhp: Double,
        recentWeatherData: List<WeatherData> = emptyList(),
        farmGatePriceOverride: Double? = null
    ): HarvestForecast {
        val baseline = getBaselineYield(crop.cropType)
        val farmGatePrice = farmGatePriceOverride ?: baseline.estimatedFarmGatePricePhpPerKg
        val weatherRisk = weatherInterpreter.computeWeatherRiskFactor(recentWeatherData)
        val fertilizerCompliance = computeFertilizerComplianceScore(fertilizerCostPhp, crop.landAreaHa, crop.cropType)

        // Yield = (baseline kg/ha × land area) × weather adjustment × fertilizer compliance bonus
        val weatherYieldMultiplier = 1.0 - (weatherRisk * 0.3) // max 30% reduction from bad weather
        val fertilizerBonus = 0.9 + (fertilizerCompliance * 0.2)  // 90–110% yield based on compliance
        val projectedYieldKg = baseline.baselineYieldKgPerHa * crop.landAreaHa *
                weatherYieldMultiplier * fertilizerBonus

        val projectedRevenuePhp = projectedYieldKg * farmGatePrice
        val otherInputCostPhp = computeOtherInputCost(crop.landAreaHa, crop.cropType)
        val projectedCostPhp = fertilizerCostPhp + otherInputCostPhp
        val netProfitPhp = projectedRevenuePhp - projectedCostPhp

        // Risk score combines weather risk and profitability risk
        val profitabilityRisk = if (projectedRevenuePhp > 0) {
            ((projectedCostPhp / projectedRevenuePhp) - 0.5).coerceIn(0.0, 1.0)
        } else 1.0
        val riskScore = ((weatherRisk * 0.6) + (profitabilityRisk * 0.4)).coerceIn(0.0, 1.0)
        val riskLabel = when {
            riskScore < 0.33 -> RiskLabel.LOW
            riskScore < 0.66 -> RiskLabel.MEDIUM
            else -> RiskLabel.HIGH
        }

        val assumptions = ForecastAssumptions(
            landAreaHa = crop.landAreaHa,
            cropType = crop.cropType,
            averageYieldKgPerHa = baseline.baselineYieldKgPerHa,
            farmGatePricePhpPerKg = farmGatePrice,
            fertilizerCostPhp = fertilizerCostPhp,
            otherInputCostPhp = otherInputCostPhp,
            weatherRiskFactor = weatherRisk,
            fertilizerComplianceScore = fertilizerCompliance
        )

        return HarvestForecast(
            id = UUID.randomUUID().toString(),
            cropId = crop.id,
            projectedYieldKg = projectedYieldKg,
            projectedRevenuePhp = projectedRevenuePhp,
            projectedCostPhp = projectedCostPhp,
            netProfitPhp = netProfitPhp,
            riskScore = riskScore,
            riskLabel = riskLabel,
            assumptions = assumptions,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Returns a compliance score (0.0–1.0) estimating how well the farmer
     * followed the fertilization schedule based on cost vs expected spend.
     */
    private fun computeFertilizerComplianceScore(
        actualFertilizerCostPhp: Double,
        landAreaHa: Double,
        cropType: CropType
    ): Double {
        val expectedCostPerHa = when (cropType) {
            CropType.RICE -> 6_000.0
            CropType.CORN -> 5_000.0
            CropType.VEGETABLE -> 8_000.0
            CropType.FRUIT -> 10_000.0
            CropType.SUGARCANE -> 7_000.0
            CropType.CASSAVA -> 4_000.0
        }
        val expectedTotal = expectedCostPerHa * landAreaHa
        return (actualFertilizerCostPhp / expectedTotal).coerceIn(0.0, 1.0)
    }

    /**
     * Estimates miscellaneous input costs (labor, seeds, pest control) per hectare.
     * Based on DA (Department of Agriculture) production cost data for the Philippines.
     */
    private fun computeOtherInputCost(landAreaHa: Double, cropType: CropType): Double {
        val laborAndMiscPerHa = when (cropType) {
            CropType.RICE -> 18_000.0  // PHP/ha — land prep, transplanting, harvest labor
            CropType.CORN -> 14_000.0
            CropType.VEGETABLE -> 25_000.0
            CropType.FRUIT -> 20_000.0
            CropType.SUGARCANE -> 22_000.0
            CropType.CASSAVA -> 12_000.0
        }
        return laborAndMiscPerHa * landAreaHa
    }

    private data class CropBaseline(
        val baselineYieldKgPerHa: Double,
        val estimatedFarmGatePricePhpPerKg: Double
    )

    /**
     * Philippine DA-calibrated baseline yield and farm gate price data.
     */
    private fun getBaselineYield(cropType: CropType): CropBaseline = when (cropType) {
        CropType.RICE -> CropBaseline(4_500.0, 19.0)       // ~4.5 MT/ha, PHP 19/kg
        CropType.CORN -> CropBaseline(4_000.0, 14.0)        // ~4.0 MT/ha, PHP 14/kg
        CropType.VEGETABLE -> CropBaseline(8_000.0, 30.0)   // Variable; avg ampalaya/sitaw
        CropType.FRUIT -> CropBaseline(10_000.0, 25.0)      // Mango, banana avg
        CropType.SUGARCANE -> CropBaseline(60_000.0, 2.5)   // ~60 MT/ha
        CropType.CASSAVA -> CropBaseline(15_000.0, 4.5)     // ~15 MT/ha
    }
}
