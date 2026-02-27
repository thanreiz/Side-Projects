package com.floapp.agriflo.di

import com.floapp.agriflo.data.repository.LanguageRepositoryImpl
import com.floapp.agriflo.domain.repository.LanguageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Tells Hilt: "whenever someone asks for [LanguageRepository], give them the
 * singleton [LanguageRepositoryImpl]."
 *
 * Using @Binds (vs @Provides) is preferred here because Hilt can do it at
 * compile time without generating a wrapper method body.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageModule {

    @Binds
    @Singleton
    abstract fun bindLanguageRepository(
        impl: LanguageRepositoryImpl
    ): LanguageRepository
}
