package com.floapp.agriflo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.domain.engine.HarvestPredictionEngine
import com.floapp.agriflo.domain.model.HarvestForecast
import com.floapp.agriflo.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HarvestForecastViewModel @Inject constructor(
    private val harvestForecastRepository: HarvestForecastRepository,
    private val cropRepository: CropRepository,
    private val cropLogRepository: CropLogRepository,
    private val fertilizerReceiptDao: com.floapp.agriflo.data.local.dao.FertilizerReceiptDao,
    private val weatherRepository: WeatherRepository,
    private val harvestPredictionEngine: HarvestPredictionEngine
) : ViewModel() {

    private val _forecast = MutableStateFlow<HarvestForecast?>(null)
    val forecast: StateFlow<HarvestForecast?> = _forecast.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadForecast(cropId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _forecast.value = harvestForecastRepository.getLatestForecastForCrop(cropId)
            _isLoading.value = false
        }
    }

    fun regenerateForecast(cropId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val crop = cropRepository.getCropById(cropId).firstOrNull() ?: run {
                _isLoading.value = false; return@launch
            }
            val fertCost = fertilizerReceiptDao.getTotalFertilizerCostForCrop(cropId) ?: 0.0
            val weatherData = weatherRepository.getWeatherRange(
                from = java.time.LocalDate.now().minusDays(14).toString(),
                to = java.time.LocalDate.now().toString()
            )
            val forecast = harvestPredictionEngine.computeForecast(
                crop = crop,
                fertilizerCostPhp = fertCost,
                recentWeatherData = weatherData
            )
            harvestForecastRepository.saveForecast(forecast)
            _forecast.value = forecast
            _isLoading.value = false
        }
    }
}
