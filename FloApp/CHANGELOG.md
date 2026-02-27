# Ani.PH — Changelog

All notable changes to this project are documented here, in reverse-chronological order.

---

## [0.8.0] — 2026-02-27 · Full English-Only UI Translation

### Goal
Remove all Tagalog, Cebuano, and Kapampangan text from every UI screen,
domain model, AI response, and system string. The app now renders entirely in
English on first launch and for all subsequent sessions. The multi-language
infrastructure (`AppStrings`, `AppLanguage` enum, `LanguagePreferenceManager`)
is preserved as dead-code scaffolding in case multi-language support is ever
re-enabled, but the default language is now locked to **English**.

---

### Files Changed

#### 🖥️ UI Screens
| File | Strings Translated |
|---|---|
| `WeatherScreen.kt` | Title "Panahon / Weather" → "Weather"; tabs "7 Araw na Panahon" / "30 Araw na Pagtataya" → "7-Day Forecast" / "30-Day Outlook"; advisory banner, permission card, empty states, loading strings, info banner. Switched all `displayNameTl` → `displayNameEn`. |
| `AddCropScreen.kt` | Screen title "Bagong Pananim" → "New Crop"; section label "Impormasyon ng Pananim" → "Crop Information"; all field labels and placeholders; save button; date-picker "Cancel" button. |
| `AIAssistantScreen.kt` | Section label "Mga Mabilis na Tanong" → "Quick Questions:"; 4 quick-ask chips rewritten in English; loading text "Flo AI ay nag-iisip…" → "Flo AI is thinking…"; input placeholder "Magtanong kay Flo…" → "Ask Flo anything…". |
| `CropWheelScreen.kt` | Stage name now shows `displayNameEn` only (Tagalog label removed); "N araw na natitira" → "N days remaining" (single English line); section heading "Mga Inirerekomendang Gawain" → "Recommended Actions:"; button "I-log ang Gawain" → "Log Activity". |
| `LoggingScreen.kt` | Title "I-log ang Gawain" → "Log Activity"; heading now English only; sub-heading added "Select the activity you performed."; save button "I-save ang Log" → "Save Log"; confirmation dialog "Na-log na!" → "Logged!", body text translated, "Tapos na" → "Done". Log-type card uses `displayNameEn`. |
| `HarvestForecastScreen.kt` | Empty state strings translated; metric card labels (Yield/Revenue/Cost/Profit) translated to English; risk label uses `displayNameEn` only; "Batayan ng Forecast" → "Forecast Assumptions" (duplicate subtitle removed). |
| `DigitalResiboScreen.kt` | FAB "I-scan ang Resibo" → "Scan Receipt"; info banner title/subtitle translated; scanning state "Binabasa ang resibo…" → "Reading your receipt…"; empty state "Walang resibo pa" → "No receipts yet" with English hint; "Buksan ang PDF" → "Open PDF"; "I-generate ang PDF" → "Generate PDF". |

#### 🧠 Domain Models
| File | Change |
|---|---|
| `WeatherData.kt` | `WeatherAdvisory` enum: `displayNameTl` field **removed** entirely. Only `displayNameEn` + `actionSuggestion` remain. |
| `CropStage.kt` | `CropStageType` enum: `displayNameTl` field and all Tagalog stage names **removed**. Also updated "Digital Resibo" → "Digital Receipt" in action text. |
| `CropLog.kt` | `LogType` enum: `displayNameTl` field and all Tagalog values **removed**. |
| `HarvestForecast.kt` | `RiskLabel` enum: `displayNameTl` and Tagalog values **removed**. |

#### ⚙️ Engine & AI
| File | Change |
|---|---|
| `WeatherInterpreter.kt` | `generateAdvisoryDetail()`: all bilingual Tagalog suffixes stripped. Every advisory detail is now a clean English-only sentence. |
| `LocalAIEngine.kt` | All 6 rule-based response branches rewritten in English only. Tagalog keyword triggers (`palay`, `abono`, `peste`, `ani`, `panahon`, `kita`) are **kept** in the `when` conditions so Tagalog-language prompts still receive correct English answers. |

#### 🗂️ Infrastructure
| File | Change |
|---|---|
| `LocalLanguage.kt` | `compositionLocalOf` default changed from `AppLanguage.TAGALOG` → `AppLanguage.ENGLISH`. |
| `LanguagePreferenceManager.kt` | `loadLanguage()` fallback changed from `AppLanguage.TAGALOG` → `AppLanguage.ENGLISH` (affects fresh installs or cleared storage). |
| `WeatherViewModel.kt` | Error/fallback messages "Gamit ang default na lokasyon (Maynila)" → "Using default location (Manila)"; "Kailangan ng pahintulot sa lokasyon" → "Location permission is required". |

---

### ⚠️ Things the AI Cannot Change

These items were intentionally left as-is because changing them is either
outside the scope of code editing, requires user action, or would break
app behaviour:

| Item | Reason |
|---|---|
| **User-entered crop names** (e.g., "Palay sa Bukid" shown in `CropWheelScreen` title) | This is **data entered by the farmer**, stored in the Room database. The app correctly shows it via `state.cropName`. The AI cannot retroactively translate data rows in a live database. If you want English crop names going forward, simply create new crops with English names. |
| **`AppStrings` Tagalog / Cebuano / Kapampangan string sets** (`TagalogStrings`, `CebuanoStrings`, `KapampanganStrings`) | Kept as dead-code scaffolding. The app no longer routes through them (default is `EnglishStrings`), but removing them would require removing the `AppLanguage` enum values too, which would break the Settings screen language selector. Safe to delete in a future cleanup sprint if multi-language is permanently dropped. |
| **`AppLanguage.TAGALOG / CEBUANO / KAPAMPANGAN` enum entries** | Related to the above — kept for compiler compatibility with `AppStrings.strings()` which switches on all four values. |
| **`displayNameTl` field name in `BottomNavItem.labelTl`** | `FloDestination.kt` still has a `labelTl` property in the `BottomNavItem` data class. The field is declared but the nav graph uses `navLabels` from `AppStrings` (which are now English). The field is harmless dead code and its removal would require refactoring `FloDestination.kt` and `FloNavGraph.kt` together. |
| **Open-Meteo API response field names** (e.g., `temperature_2m_max`) | These are server-defined JSON keys — they cannot be translated. They map directly to Moshi-annotated Kotlin fields in `WeatherApiService.kt`. |
| **Room database column names and `forecastType` string values** (e.g., `"DAILY_FORECAST"`, `"CLIMATOLOGY"`) | Internal storage identifiers used in `@Query` SQL strings. Not user-visible. Changing them would require a Room database migration. |
| **Package names and file paths** (`com.floapp.agriflo`) | Android package identifiers are not translated — they are technical identifiers, not UI strings. |
| **Third-party library strings** (e.g., ML Kit OCR results, ONNX model output) | These come from external dependencies and are outside the app's control. |

---

## [0.7.0] — 2026-02-27 · Mock Weather Seed Data + API Guard Fix


### Goal
Ensure the Weather screen is **never blank** on first launch or while offline.
Seed both the 7-day forecast and 30-day climatology caches immediately with
realistic Philippine seasonal data. Real Open-Meteo data overwrites the mock
automatically when the network call completes.

Also fixed: removed the over-aggressive `isOnline()` guard that was preventing
the archive API from being attempted on the emulator — all API calls now rely
solely on try/catch for offline safety.

### New Files
| File | Purpose |
|---|---|
| `data/remote/mock/WeatherMockDataGenerator.kt` | Sine-wave-modulated Philippine climate mock data generator for 7-day forecast and 30-day climatology; dry season (Nov–May) vs wet season (Jun–Oct) aware |

### Modified Files
| File | What Changed |
|---|---|
| `domain/repository/WeatherRepository.kt` | Added `seedMockDataIfEmpty()` to the interface |
| `data/repository/WeatherRepositoryImpl.kt` | Removed `isOnline()` guard from `refreshClimatology()` (try/catch handles offline cleanly); implemented `seedMockDataIfEmpty()` — seeds mock data for 7-day (if `getForecastCount() == 0`) and climatology (if `getClimatologyLastFetchTimestamp() == null`) |
| `ui/viewmodel/WeatherViewModel.kt` | `init {}` now launches `seedMockDataIfEmpty()` first, then calls `refresh()` — mock data is in cache before the first API call returns |

### Data Flow
```
App opens
    ↓
seedMockDataIfEmpty()  →  Room instantly populated with mock rows
    ↓                         ↓
Room Flows emit         UI renders immediately (no blank screen)
    ↓
refresh() runs concurrently
    ├── refreshWeather()     → real 7-day data from Open-Meteo (overwrites mock via REPLACE)
    └── refreshClimatology() → real 5-yr average from archive API (overwrites mock via REPLACE)
```

### Mock Climate Model
| Season | Months | Temp Min | Temp Max | Rainfall | UV |
|---|---|---|---|---|---|
| **Dry** | Nov–May | 25–27°C | 33–36°C | 0–3 mm | 9–12 |
| **Wet** | Jun–Oct | 23–25°C | 30–32°C | 5–15 mm | 6–9 |

Values are modulated by `sin()` wave per day to avoid flat/identical data.

---

## [0.6.0] — 2026-02-27 · Comprehensive Online/Offline Climate Cache


### Goal
Upgrade the Weather screen to support both a **7-day live forecast** and a **30-day
seasonal outlook** built from 5-year historical averages (climatology). The entire
feature works completely offline using cached Room data, and silently refreshes
in the background when connectivity is detected.

### New Files
| File | Layer | Purpose |
|---|---|---|
| *(none — extended existing files only)* | — | — |

### Modified Files
| File | What Changed |
|---|---|
| `data/remote/api/WeatherApiService.kt` | Added `HistoricalWeatherApiService` interface pointing at `archive-api.open-meteo.com/v1/archive`; accepts `start_date` and `end_date` query params for pulling exact historical windows |
| `di/NetworkModule.kt` | Added `@Named("weather_archive")` Retrofit + `provideHistoricalWeatherApiService()` provider |
| `data/local/dao/WeatherCacheDao.kt` | Added `getClimatology(): Flow` (forecastType = CLIMATOLOGY), `deleteAllClimatology()`, `getClimatologyLastFetchTimestamp()` |
| `domain/repository/WeatherRepository.kt` | Added `getClimatology(): Flow<List<WeatherData>>` and `refreshClimatology(lat, lon): Result<Unit>` to the interface |
| `data/repository/WeatherRepositoryImpl.kt` | Implemented `refreshClimatology()`: fetches the upcoming 30-day window from each of the 5 past years via the archive API, averages per-day-index across all 5 responses, writes `CLIMATOLOGY` rows to Room; added `isOnline()` guard on all network calls so the app never crashes offline |
| `ui/viewmodel/WeatherViewModel.kt` | Added `climatology: StateFlow<List<WeatherData>>`; `refresh()` now concurrently launches both `refreshWeather()` and `refreshClimatology()` using separate `launch {}` blocks |
| `ui/screens/WeatherScreen.kt` | Added `WeatherTab` enum and animated `WeatherTabRow` toggle; "**7 Araw na Panahon**" tab unchanged; new "**30 Araw na Pagtataya**" tab shows climatology info banner + 30 `ClimatologyDayCard` rows; empty states for both tabs while loading |

### Architecture: Offline-First Flow
```
Room (CLIMATOLOGY rows)  ─── getClimatology() Flow ──→  WeatherViewModel ──→  UI (renders immediately from cache)
            ↑
  refreshClimatology()   ←── isOnline() check
            │  YES: call archive API × 5 years, average, write to Room → Flow re-emits
            │  NO:  return Result.failure (no crash, UI shows cached data)
```

### Climatology Algorithm
1. Take today + 1 … today + 30 as the **target window** (next 30 days)
2. For each year back in `[1..5]`, shift the window back by that many years
3. Call `HistoricalWeatherApiService.getHistorical(startDate, endDate)` for each shifted window
4. Accumulate each day's `tempMax`, `tempMin`, `precipitation`, `windSpeed`, `uvIndex` into 30 per-day buckets
5. Average each bucket → 30 averaged `WeatherData` objects
6. Run `WeatherInterpreter.interpret()` on each averaged day to produce the agronomic advisory
7. Store as Room entities with `forecastType = "CLIMATOLOGY"`

### Key Design Decisions
- **No new entity or table** — re-uses `WeatherCacheEntity` with `forecastType = "CLIMATOLOGY"`; no Room migration needed
- **No extra OkHttp client** — archive Retrofit reuses the existing singleton client
- **Concurrent refresh** — 7-day and 30-day fetches do NOT block each other
- **Offline guard** — `isOnline()` checks `NetworkCapabilities.NET_CAPABILITY_INTERNET` before any API call

---

## [0.5.0] — 2026-02-27 · Material 3 Date Picker for Planting Date


### Goal
Replace the free-text planting-date `OutlinedTextField` on the "Bagong Pananim" screen
with a proper read-only field backed by a Material 3 `DatePickerDialog` that defaults
to today's date.

### Modified Files
| File | What Changed |
|---|---|
| `ui/screens/AddCropScreen.kt` | Replaced raw `plantingDateText: String` state with `plantingDate: LocalDate` (defaults to `LocalDate.now()`); added `PlantingDateField` composable at the bottom of the file; updated `createCrop` call to pass `plantingDate.toString()` (ISO `YYYY-MM-DD`) |

### New Composable: `PlantingDateField`
| Feature | Detail |
|---|---|
| **Read-only** | `readOnly = true`, `onValueChange = {}` — keyboard never appears |
| **Display format** | `DateTimeFormatter("MMM dd, yyyy")` e.g. `"Feb 27, 2026"` |
| **Leading icon** | `Icons.Filled.DateRange` — decorative calendar glyph |
| **Trailing icon** | `Icons.Filled.CalendarMonth` — tapping opens the dialog |
| **Dialog default** | `rememberDatePickerState(initialSelectedDateMillis = today)` |
| **Timezone-safe conversion** | `Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()` |
| **Confirm / Cancel** | "OK" updates state; "Kanselahin" dismisses without change |
| **Mode toggle** | `showModeToggle = true` — user can switch between calendar grid and keyboard text-input |
| **ViewModel contract** | `plantingDate.toString()` produces ISO `YYYY-MM-DD`, compatible with existing `LocalDate.parse()` in `AddCropViewModel` |

---

## [0.4.0] — 2026-02-27 · StateFlow-Based Global Language State

### Goal
Fix the multi-tab language bug: when a language was changed on one screen, other tabs
did not recompose because they held their own stale snapshots of a global `mutableStateOf`.
Replaced the Compose-layer hack with a proper `StateFlow`-backed singleton repository
following Clean Architecture conventions.

### New Files
| File | Layer | Purpose |
|---|---|---|
| `domain/repository/LanguageRepository.kt` | Domain | Interface — `language: StateFlow<AppLanguage>` + `setLanguage()` |
| `data/repository/LanguageRepositoryImpl.kt` | Data | `@Singleton` `MutableStateFlow` seeded from `LanguagePreferenceManager` on first creation |
| `di/LanguageModule.kt` | DI | `@Binds @Singleton` — wires interface to implementation at compile time |
| `ui/viewmodel/AppViewModel.kt` | UI | Activity-scoped ViewModel; exposes `language: StateFlow` to `FloNavGraph` |

### Modified Files
| File | What Changed |
|---|---|
| `ui/theme/LocalLanguage.kt` | Removed global `appLanguage = mutableStateOf(…)` — single source of truth is now `LanguageRepositoryImpl._language` |
| `ui/navigation/FloNavGraph.kt` | Accepts `AppViewModel` via `hiltViewModel()`; collects `language` with `collectAsStateWithLifecycle()`; `CompositionLocalProvider` re-provides value to ALL tabs on every emit |
| `ui/viewmodel/SettingsViewModel.kt` | Now injects `LanguageRepository` instead of writing the global state directly |
| `ui/screens/SettingsScreen.kt` | Collects `viewModel.language` as `StateFlow` instead of using `derivedStateOf` over a global |
| `FloApplication.kt` | Removed manual `appLanguage.value = languagePrefs.loadLanguage()` — language restoration now happens automatically inside `LanguageRepositoryImpl`'s constructor |

### Why This Fixes the Bug
`LanguageRepositoryImpl` is a Hilt `@Singleton` — there is exactly **one**
`MutableStateFlow` in the entire app. `AppViewModel` collects it at the NavGraph
root (Activity scope), surviving all tab switches. When `setLanguage()` emits,
`FloNavGraph` recomposes and pushes the new value through `CompositionLocalProvider`
to every active tab simultaneously, on the very next frame.

---

## [0.3.0] — 2026-02-27 · Settings Navigation & 4-Language Support


### Goal
Add a dedicated Settings screen with full dynamic in-app language switching across
English, Tagalog, Cebuano, and Kapampangan. Language preference persists across
app restarts.

### New Files
| File | Purpose |
|---|---|
| `ui/screens/SettingsScreen.kt` | New Settings screen — language selector list with radio circles, back button, and translated section headers |
| `ui/viewmodel/SettingsViewModel.kt` | Updates the global `appLanguage` state instantly and persists the selection via SharedPreferences |
| `data/preference/LanguagePreferenceManager.kt` | `@Singleton` Hilt-injected SharedPreferences wrapper — saves and loads the chosen language |
| `di/PreferenceModule.kt` | Hilt module marker for the preference layer |
| `res/values-tl/strings.xml` | Tagalog Android resource file (placeholder translations ready to fill) |
| `res/values-b+ceb/strings.xml` | Cebuano Android resource file (placeholder translations ready to fill) |
| `res/values-b+pam/strings.xml` | Kapampangan Android resource file (placeholder translations ready to fill) |

### Modified Files
| File | What Changed |
|---|---|
| `ui/theme/LocalLanguage.kt` | Added `CEBUANO` and `KAPAMPANGAN` to `AppLanguage` enum, each with `.nativeName` and `.bcp47` properties |
| `ui/theme/AppStrings.kt` | Added Settings screen strings (`settingsTitle`, `settingsLanguageSection`, `settingsBackDesc`); added `CebuanoStrings` and `KapampanganStrings`; `strings()` now covers all 4 languages |
| `ui/navigation/FloDestination.kt` | Added `Settings` destination object |
| `ui/navigation/FloNavGraph.kt` | Routes to `SettingsScreen`; bottom bar auto-hides on Settings and all sub-screens |
| `ui/screens/HomeScreen.kt` | Settings ⚙️ icon now navigates to the Settings screen (replaced inline dropdown) |
| `FloApplication.kt` | Injects `LanguagePreferenceManager` and restores the persisted language before first composition |
| `res/values/strings.xml` | Updated with all placeholder string keys (English defaults) |

### Design Rules (Enforced)
- **"Ani.PH"** is hardcoded at call sites and never stored in `AppStrings` — it is never translated.
- **Language option labels** (English, Tagalog, Cebuano, Kapampangan) always come from `AppLanguage.nativeName` — they are never looked up from `AppStrings` and can never accidentally be translated.
- Language switching is **instant** — backed by `mutableStateOf` + `CompositionLocalProvider`, no Activity restart required.
- Language choice **persists across restarts** — saved to `SharedPreferences` and restored in `FloApplication.onCreate()`.

---

## [0.2.0] — 2026-02-27 · Bilingual UI (English / Tagalog)

### Goal
Make every visible string on the Landing Page react to a language toggle between
English and Tagalog. The app name must remain untranslated in all cases.

### New Files
| File | Purpose |
|---|---|
| `ui/theme/LocalLanguage.kt` | `AppLanguage` enum + global `appLanguage` mutableState + `LocalLanguage` CompositionLocal |
| `ui/theme/AppStrings.kt` | Centralized bilingual string container (`EnglishStrings`, `TagalogStrings`) with a `strings()` extension helper |

### Modified Files
| File | What Changed |
|---|---|
| `ui/navigation/FloNavGraph.kt` | Holds `language` state via `CompositionLocalProvider`; bottom nav labels show one word based on selected language |
| `ui/screens/HomeScreen.kt` | All static text reads from `language.strings()` — FAB, empty state, buttons, icon descriptions all translate |

---

## [0.1.0] — 2026-02-27 · Landing Page Corrections

### Goal
Correct the Landing Page layout and branding for the initial release.

### Changes
| # | Item | Change |
|---|---|---|
| 1 | **App name** | Renamed from "Flo" / "Agri-Flo" to **Ani.PH** |
| 2 | **Settings menu** | Added ⚙️ `IconButton` in the top-right corner with an English / Tagalog language picker |
| 3 | **🌾 emoji** | Moved from the centered title to the `navigationIcon` slot (upper left of the Top App Bar) |
| 4 | **Bottom nav labels** | Changed from showing both "Tahanan / Home" to showing only one word depending on the active language |

### Modified Files
| File | What Changed |
|---|---|
| `res/values/strings.xml` | `app_name` updated from `"Flo"` to `"Ani.PH"` |
| `ui/screens/HomeScreen.kt` | Rewrote Top App Bar layout; added settings dropdown |
| `ui/navigation/FloNavGraph.kt` | Bottom nav now shows single-language labels |

---

## [0.0.1] — 2026-02-26 · Initial Build & Run

### Goal
Verify the project builds and runs on an Android emulator.

### Notes
- Java 25 (Zulu) is incompatible with Gradle 8.9 + Kotlin compiler — builds must use **Java 21**.
- Command to build: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew installDebug`
- Emulator used: `Medium_Phone_API_36.1`
- App entry point: `com.floapp.agriflo.MainActivity`
