package com.floapp.agriflo.di

import com.floapp.agriflo.BuildConfig
import com.floapp.agriflo.data.remote.api.GeminiApiService
import com.floapp.agriflo.data.remote.api.HistoricalWeatherApiService
import com.floapp.agriflo.data.remote.api.WeatherApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.IS_RELEASE) {
                HttpLoggingInterceptor.Level.NONE
            } else {
                HttpLoggingInterceptor.Level.BODY
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideWeatherApiService(@Named("weather") retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    @Provides
    @Singleton
    @Named("weather_archive")
    fun provideWeatherArchiveRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://archive-api.open-meteo.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideHistoricalWeatherApiService(
        @Named("weather_archive") retrofit: Retrofit
    ): HistoricalWeatherApiService = retrofit.create(HistoricalWeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideGeminiApiService(@Named("gemini") retrofit: Retrofit): GeminiApiService =
        retrofit.create(GeminiApiService::class.java)
}
