package com.floapp.agriflo.domain.repository

import com.floapp.agriflo.domain.model.HarvestForecast
import kotlinx.coroutines.flow.Flow

interface HarvestForecastRepository {
    fun getForecastsForCrop(cropId: String): Flow<List<HarvestForecast>>
    suspend fun getLatestForecastForCrop(cropId: String): HarvestForecast?
    suspend fun saveForecast(forecast: HarvestForecast)
    suspend fun deleteOldForecasts(cropId: String, keepId: String)
}
