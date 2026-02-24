package com.floapp.agriflo.data.repository

import com.floapp.agriflo.data.local.dao.CropLogDao
import com.floapp.agriflo.data.local.mapper.toDomain
import com.floapp.agriflo.data.local.mapper.toEntity
import com.floapp.agriflo.domain.model.CropLog
import com.floapp.agriflo.domain.model.LogType
import com.floapp.agriflo.domain.repository.CropLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropLogRepositoryImpl @Inject constructor(
    private val cropLogDao: CropLogDao
) : CropLogRepository {

    override fun getLogsForCrop(cropId: String): Flow<List<CropLog>> =
        cropLogDao.getLogsForCrop(cropId).map { it.map { e -> e.toDomain() } }

    override fun getLogsByType(cropId: String, logType: LogType): Flow<List<CropLog>> =
        cropLogDao.getLogsByType(cropId, logType.name).map { it.map { e -> e.toDomain() } }

    override suspend fun addLog(log: CropLog) =
        cropLogDao.insertLog(log.toEntity())

    override suspend fun softDeleteLog(id: String) =
        cropLogDao.softDeleteLog(id)

    override suspend fun getFertilizerLogsForCrop(cropId: String): List<CropLog> =
        cropLogDao.getFertilizerLogsForCrop(cropId).map { it.toDomain() }

    override suspend fun getLogCount(cropId: String): Int =
        cropLogDao.getLogCountForCrop(cropId)
}
