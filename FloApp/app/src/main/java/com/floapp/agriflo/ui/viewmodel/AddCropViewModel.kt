package com.floapp.agriflo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.domain.model.Crop
import com.floapp.agriflo.domain.model.CropType
import com.floapp.agriflo.domain.repository.CropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddCropViewModel @Inject constructor(
    private val cropRepository: CropRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun createCrop(
        name: String,
        variety: String,
        cropType: CropType,
        landAreaHa: Double,
        plantingDateIso: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val plantingDate = try {
                LocalDate.parse(plantingDateIso)
            } catch (e: DateTimeParseException) {
                LocalDate.now()
            }
            val crop = Crop(
                id = UUID.randomUUID().toString(),
                name = name,
                variety = variety,
                cropType = cropType,
                plantingDate = plantingDate,
                landAreaHa = landAreaHa,
                stageDurations = emptyMap(), // Uses CropCycleEngine defaults
                notes = "",
                isActive = true,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            cropRepository.addCrop(crop)
            _isLoading.value = false
            onSuccess()
        }
    }
}
