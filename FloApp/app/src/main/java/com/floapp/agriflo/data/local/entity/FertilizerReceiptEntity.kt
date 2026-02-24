package com.floapp.agriflo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores a photographed fertilizer receipt extracted via ML Kit OCR.
 * The [productName] is validated against the locally-cached DA-approved
 * fertilizer catalog before being accepted.
 */
@Entity(tableName = "fertilizer_receipts")
data class FertilizerReceiptEntity(
    @PrimaryKey
    val id: String,                          // UUID
    val productName: String,                 // OCR-extracted product name
    val purchaseDate: String,               // ISO date extracted from receipt
    val costPhp: Double,                    // Cost in Philippine Pesos
    val quantityKg: Double? = null,         // Optional quantity extracted from receipt
    val supplierName: String? = null,       // Optional vendor name
    val imageUri: String,                   // Local URI of the receipt photo
    val validated: Boolean = false,         // True if product found in DA catalog
    val pdfExportUri: String? = null,       // Local URI of generated PDF report
    val cropId: String? = null,            // Optional association to a crop cycle
    val rawOcrText: String = "",           // Raw OCR output for re-processing if needed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
