package com.floapp.agriflo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single activity log entry from the farmer.
 * Logs are created via icon taps (never text input) and must never be deleted,
 * even during conflict resolution on sync.
 */
@Entity(
    tableName = "crop_logs",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["id"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cropId"), Index("timestamp")]
)
data class CropLogEntity(
    @PrimaryKey
    val id: String,                          // UUID
    val cropId: String,                      // Foreign key → CropEntity
    val logType: String,                     // LogType enum name: FERTILIZER, PEST, RAINFALL, IRRIGATION, HARVEST, OTHER
    val note: String = "",                   // Optional short note
    val imageUri: String? = null,            // Nullable local file URI for photo attachment
    val quantity: Double? = null,            // Optional quantity (kg, liters, etc.)
    val unit: String? = null,               // Unit string (e.g. "kg", "L", "bags")
    val timestamp: Long,                     // When the activity actually happened
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    /**
     * CRITICAL: This flag is never set to true. Logs may be soft-deleted
     * only via explicit user action, never by the sync engine.
     */
    val isDeleted: Boolean = false
)
