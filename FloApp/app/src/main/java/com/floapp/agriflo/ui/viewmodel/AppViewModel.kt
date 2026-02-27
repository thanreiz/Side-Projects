package com.floapp.agriflo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.floapp.agriflo.domain.repository.LanguageRepository
import com.floapp.agriflo.ui.theme.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Root-level ViewModel scoped to the NavGraph host Activity.
 *
 * Because [hiltViewModel()] inside [FloNavGraph] is called at the Activity level,
 * this ViewModel survives tab switches and back-stack changes — its lifetime is
 * the same as MainActivity. This is the bridge between the Hilt singleton
 * [LanguageRepository] and the Compose world.
 *
 * Why a ViewModel here instead of collecting StateFlow directly in a composable?
 * - ViewModels survive configuration changes (screen rotation).
 * - [hiltViewModel()] at the NavGraph root gives us one shared instance for
 *   the entire nav graph, not a per-destination instance.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    languageRepository: LanguageRepository
) : ViewModel() {

    /**
     * Exposes the language StateFlow directly.
     * [FloNavGraph] collects this with [collectAsStateWithLifecycle] so the
     * [CompositionLocalProvider] re-provides a new value whenever the language changes,
     * triggering recomposition in every active tab simultaneously.
     */
    val language: StateFlow<AppLanguage> = languageRepository.language
}
