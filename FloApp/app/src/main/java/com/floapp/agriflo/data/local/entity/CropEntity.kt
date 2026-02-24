package com.floapp.agriflo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a crop cycle record in the local database.
 * Each crop has a planting date and stage durations (stored as JSON)
 * which the CropCycleEngine uses to compute the current biological stage.
 */
@Entity(tableName = "crops")
data class CropEntity(
    @PrimaryKey
    val id: String,                          // UUID
    val name: String,                        // e.g. "Palay (Rice)"
    val variety: String,                     // e.g. "NSIC Rc222"
    val landAreaHa: Double,                  // Land area in hectares
    val plantingDate: Long,                  // Unix timestamp (ms)
    val expectedHarvestDate: Long,           // Unix timestamp (ms) — computed on creation
    /**
     * JSON-encoded map of stage durations in days.
     * e.g. {"LAND_PREP":14,"PLANTING":7,"VEGETATIVE":40,"FERTILIZATION":14,"HARVEST":7}
     */
    val stageDurationsJson: String,
    val cropType: String,                    // "RICE", "CORN", "VEGETABLE", "FRUIT"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false,             // Sync flag for background sync engine
    val isActive: Boolean = true
)
