package com.floapp.agriflo.data.local.dao

import androidx.room.*
import com.floapp.agriflo.data.local.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrop(crop: CropEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropEntity>)

    @Update
    suspend fun updateCrop(crop: CropEntity)

    @Query("SELECT * FROM crops WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveCrops(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :id")
    suspend fun getCropById(id: String): CropEntity?

    @Query("SELECT * FROM crops WHERE id = :id")
    fun getCropByIdFlow(id: String): Flow<CropEntity?>

    @Query("SELECT * FROM crops WHERE synced = 0 AND isActive = 1")
    suspend fun getUnsyncedCrops(): List<CropEntity>

    @Query("UPDATE crops SET synced = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markCropSynced(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE crops SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteCrop(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM crops WHERE isActive = 1")
    suspend fun getActiveCropCount(): Int
}
