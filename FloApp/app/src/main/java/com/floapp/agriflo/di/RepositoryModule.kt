package com.floapp.agriflo.di

import com.floapp.agriflo.data.repository.*
import com.floapp.agriflo.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCropRepository(impl: CropRepositoryImpl): CropRepository

    @Binds
    @Singleton
    abstract fun bindCropLogRepository(impl: CropLogRepositoryImpl): CropLogRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindHarvestForecastRepository(impl: HarvestForecastRepositoryImpl): HarvestForecastRepository
}
