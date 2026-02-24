package com.floapp.agriflo.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WorkManager job scheduling for all background tasks:
 * - Periodic data sync (every 6 hours, network-constrained)
 * - Periodic weather refresh (every 3 hours, network-constrained)
 *
 * Exponential backoff policy:
 * - Initial delay: 30 seconds
 * - Multiplier: 2x per retry (30s, 60s, 120s, 240s, 480s)
 * - Max backoff: 5 minutes
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules all periodic background tasks. Safe to call multiple times —
     * WorkManager deduplicates by unique work name with KEEP policy.
     */
    fun scheduleAll() {
        scheduleSyncWorker()
        scheduleWeatherWorker()
    }

    private fun scheduleSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun scheduleWeatherWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val weatherRequest = PeriodicWorkRequestBuilder<WeatherWorker>(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WeatherWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            weatherRequest
        )
    }

    /**
     * Triggers an immediate one-shot sync (e.g., when app comes to foreground with network).
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val immediateSync = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        workManager.enqueue(immediateSync)
    }

    fun cancelAll() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(WeatherWorker.WORK_NAME)
    }
}
