package com.floapp.agriflo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.floapp.agriflo.domain.repository.LanguageRepository
import com.floapp.agriflo.ui.theme.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Delegates entirely to [LanguageRepository]:
 * - Reading the current language comes from [LanguageRepository.language] StateFlow.
 * - Writing calls [LanguageRepository.setLanguage], which updates the singleton
 *   MutableStateFlow — this single write is then observed by EVERY tab via [AppViewModel].
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageRepository: LanguageRepository
) : ViewModel() {

    /** Current language — same StateFlow as the rest of the app. */
    val language: StateFlow<AppLanguage> = languageRepository.language

    /** Select a new language; all tabs update on the next frame. */
    fun setLanguage(language: AppLanguage) {
        languageRepository.setLanguage(language)
    }
}
