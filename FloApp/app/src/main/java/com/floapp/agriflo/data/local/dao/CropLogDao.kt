package com.floapp.agriflo.data.local.dao

import androidx.room.*
import com.floapp.agriflo.data.local.entity.CropLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CropLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<CropLogEntity>)

    @Update
    suspend fun updateLog(log: CropLogEntity)

    @Query("SELECT * FROM crop_logs WHERE cropId = :cropId AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getLogsForCrop(cropId: String): Flow<List<CropLogEntity>>

    @Query("SELECT * FROM crop_logs WHERE cropId = :cropId AND logType = :logType AND isDeleted = 0 ORDER BY timestamp DESC")
    fun getLogsByType(cropId: String, logType: String): Flow<List<CropLogEntity>>

    @Query("SELECT * FROM crop_logs WHERE synced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedLogs(): List<CropLogEntity>

    @Query("UPDATE crop_logs SET synced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markLogSynced(id: String, timestamp: Long = System.currentTimeMillis())

    /**
     * IMPORTANT: Logs are SOFT-DELETED only. The sync engine must NEVER call
     * a hard delete method. Hard deletes are only performed via explicit admin action.
     */
    @Query("UPDATE crop_logs SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteLog(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM crop_logs WHERE cropId = :cropId AND logType = 'FERTILIZER' AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun getFertilizerLogsForCrop(cropId: String): List<CropLogEntity>

    @Query("SELECT COUNT(*) FROM crop_logs WHERE cropId = :cropId AND isDeleted = 0")
    suspend fun getLogCountForCrop(cropId: String): Int
}
