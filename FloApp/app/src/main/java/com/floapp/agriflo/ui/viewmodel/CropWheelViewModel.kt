package com.floapp.agriflo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.domain.engine.CropCycleEngine
import com.floapp.agriflo.domain.model.Crop
import com.floapp.agriflo.domain.model.CropStage
import com.floapp.agriflo.domain.repository.CropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CropWheelUiState(
    val cropName: String = "",
    val crop: Crop? = null,
    val cropStage: CropStage? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CropWheelViewModel @Inject constructor(
    private val cropRepository: CropRepository,
    private val cropCycleEngine: CropCycleEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(CropWheelUiState())
    val uiState: StateFlow<CropWheelUiState> = _uiState.asStateFlow()

    fun loadCrop(cropId: String) {
        viewModelScope.launch {
            cropRepository.getCropById(cropId).collect { crop ->
                if (crop != null) {
                    val stage = cropCycleEngine.computeCurrentStage(
                        plantingDate = crop.plantingDate,
                        stageDurations = crop.stageDurations,
                        observationDate = LocalDate.now()
                    )
                    _uiState.value = CropWheelUiState(
                        cropName = crop.name,
                        crop = crop,
                        cropStage = stage,
                        isLoading = false
                    )
                }
            }
        }
    }
}
