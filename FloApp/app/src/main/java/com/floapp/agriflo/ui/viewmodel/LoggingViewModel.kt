package com.floapp.agriflo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.domain.model.*
import com.floapp.agriflo.domain.repository.CropLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LoggingViewModel @Inject constructor(
    private val cropLogRepository: CropLogRepository
) : ViewModel() {

    fun logActivity(cropId: String, logType: LogType) {
        viewModelScope.launch {
            val log = CropLog(
                id = UUID.randomUUID().toString(),
                cropId = cropId,
                logType = logType,
                timestamp = System.currentTimeMillis()
            )
            cropLogRepository.addLog(log)
        }
    }
}
