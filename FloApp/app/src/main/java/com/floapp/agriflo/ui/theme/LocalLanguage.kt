package com.floapp.agriflo.ui.theme

import androidx.compose.runtime.compositionLocalOf

/** All languages the app supports. */
enum class AppLanguage(
    /** Native display name — always shown as-is, never translated. */
    val nativeName: String,
    /** BCP-47 tag used for Android resource resolution (informational). */
    val bcp47: String
) {
    ENGLISH     ("English",      "en"),
    TAGALOG     ("Tagalog",      "tl"),
    CEBUANO     ("Cebuano",      "ceb"),
    KAPAMPANGAN ("Kapampangan",  "pam"),
}

/**
 * CompositionLocal so any composable can read the current language without prop-drilling.
 *
 * NOTE: The global `mutableStateOf` approach has been removed.
 * The single source of truth is now [LanguageRepository]'s StateFlow, collected
 * by [AppViewModel] and provided here via [CompositionLocalProvider] in FloNavGraph.
 * This guarantees ALL tabs see the same value at all times.
 */
val LocalLanguage = compositionLocalOf { AppLanguage.ENGLISH }
