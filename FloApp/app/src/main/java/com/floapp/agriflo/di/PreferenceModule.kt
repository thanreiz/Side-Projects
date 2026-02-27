package com.floapp.agriflo.di

import com.floapp.agriflo.data.preference.LanguagePreferenceManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module that makes [LanguagePreferenceManager] injectable.
 *
 * No explicit @Provides needed — Hilt auto-discovers the @Inject constructor
 * on [LanguagePreferenceManager] since it's annotated with @Singleton.
 * This module exists as a marker so the component is aware of the binding.
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule
