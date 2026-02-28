package com.floapp.agriflo.ui.theme

/**
 * All user-visible strings for the app.
 *
 * RULES:
 *  - App name "Ani.PH" is NEVER stored here — it stays hardcoded wherever it appears.
 *  - Language option labels (English, Tagalog, Cebuano, Kapampangan) are NEVER stored here;
 *    they are always read from AppLanguage.nativeName so they are always in their native form.
 *  - Add every new UI string to this class and all four language instances below.
 */
data class AppStrings(

    // ── Top-bar / Settings icon ────────────────────────────────────────────────
    val settingsContentDesc: String,

    // ── Settings Screen ────────────────────────────────────────────────────────
    val settingsTitle: String,
    val settingsLanguageSection: String,
    val settingsBackDesc: String,

    // ── Bottom navigation ──────────────────────────────────────────────────────
    val navHome: String,
    val navWeather: String,
    val navResibo: String,
    val navAI: String,
    val navLandData: String,

    // ── Home – FAB ─────────────────────────────────────────────────────────────
    val fabAddCrop: String,

    // ── Home – Empty-state ─────────────────────────────────────────────────────
    val emptyTitle: String,
    val emptySubtitle: String,
    val emptyButton: String,

    // ── Home – Crop card ───────────────────────────────────────────────────────
    val cropCardAreaSuffix: String,
    val cropCardContentDesc: String,
)

// ─── English ──────────────────────────────────────────────────────────────────

val EnglishStrings = AppStrings(
    settingsContentDesc      = "Settings",

    settingsTitle            = "Settings",
    settingsLanguageSection  = "Language",
    settingsBackDesc         = "Back",

    navHome     = "Home",
    navWeather  = "Weather",
    navResibo   = "Receipt",
    navAI       = "AI Assistant",
    navLandData = "Land Data",

    fabAddCrop  = "New Crop",

    emptyTitle    = "No crops yet",
    emptySubtitle = "Tap the button below to add your first crop.",
    emptyButton   = "Plant now",

    cropCardAreaSuffix  = " ha",
    cropCardContentDesc = "View crop",
)

// ─── Tagalog ──────────────────────────────────────────────────────────────────

val TagalogStrings = AppStrings(
    settingsContentDesc      = "Mga Setting",

    settingsTitle            = "Mga Setting",
    settingsLanguageSection  = "Wika",
    settingsBackDesc         = "Bumalik",

    navHome     = "Tahanan",
    navWeather  = "Panahon",
    navResibo   = "Resibo",
    navAI       = "AI Katulong",
    navLandData = "Lupain",

    fabAddCrop  = "Bagong Pananim",

    emptyTitle    = "Wala pang pananim",
    emptySubtitle = "I-tap ang pindutan sa ibaba para magdagdag ng pananim.",
    emptyButton   = "Magtanim ngayon",

    cropCardAreaSuffix  = " ha",
    cropCardContentDesc = "Tingnan ang pananim",
)

// ─── Cebuano ──────────────────────────────────────────────────────────────────
// TODO: Replace placeholder translations with verified Cebuano text.

val CebuanoStrings = AppStrings(
    settingsContentDesc      = "Mga Setting",

    settingsTitle            = "Mga Setting",
    settingsLanguageSection  = "Pinulongan",
    settingsBackDesc         = "Balik",

    navHome     = "Balay",
    navWeather  = "Panahon",
    navResibo   = "Resibo",
    navAI       = "AI Katabang",
    navLandData = "Yuta",

    fabAddCrop  = "Bag-ong Tanom",

    emptyTitle    = "Walay tanom pa",
    emptySubtitle = "I-tap ang buton sa ubos aron magdugang og tanom.",
    emptyButton   = "Magtanom na",

    cropCardAreaSuffix  = " ha",
    cropCardContentDesc = "Tan-awa ang tanom",
)

// ─── Kapampangan ──────────────────────────────────────────────────────────────
// TODO: Replace placeholder translations with verified Kapampangan text.

val KapampanganStrings = AppStrings(
    settingsContentDesc      = "Mga Setting",

    settingsTitle            = "Mga Setting",
    settingsLanguageSection  = "Amanu",
    settingsBackDesc         = "Balik",

    navHome     = "Abung",
    navWeather  = "Panahon",
    navResibo   = "Resibo",
    navAI       = "AI Katulung",
    navLandData = "Lupa",

    fabAddCrop  = "Bayung Tanam",

    emptyTitle    = "Ala pang tanam",
    emptySubtitle = "I-tap ing button king baba para makadugang ka ning tanam.",
    emptyButton   = "Magtanam na",

    cropCardAreaSuffix  = " ha",
    cropCardContentDesc = "Lakuan ing tanam",
)

// ─── Helper ───────────────────────────────────────────────────────────────────

/** Returns the correct [AppStrings] for this [AppLanguage]. */
fun AppLanguage.strings(): AppStrings = when (this) {
    AppLanguage.ENGLISH      -> EnglishStrings
    AppLanguage.TAGALOG      -> TagalogStrings
    AppLanguage.CEBUANO      -> CebuanoStrings
    AppLanguage.KAPAMPANGAN  -> KapampanganStrings
}
