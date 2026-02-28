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
import kotlinx.coroutines.withTimeoutOrNull
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

    // ── Loading / error / location state ─────────────────────────────────────

    private val _isLoading      = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _locationError  = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private val _locationName   = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    // ── 7-Day Forecast (Room Flow — emits immediately from cache) ─────────────

    val forecasts: StateFlow<List<WeatherData>> = weatherRepository.get7DayForecast()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val criticalAdvisory: StateFlow<WeatherAdvisory?> = forecasts.map { list ->
        if (list.isEmpty()) null else weatherInterpreter.getMostCriticalAdvisory(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── 30-Day Climatology (Room Flow — emits immediately from cache) ─────────

    val climatology: StateFlow<List<WeatherData>> = weatherRepository.getClimatology()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            // Seed mock data immediately so both tabs are never blank.
            // Real API data will overwrite when the network call in refresh() completes.
            weatherRepository.seedMockDataIfEmpty()
        }
        refresh()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Refresh both the 7-day forecast AND the 30-day climatology simultaneously. */
    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _locationError.value = null
            try {
                val location = getBestLocation()
                val lat = location?.latitude  ?: 14.5995   // Fall back to Manila
                val lon = location?.longitude ?: 120.9842

                if (location != null) resolveLocationName(lat, lon)
                else _locationError.value = "Using default location (Manila)"

                // Launch both refreshes concurrently — one does NOT block the other
                launch { weatherRepository.refreshWeather(lat, lon) }
                launch { weatherRepository.refreshClimatology(lat, lon) }

            } catch (e: SecurityException) {
                _locationError.value = "Location permission is required"
            } catch (e: Exception) {
                _locationError.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onPermissionGranted() = refresh()

    // ── Location helpers ──────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(): Location? {
        // Request a fresh one-shot fix first (5-second timeout).
        // This bypasses the Play Services cache which can hold a stale location
        // (e.g. the emulator default of Mountain View, California) even after
        // the emulator's GPS coordinates are changed in Extended Controls.
        val fresh = withTimeoutOrNull(5_000L) {
            suspendCancellableCoroutine { cont ->
                val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
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
        if (fresh != null) return fresh

        // Fall back to lastLocation if a fresh fix timed out
        return suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveLocationName(lat: Double, lon: Double) {
        try {
            val geocoder = Geocoder(getApplication(), Locale("fil", "PH"))
            val addr = geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
            _locationName.value = listOfNotNull(
                addr?.subAdminArea ?: addr?.locality,
                addr?.adminArea
            ).joinToString(", ").ifBlank { "%.4f°N %.4f°E".format(lat, lon) }
        } catch (_: Exception) {
            _locationName.value = "%.4f°N, %.4f°E".format(lat, lon)
        }
    }
}
