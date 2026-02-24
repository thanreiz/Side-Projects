package com.floapp.agriflo.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.domain.engine.WeatherInterpreter
import com.floapp.agriflo.domain.model.WeatherAdvisory
import com.floapp.agriflo.domain.model.WeatherData
import com.floapp.agriflo.domain.repository.WeatherRepository
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class WeatherViewModel @Inject constructor(
    app: Application,
    private val weatherRepository: WeatherRepository,
    private val weatherInterpreter: WeatherInterpreter
) : AndroidViewModel(app) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    val forecasts: StateFlow<List<WeatherData>> = weatherRepository.get7DayForecast()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalAdvisory: StateFlow<WeatherAdvisory?> = forecasts.map { list ->
        if (list.isEmpty()) null else weatherInterpreter.getMostCriticalAdvisory(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _locationError.value = null
            try {
                val location = getBestLocation()
                if (location != null) {
                    resolveLocationName(location.latitude, location.longitude)
                    weatherRepository.refreshWeather(location.latitude, location.longitude)
                } else {
                    _locationError.value = "Hinihiling ang lokasyon… / Requesting location…"
                    // Fall back to Manila so the screen isn't empty
                    weatherRepository.refreshWeather(14.5995, 120.9842)
                }
            } catch (e: SecurityException) {
                _locationError.value = "Location permission required"
            } catch (e: Exception) {
                _locationError.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Called by the UI once location permission is granted
    fun onPermissionGranted() = refresh()

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(): Location? {
        // Try last known first (fast)
        val last = suspendCancellableCoroutine<Location?> { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
        if (last != null) return last

        // Request a fresh single update
        return suspendCancellableCoroutine { cont ->
            val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 0)
                .setMaxUpdates(1)
                .build()
            val cb = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedClient.removeLocationUpdates(this)
                    cont.resume(result.lastLocation)
                }
            }
            fusedClient.requestLocationUpdates(req, cb, Looper.getMainLooper())
            cont.invokeOnCancellation { fusedClient.removeLocationUpdates(cb) }
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveLocationName(lat: Double, lon: Double) {
        val app = getApplication<Application>()
        try {
            val geocoder = Geocoder(app, Locale("fil", "PH"))
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val addr = addresses?.firstOrNull()
            _locationName.value = listOfNotNull(
                addr?.subAdminArea ?: addr?.locality,
                addr?.adminArea
            ).joinToString(", ").ifBlank { "%.4f°N %.4f°E".format(lat, lon) }
        } catch (_: Exception) {
            _locationName.value = "%.4f°N, %.4f°E".format(lat, lon)
        }
    }
}
