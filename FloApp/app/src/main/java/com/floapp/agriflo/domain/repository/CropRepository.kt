package com.floapp.agriflo.domain.repository

import com.floapp.agriflo.domain.model.Crop
import com.floapp.agriflo.domain.model.CropType
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for crop data access.
 * Implemented in the data layer — domain layer has no knowledge of Room or Retrofit.
 */
interface CropRepository {
    fun getAllActiveCrops(): Flow<List<Crop>>
    fun getCropById(id: String): Flow<Crop?>
    suspend fun addCrop(crop: Crop)
    suspend fun createCrop(crop: Crop) = addCrop(crop) // backwards-compat alias
    suspend fun updateCrop(crop: Crop)
    suspend fun softDeleteCrop(id: String)
    suspend fun getCropCount(): Int
}
