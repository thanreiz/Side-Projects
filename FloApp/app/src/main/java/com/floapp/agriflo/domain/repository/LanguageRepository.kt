package com.floapp.agriflo.domain.repository

import com.floapp.agriflo.ui.theme.AppLanguage
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain-layer contract for language preference.
 *
 * This lives in the domain layer so ViewModels depend only on the abstraction,
 * not on the concrete data-layer implementation.
 */
interface LanguageRepository {

    /** Current language — never null, defaults to Tagalog on first install. */
    val language: StateFlow<AppLanguage>

    /**
     * Change the active language.
     * - Updates [language] StateFlow immediately (triggers recomposition in all observers).
     * - Persists the choice to SharedPreferences.
     */
    fun setLanguage(language: AppLanguage)
}
