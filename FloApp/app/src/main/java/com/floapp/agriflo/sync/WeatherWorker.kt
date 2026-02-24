package com.floapp.agriflo.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.floapp.agriflo.data.repository.WeatherRepositoryImpl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that refreshes the local 7-day weather cache.
 * Runs periodically when network is available (scheduled by SyncManager).
 *
 * Uses the device's last known location (stored in preferences) for the API call.
 * Falls back to Manila coordinates if no location is stored.
 */
@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepositoryImpl
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("flo_location_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat(PREF_LATITUDE, DEFAULT_LAT_MANILA).toDouble()
        val lon = prefs.getFloat(PREF_LONGITUDE, DEFAULT_LON_MANILA).toDouble()

        return weatherRepository.refreshWeather(lat, lon).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    companion object {
        const val WORK_NAME = "FloWeatherWorker"
        private const val PREF_LATITUDE = "user_latitude"
        private const val PREF_LONGITUDE = "user_longitude"
        // Default: Manila, Philippines
        private const val DEFAULT_LAT_MANILA = 14.5995f
        private const val DEFAULT_LON_MANILA = 120.9842f
    }
}
