package com.floapp.agriflo.data.repository

import com.floapp.agriflo.data.preference.LanguagePreferenceManager
import com.floapp.agriflo.domain.repository.LanguageRepository
import com.floapp.agriflo.ui.theme.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [LanguageRepository].
 *
 * Marked @Singleton so Hilt creates exactly ONE instance for the entire app lifetime.
 * This is the key guarantee that all tabs always observe the same state.
 *
 * Initialisation order (happens once, before the first Compose frame):
 *   1. Hilt creates this singleton.
 *   2. Constructor calls [LanguagePreferenceManager.loadLanguage()] to restore the
 *      persisted choice — so the app always starts in the user's last-used language.
 *   3. [_language] is seeded with that value.
 *   4. Every ViewModel that depends on [LanguageRepository] receives the same instance.
 */
@Singleton
class LanguageRepositoryImpl @Inject constructor(
    private val languagePrefs: LanguagePreferenceManager
) : LanguageRepository {

    // ── The single source of truth ────────────────────────────────────────────
    //
    // MutableStateFlow is safe to write from any thread, and it conflates
    // rapid updates (only the latest value is delivered to collectors) —
    // exactly what we need for a simple language toggle.

    private val _language = MutableStateFlow(languagePrefs.loadLanguage())

    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // ── Write path ────────────────────────────────────────────────────────────
    //
    // Updating _language.value triggers every StateFlow collector to recompose
    // immediately on the next frame — including all tabs inside FloNavGraph.

    override fun setLanguage(language: AppLanguage) {
        _language.value = language          // instant UI update across ALL tabs
        languagePrefs.saveLanguage(language) // survive app restart
    }
}
