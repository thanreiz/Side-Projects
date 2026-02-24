package com.floapp.agriflo.data.local.dao

import androidx.room.*
import com.floapp.agriflo.data.local.entity.HarvestForecastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: HarvestForecastEntity)

    @Update
    suspend fun updateForecast(forecast: HarvestForecastEntity)

    @Query("SELECT * FROM harvest_forecasts WHERE cropId = :cropId ORDER BY generatedAt DESC")
    fun getForecastsForCrop(cropId: String): Flow<List<HarvestForecastEntity>>

    @Query("SELECT * FROM harvest_forecasts WHERE cropId = :cropId ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getLatestForecastForCrop(cropId: String): HarvestForecastEntity?

    @Query("SELECT * FROM harvest_forecasts WHERE id = :id")
    suspend fun getForecastById(id: String): HarvestForecastEntity?

    @Query("SELECT * FROM harvest_forecasts WHERE synced = 0")
    suspend fun getUnsyncedForecasts(): List<HarvestForecastEntity>

    @Query("UPDATE harvest_forecasts SET synced = 1 WHERE id = :id")
    suspend fun markForecastSynced(id: String)

    @Query("DELETE FROM harvest_forecasts WHERE cropId = :cropId AND id != :keepId")
    suspend fun deleteOldForecasts(cropId: String, keepId: String)
}
