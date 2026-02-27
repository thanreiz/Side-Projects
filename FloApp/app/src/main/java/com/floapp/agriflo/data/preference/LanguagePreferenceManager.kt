package com.floapp.agriflo.data.preference

import android.content.Context
import com.floapp.agriflo.ui.theme.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user's chosen [AppLanguage] in SharedPreferences so the
 * preference survives app restarts.
 */
@Singleton
class LanguagePreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Save [language] to disk. */
    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
    }

    /**
     * Load the persisted language, falling back to [AppLanguage.ENGLISH]
     * if nothing is saved yet.
     */
    fun loadLanguage(): AppLanguage {
        val name = prefs.getString(KEY_LANGUAGE, null) ?: return AppLanguage.ENGLISH
        return runCatching { AppLanguage.valueOf(name) }.getOrDefault(AppLanguage.ENGLISH)
    }

    companion object {
        private const val PREFS_NAME = "ani_ph_prefs"
        private const val KEY_LANGUAGE = "selected_language"
    }
}
