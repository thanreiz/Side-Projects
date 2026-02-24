package com.floapp.agriflo.data.repository

import com.floapp.agriflo.data.local.dao.CropDao
import com.floapp.agriflo.data.local.mapper.toDomain
import com.floapp.agriflo.data.local.mapper.toEntity
import com.floapp.agriflo.domain.model.Crop
import com.floapp.agriflo.domain.repository.CropRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropRepositoryImpl @Inject constructor(
    private val cropDao: CropDao
) : CropRepository {

    override fun getAllActiveCrops(): Flow<List<Crop>> =
        cropDao.getAllActiveCrops().map { entities -> entities.map { it.toDomain() } }

    override fun getCropById(id: String): Flow<Crop?> =
        cropDao.getCropByIdFlow(id).map { it?.toDomain() }

    override suspend fun addCrop(crop: Crop) =
        cropDao.insertCrop(crop.toEntity())

    override suspend fun updateCrop(crop: Crop) =
        cropDao.updateCrop(crop.toEntity())

    override suspend fun softDeleteCrop(id: String) =
        cropDao.softDeleteCrop(id)

    override suspend fun getCropCount(): Int =
        cropDao.getActiveCropCount()
}
