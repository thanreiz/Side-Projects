package com.floapp.agriflo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persists a generated harvest forecast for a specific crop cycle.
 * Forecasts are computed entirely on-device by HarvestPredictionEngine.
 */
@Entity(
    tableName = "harvest_forecasts",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["id"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cropId")]
)
data class HarvestForecastEntity(
    @PrimaryKey
    val id: String,                          // UUID
    val cropId: String,                      // FK → CropEntity
    val projectedYieldKg: Double,            // Estimated yield in kilograms
    val projectedRevenuePhp: Double,         // Projected gross revenue in PHP
    val projectedCostPhp: Double,            // Total estimated input cost in PHP
    val netProfitPhp: Double,               // Revenue - Cost
    /**
     * Risk score 0.0–1.0 (higher = riskier).
     * Computed from weather variance, fertilizer compliance, and land area.
     */
    val riskScore: Double,
    val riskLabel: String,                  // "LOW", "MEDIUM", "HIGH"
    val assumptions: String,                // JSON-encoded assumptions used in calculation
    val generatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
