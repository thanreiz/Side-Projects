package com.floapp.agriflo.data.repository

import com.floapp.agriflo.data.local.dao.HarvestForecastDao
import com.floapp.agriflo.data.local.mapper.toDomain
import com.floapp.agriflo.data.local.mapper.toEntity
import com.floapp.agriflo.domain.model.HarvestForecast
import com.floapp.agriflo.domain.repository.HarvestForecastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HarvestForecastRepositoryImpl @Inject constructor(
    private val harvestForecastDao: HarvestForecastDao
) : HarvestForecastRepository {

    override fun getForecastsForCrop(cropId: String): Flow<List<HarvestForecast>> =
        harvestForecastDao.getForecastsForCrop(cropId).map { it.map { e -> e.toDomain() } }

    override suspend fun getLatestForecastForCrop(cropId: String): HarvestForecast? =
        harvestForecastDao.getLatestForecastForCrop(cropId)?.toDomain()

    override suspend fun saveForecast(forecast: HarvestForecast) {
        harvestForecastDao.insertForecast(forecast.toEntity())
        // Keep only the latest forecast per crop
        harvestForecastDao.deleteOldForecasts(forecast.cropId, forecast.id)
    }

    override suspend fun deleteOldForecasts(cropId: String, keepId: String) =
        harvestForecastDao.deleteOldForecasts(cropId, keepId)
}
