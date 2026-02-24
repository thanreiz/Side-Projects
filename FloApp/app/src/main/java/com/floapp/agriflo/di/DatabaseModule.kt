package com.floapp.agriflo.di

import android.content.Context
import com.floapp.agriflo.data.local.FloDatabase
import com.floapp.agriflo.data.local.dao.*
import com.floapp.agriflo.utils.KeystoreHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFloDatabase(
        @ApplicationContext context: Context,
        keystoreHelper: KeystoreHelper
    ): FloDatabase {
        val passphrase = keystoreHelper.getOrCreateDatabasePassphrase()
        return FloDatabase.build(context, passphrase).also {
            // Zero out passphrase bytes from memory after use
            passphrase.fill(0)
        }
    }

    @Provides
    fun provideCropDao(db: FloDatabase): CropDao = db.cropDao()

    @Provides
    fun provideCropLogDao(db: FloDatabase): CropLogDao = db.cropLogDao()

    @Provides
    fun provideWeatherCacheDao(db: FloDatabase): WeatherCacheDao = db.weatherCacheDao()

    @Provides
    fun provideFertilizerReceiptDao(db: FloDatabase): FertilizerReceiptDao = db.fertilizerReceiptDao()

    @Provides
    fun provideHarvestForecastDao(db: FloDatabase): HarvestForecastDao = db.harvestForecastDao()
}
