package com.floapp.agriflo.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.floapp.agriflo.data.remote.mock.LandDataMockGenerator
import com.floapp.agriflo.domain.model.LandData
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * UI state for the Land Data tab.
 *
 * Sealed class so the UI can easily switch between states
 * using a single `when` expression.
 */
sealed interface LandDataUiState {
    /** Initial load in progress (spinner shown over empty content). */
    object Loading : LandDataUiState

    /** Data loaded successfully and ready to display. */
    data class Success(val data: LandData) : LandDataUiState

    /** Simulated download in progress after the user chooses a new region. */
    data class Downloading(
        val regionName: String,
        val progressFraction: Float
    ) : LandDataUiState

    /** An error occurred during loading. [message] is user-facing. */
    data class Error(val message: String) : LandDataUiState
}

/**
 * ViewModel for the Land Data tab.
 *
 * Responsibilities:
 * 1. Initialise from mock GPS data on first launch.
 * 2. Expose a list of available regions for the region picker.
 * 3. Simulate an asynchronous "download" when the user changes regions.
 * 4. Expose the detected GPS location name for display.
 *
 * Injected by Hilt — no constructor parameters needed from the UI layer.
 */
@HiltViewModel
class LandDataViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    // ── UI state ──────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<LandDataUiState>(LandDataUiState.Loading)
    val uiState: StateFlow<LandDataUiState> = _uiState.asStateFlow()

    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    /** Available regions exposed so the UI can show a picker. */
    val availableRegions: List<Pair<String, String>> = LandDataMockGenerator.getAvailableRegions()

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        loadFromGps()
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Called when the user grants location permission mid-session.
     * Reloads data using the GPS location.
     */
    fun onPermissionGranted() = loadFromGps()

    /**
     * Loads data for the given [regionKey].
     *
     * Emits [LandDataUiState.Downloading] with animated progress, then resolves
     * to [LandDataUiState.Success] — simulating a real network download.
     *
     * @param regionKey  The key from [LandDataMockGenerator.getAvailableRegions].
     * @param regionName The human-readable display name (for the progress banner).
     */
    fun loadRegion(regionKey: String, regionName: String) {
        viewModelScope.launch {
            // Simulate incremental download progress
            val totalSteps = 20
            for (step in 1..totalSteps) {
                _uiState.value = LandDataUiState.Downloading(
                    regionName       = regionName,
                    progressFraction = step / totalSteps.toFloat()
                )
                delay(60L) // ~1.2 s total simulated download time
            }
            val data = LandDataMockGenerator.getDataForRegion(regionKey)
            _uiState.value = LandDataUiState.Success(data)
            _locationName.value = regionName
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Attempts to get the device's GPS location.
     * Defaults to the Cabanatuan area mock (Nueva Ecija) if location is
     * unavailable or permission is not granted.
     */
    private fun loadFromGps() {
        viewModelScope.launch {
            _uiState.value = LandDataUiState.Loading
            try {
                val location = getBestLocation()
                if (location != null) {
                    resolveLocationName(location.latitude, location.longitude)
                } else {
                    // Default: Cabanatuan, Nueva Ecija — the app's target audience
                    _locationName.value = "Nueva Ecija – Cabanatuan Area (GPS default)"
                }
                // In a real app we would use lat/lon to pick the closest data set.
                // For now, mock data is always Nueva Ecija (Cabanatuan).
                _uiState.value = LandDataUiState.Success(LandDataMockGenerator.getDefaultGpsData())
            } catch (e: SecurityException) {
                _uiState.value = LandDataUiState.Error(
                    "Location permission is required. Showing default Nueva Ecija data."
                )
                // Still show something useful even without permission
                _locationName.value = "Nueva Ecija – Cabanatuan Area (default)"
                _uiState.value = LandDataUiState.Success(LandDataMockGenerator.getDefaultGpsData())
            } catch (e: Exception) {
                _uiState.value = LandDataUiState.Error(
                    "Could not fetch location: ${e.localizedMessage}. Showing default data."
                )
                _uiState.value = LandDataUiState.Success(LandDataMockGenerator.getDefaultGpsData())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getBestLocation(): android.location.Location? {
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
            ).joinToString(", ").ifBlank { "%.4f°N, %.4f°E".format(lat, lon) }
        } catch (_: Exception) {
            _locationName.value = "%.4f°N, %.4f°E".format(lat, lon)
        }
    }
}
