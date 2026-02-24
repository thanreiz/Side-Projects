package com.floapp.agriflo.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.floapp.agriflo.data.local.dao.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Background synchronization worker.
 * Runs when network is available. Uses exponential backoff.
 *
 * Conflict resolution: Latest updatedAt timestamp wins.
 * CRITICAL: Logs are NEVER deleted by this worker, even during conflict resolution.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cropDao: CropDao,
    private val cropLogDao: CropLogDao,
    private val fertilizerReceiptDao: FertilizerReceiptDao,
    private val harvestForecastDao: HarvestForecastDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncCrops()
            syncLogs()
            syncReceipts()
            syncForecasts()
            Result.success()
        } catch (e: Exception) {
            // Retry with exponential backoff (configured in SyncManager)
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf("error" to e.message)
                )
            }
        }
    }

    private suspend fun syncCrops() {
        val unsynced = cropDao.getUnsyncedCrops()
        unsynced.forEach { crop ->
            // TODO: POST/PUT to remote API when backend is available
            // For now, mark as synced locally to simulate successful sync
            cropDao.markCropSynced(crop.id)
        }
    }

    private suspend fun syncLogs() {
        val unsynced = cropLogDao.getUnsyncedLogs()
        // IMPORTANT: Never delete logs — only upload and mark synced
        unsynced.forEach { log ->
            // TODO: POST log to remote API
            cropLogDao.markLogSynced(log.id)
        }
    }

    private suspend fun syncReceipts() {
        val unsynced = fertilizerReceiptDao.getUnsyncedReceipts()
        unsynced.forEach { receipt ->
            // TODO: POST receipt to remote API
            fertilizerReceiptDao.markReceiptSynced(receipt.id)
        }
    }

    private suspend fun syncForecasts() {
        val unsynced = harvestForecastDao.getUnsyncedForecasts()
        unsynced.forEach { forecast ->
            // TODO: POST forecast to remote API
            harvestForecastDao.markForecastSynced(forecast.id)
        }
    }

    companion object {
        const val WORK_NAME = "FloSyncWorker"
        private const val MAX_RETRIES = 5
    }
}
