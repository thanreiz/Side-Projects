package com.floapp.agriflo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.floapp.agriflo.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 *
 * Language restoration on cold start no longer needs to happen here.
 * [LanguageRepositoryImpl] is a Hilt singleton; its constructor calls
 * [LanguagePreferenceManager.loadLanguage()] so the saved language is seeded
 * into the StateFlow before the first Compose frame is rendered.
 */
@HiltAndroidApp
class FloApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        // Schedule background sync and weather refresh
        syncManager.scheduleAll()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
