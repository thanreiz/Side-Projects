package com.floapp.agriflo.domain.repository

import com.floapp.agriflo.domain.model.CropLog
import com.floapp.agriflo.domain.model.LogType
import kotlinx.coroutines.flow.Flow

interface CropLogRepository {
    fun getLogsForCrop(cropId: String): Flow<List<CropLog>>
    fun getLogsByType(cropId: String, logType: LogType): Flow<List<CropLog>>
    suspend fun addLog(log: CropLog)
    suspend fun softDeleteLog(id: String)
    suspend fun getFertilizerLogsForCrop(cropId: String): List<CropLog>
    suspend fun getLogCount(cropId: String): Int
}
