package com.floapp.agriflo.data.local.dao

import androidx.room.*
import com.floapp.agriflo.data.local.entity.FertilizerReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: FertilizerReceiptEntity)

    @Update
    suspend fun updateReceipt(receipt: FertilizerReceiptEntity)

    @Query("SELECT * FROM fertilizer_receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<FertilizerReceiptEntity>>

    @Query("SELECT * FROM fertilizer_receipts WHERE id = :id")
    suspend fun getReceiptById(id: String): FertilizerReceiptEntity?

    @Query("SELECT * FROM fertilizer_receipts WHERE cropId = :cropId ORDER BY purchaseDate DESC")
    suspend fun getReceiptsForCrop(cropId: String): List<FertilizerReceiptEntity>

    @Query("SELECT * FROM fertilizer_receipts WHERE synced = 0")
    suspend fun getUnsyncedReceipts(): List<FertilizerReceiptEntity>

    @Query("UPDATE fertilizer_receipts SET synced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markReceiptSynced(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE fertilizer_receipts SET pdfExportUri = :uri, updatedAt = :timestamp WHERE id = :id")
    suspend fun updatePdfUri(id: String, uri: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE fertilizer_receipts SET validated = :validated, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateValidationStatus(id: String, validated: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT SUM(costPhp) FROM fertilizer_receipts WHERE cropId = :cropId")
    suspend fun getTotalFertilizerCostForCrop(cropId: String): Double?
}
