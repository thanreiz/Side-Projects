# Ani.PH — Changelog

All notable changes to this project are documented here, in reverse-chronological order.

---

## [1.2.0] — 2026-02-28 · UI/UX Polish — Navigation, Headers & Cards

### Goal
A set of styling and layout refinements across the Land Data and Weather tabs to
improve visual consistency with the rest of the application.

---

### Files Changed

| File | What Changed |
|---|---|
| `ui/navigation/FloDestination.kt` | Reordered `bottomNavItems` to: Receipt → Weather → Home → AI Assistant → Land Data |
| `ui/screens/WeatherScreen.kt` | Fixed compressed header — `titleLarge` font (matching other tabs), `FastOutSlowInEasing` animation, natural height in expanded state |
| `ui/screens/LandDataScreen.kt` | Same header fixes as Weather; card padding tightened; summary card copy simplified |

---

### Changes in Detail

#### 1. Navigation Tab Reorder (`FloDestination.kt`)
New bottom nav order: **Receipt → Weather → Home → AI Assistant → Land Data**

#### 2. Header Animation & Font (`WeatherScreen.kt`, `LandDataScreen.kt`)
- Font restored to `titleLarge`/bold in both expanded and compressed states —
  matching the `titleLarge` used by Home, Resibo, and AI Assistant headers.
- Expanded state no longer applies `Modifier.height(...)` — the `TopAppBar` uses
  its natural Material3 height and built-in internal padding, eliminating the
  flush-against-edge look.
- Compressed state still constrains height to `56dp` via `animateDpAsState`
  with `FastOutSlowInEasing` (300 ms) for a fluid, non-jerky transition.

#### 3. Land Data Card Padding (`LandDataScreen.kt`)
- `SoilProfileCard` and `FertilizerGuidelineCard` inner `Column` padding changed
  from `padding(16.dp)` (all sides equal) to
  `padding(horizontal = 16.dp, vertical = 14.dp)` — removes the excess bottom
  gap while keeping left/right symmetry.
- Removed a redundant `Spacer(8.dp)` before the source attribution in
  fertilizer cards.

#### 4. Summary Card Copy (`LandDataScreen.kt`)
| Before | After |
|---|---|
| `"🌾 Crops that will PROSPER here"` | `"🌾 Ideal crops"` |
| `"⚠ Crops that will STRUGGLE here"` | `"⚠ Unsuitable crops"` |

---

## [1.1.0] — 2026-02-28 · Land Analysis Tab (Land Data)

### Goal
Implement the **Land Analysis** feature by adding a 5th tab — **Land Data** — to the
bottom navigation bar. The tab displays soil composition data, layman-friendly
explanations, beneficial crop lists, and fertilizer guidelines for the user's
current GPS location. Mock data is seeded from BSWM (Bureau of Soils and Water
Management) official maps and downloadable guides, focused on the **Nueva Ecija**
region of the Philippines.

---

### New Files

| File | Layer | Purpose |
|---|---|---|
| `domain/model/LandData.kt` | Domain | Data model containing `LandData`, `SoilProfile`, and `FertilizerGuideline` data classes |
| `data/remote/mock/LandDataMockGenerator.kt` | Data | BSWM-sourced mock data for 5 Nueva Ecija sub-regions (Cabanatuan, Muñoz, Palayan, Gapan, San Jose); exposes `getAvailableRegions()`, `getDataForRegion()`, and `getDefaultGpsData()` |
| `ui/viewmodel/LandDataViewModel.kt` | UI | Sealed `LandDataUiState` (Loading / Downloading / Success / Error); GPS location seeding; simulated async region download with animated progress |
| `ui/screens/LandDataScreen.kt` | UI | Full composable screen with crop summary card, soil profile card stack, fertilizer guideline cards, region-change warning dialog, and region picker dialog |

### Modified Files

| File | What Changed |
|---|---|
| `ui/navigation/FloDestination.kt` | Added `object LandData : FloDestination("land_data")`; added `BottomNavItem` entry with `Icons.Filled.Landscape` icon and all 4-language labels |
| `ui/navigation/FloNavGraph.kt` | Added `navLandData` to the `navLabels` map; added `composable(FloDestination.LandData.route) { LandDataScreen() }` to the `NavHost` |
| `ui/theme/AppStrings.kt` | Added `navLandData` property to the `AppStrings` data class and all 4 language instances: EN `"Land Data"`, TL `"Lupain"`, CEB `"Yuta"`, KAP `"Lupa"` |

---

### Feature Details

#### Data Layer — BSWM Mock Data
Each of the 5 sub-regions contains:
- Named BSWM soil series (e.g., Abnam Loam Clay, Quingua Silt Loam, Lipa Clay)
- Sand / Silt / Clay percentages, pH value + rating, and organic matter level
- Coverage percentage per soil type in the sub-region
- Seasonal fertilizer recommendations (Wet / Dry) per crop with N, P, K, and Zn kg/ha amounts
- Application schedule notes and BSWM/IRRI/DA source attribution

#### ViewModel — `LandDataUiState` sealed interface
| State | When shown |
|---|---|
| `Loading` | On first launch while GPS is being acquired |
| `Downloading(regionName, progressFraction)` | After the user selects a new region |
| `Success(data)` | Data is ready to display |
| `Error(message)` | Location/network failure (auto-recovers to default data) |

The ViewModel mirrors `WeatherViewModel`'s GPS strategy: fresh one-shot location request
(5-second timeout) before falling back to `lastLocation`, then Geocoder for a readable
place name.

#### Screen Layout

1. **Crop Summary Card** — Top-level overview of all crops that will **prosper** or
   **struggle** across the region's soils, shown as color-coded chips
   (green = good, gold = poor).

2. **Soil Profile Cards** — One `ElevatedCard` per soil type with:
   - Animated horizontal composition bar (Sand 🟡 / Silt 🟢 / Clay 🟤)
   - pH pill and Organic Matter pill
   - **"In Simple Terms"** layman explanation block (💬 quote card)
   - Good crops / Crops to Avoid chip rows
   - "Dominant Soil" badge on the first card

3. **Fertilizer Guideline Cards** — One per crop/season pair with:
   - Element badges for N, P, K, Zn with color-coded backgrounds
   - Application schedule notes
   - Source attribution footer

4. **Scroll-to-top FAB** — Same `KeyboardArrowUp` pattern as WeatherScreen

#### Region Change Flow
1. User taps the 🗺 map icon in the top-right of the header
2. **Warning dialog fires immediately**: *"Changing regions will download extra data from
   the internet. An active internet connection is required."*
3. If **Proceed** → Region picker dialog lists the 5 Nueva Ecija sub-regions
4. Selecting a region triggers a simulated download with an animated `LinearProgressIndicator`
   (~1.2 s at 60 ms / step) before the new data is displayed

---

## [1.0.1] — 2026-02-27 · WeatherScreen Bug Fixes

### Goal
Fix four issues discovered after the v1.0.0 WeatherScreen polish: stale-row date
duplication in the 7-day forecast, incorrect compressed-header alignment and sizing,
missing physical header compression, and the refresh button needing to be icon-only.

---

### Files Changed

| File | What Changed |
|---|---|
| `data/local/entity/WeatherCacheEntity.kt` | Changed PK to stable string — see Bug 5 |
| `data/local/dao/WeatherCacheDao.kt` | Fixed `get7DayForecast()` — see Bug 1 |
| `data/repository/WeatherRepositoryImpl.kt` | Fixed row insertion stability — see Bug 5 |
| `data/remote/mock/WeatherMockDataGenerator.kt` | Fixed mock row insertion stability — see Bug 5 |
| `ui/screens/WeatherScreen.kt` | Fixed `WeatherHeader` and refresh FAB — see Bugs 2–4, 6 |

---

### Bug Fixes

#### Bug 1 — 7-Day Forecast Showed Duplicate / Stale Dates
**Root cause**: `get7DayForecast()` queried `SELECT * … ORDER BY date ASC LIMIT 7`
with no date filter. If the Room database contained `DAILY_FORECAST` rows from a
previous day's session they would fill the first slots in the 7-result window before
today's rows, causing repeated dates.

**Fix**: Added `AND date >= :today` to the query. The parameter defaults to
`LocalDate.now().toString()` so callers need no changes.

```sql
-- Before
SELECT * FROM weather_cache WHERE forecastType = 'DAILY_FORECAST'
ORDER BY date ASC LIMIT 7

-- After
SELECT * FROM weather_cache
WHERE forecastType = 'DAILY_FORECAST' AND date >= :today
ORDER BY date ASC LIMIT 7
```

#### Bug 2 — Compressed Header: Wrong Title/Location Alignment
**Root cause**: The compressed state had location on the left and title on the right — the
opposite of the intended layout.

**Fix**: Swapped the two `Text` composables so **"Weather"** is on the left and the
**location** is on the right. Added `padding(horizontal = 4.dp)` to the `Row` to keep
text off the screen edges.

#### Bug 3 — Header Did Not Physically Shrink on Scroll
**Root cause**: `WeatherHeader` only swapped text positions; the `TopAppBar` height was
unchanged, producing no visual compression effect.

**Fix**: Added `animateDpAsState(targetValue = if (isScrolled) 48.dp else 64.dp,
tween(250))` and applied the animated value to `Modifier.height(headerHeight)` on the
`TopAppBar`. The title also shifts from `titleLarge` (expanded) to `titleSmall`
(compressed) for a more compact feel.

#### Bug 4 — Refresh Button Was Rectangular With Text
**Root cause**: The "Refresh" button from v1.0.0 used a `Button` with a rectangular
`RoundedCornerShape(12.dp)` and included text labels.

**Fix**: Replaced with a `FloatingActionButton` using `CircleShape` — icon-only,
matching the compact style requested. Container colour transitions to `FloGreen200`
while loading (disabled state).

#### Bug 5 — Data Stability: Forecast "Shrinking" on Refresh
**Root cause**: Every time the app parsed new API data or generated mock data, it assigned
a `UUID.randomUUID()` to the `id` field of the `WeatherCacheEntity`. Room's `REPLACE`
strategy relies on the primary key to know which row to overwrite. Because every insert
had a brand new UUID, Room never overwrote anything — it just added new duplicate rows
next to the old ones. The `.distinctBy { it.date }.take(7)` safety net then filtered these
un-ordered duplicates, resulting in missing days in the UI. Over-aggressive seed guards
(`getForecastCount() == 0` instead of counting only current rows) compounded the issue by
suppressing mock regeneration.

**Fix**:
1. Changed `WeatherCacheEntity.id` from a random UUID to a stable composite string:
`"${forecastType}_$date"` (e.g. `"DAILY_FORECAST_2026-02-27"`).
2. Updated `WeatherRepositoryImpl.kt` and `WeatherMockDataGenerator.kt` to use this
stable key so Room's `REPLACE` correctly updates the row in-place.
3. Fixed `seedMockDataIfEmpty()` to purge expired rows first, then count only current/future
rows before deciding whether to re-seed.
4. Fixed the expiration threshold in `refreshWeather()` to `LocalDate.now()` so today's
row is not deleted prematurely.

#### Bug 6 — Compressed Header Text Clipped at Bottom
**Root cause**: The `48.dp` compressed height left no room for vertical padding,
leaving the "Weather" and "📍 Location" text visually resting directly on the bottom edge.

**Fix**: Increased `compressedHeight` to `60.dp` and widened horizontal padding to `16.dp`.
Added `padding(vertical = 8.dp)` to the compressed `Row` to give the text proper breathing
room from the header's bounds while maintaining the Left/Right alignment.

---

## [1.0.0] — 2026-02-27 · WeatherScreen UI Enhancements

### Goal
Polish the Weather screen with six UX improvements: quick scroll navigation,
improved date readability, contextual today/tomorrow highlights,
a purpose-built refresh button, and a space-efficient compressed header.

---

### Files Changed

#### 🖥️ UI Screens
| File | What Changed |
|---|---|
| `ui/screens/WeatherScreen.kt` | All 6 enhancements applied — see details below |

---

### Feature Details

#### 1. Scroll-to-Top Button
- Added `rememberLazyListState()` shared across both tabs.
- An `AnimatedVisibility` `SmallFloatingActionButton` (↑ `KeyboardArrowUp`, `FloGreen700`) appears at `TopCenter` once `firstVisibleItemIndex > 0`.
- Tapping it triggers `listState.animateScrollToItem(0)` via a coroutine scope.
- Tab switches automatically reset scroll position to 0.

#### 2. 7-Day Forecast Date Formatting
- Replaced the raw `"YYYY-MM-DD"` string with a two-part `Row`:
  - **Day name** (e.g. `"Monday"`) in bold default text color.
  - **Month + day** (e.g. `"February 27"`) at 45% opacity to de-emphasize it.
- Helper: `formatForecastDate(iso) → Pair(dayName, monthDay)` using `DateTimeFormatter`.

#### 3. 30-Day Climatology Date Formatting
- Date label changed from `"YYYY-MM-DD"` to `"MMMM d, yyyy"` (e.g. `"February 28, 2026"`).
- Helper: `formatClimatologyDate(iso) → String`.

#### 4. Today / Tomorrow Highlights
- `dayLabel(iso)` compares each card's date against `LocalDate.now()`.
- Matching cards receive:
  - A `2.dp FloGreen500` border via `Modifier.border(...)` on the `ElevatedCard`.
  - A slightly lighter `FloGreen100` background gradient (was `FloGreen50` / `FloBlue50`).
  - A small `"Today"` / `"Tomorrow"` chip (`FloGreen100` background, `FloGreen700` bold text) above the day name.
- Works identically on both the 7-day and 30-day lists.

#### 5. Custom Refresh Button
- Removed the refresh `IconButton` and loading `CircularProgressIndicator` from the `TopAppBar` actions block entirely.
- Added a dark-green `Button` pinned to `BottomEnd` of the screen:
  - `containerColor = FloGreen700`, `shape = RoundedCornerShape(12.dp)` (beveled).
  - Shows `Icon(Refresh)` + `Text("Refresh")` when idle.
  - Shows an inline `CircularProgressIndicator(18.dp)` + `Text("Refreshing…")` while loading.
  - Disabled (greyed to `FloGreen200`) while a refresh is in progress.

#### 6. Sticky Compressed Header
- `TopAppBar` replaced by a private `WeatherHeader(isScrolled)` composable.
- **Expanded** (`firstVisibleItemIndex == 0`): `"Weather"` title stacked above `"📍 location"` — same as before.
- **Compressed** (`firstVisibleItemIndex > 0`): single `Row` with `Arrangement.SpaceBetween` — location string moves to the **left**, `"Weather"` title moves to the **right**.
- `containerColor = FloGreen100` is unchanged in both states.

---

### New Imports Added
| Import | Purpose |
|---|---|
| `androidx.compose.animation.AnimatedVisibility` | Scroll-to-top button show/hide |
| `androidx.compose.animation.fadeIn/Out, scaleIn/Out` | Button enter/exit animation |
| `androidx.compose.foundation.border` | Today/Tomorrow card border |
| `androidx.compose.foundation.lazy.rememberLazyListState` | Shared list scroll state |
| `kotlinx.coroutines.launch` | Animate scroll to top |
| `java.time.LocalDate` / `java.time.format.DateTimeFormatter` | Date parsing and formatting |
| `java.util.Locale` | English locale for date formatter |

---

## [0.9.0] — 2026-02-27 · Windows Build Infrastructure & GPS Location Fix

### Goal
Two independent fixes applied in the same session:

1. **Windows build infrastructure** — the project was missing `gradlew.bat` (the Windows
   Gradle wrapper) and `local.properties` (Android SDK path). Without these, no Gradle
   command could be run on Windows at all. Both files were created so the project can
   be built, installed, and launched from the command line on Windows without Android Studio.

2. **Stale GPS location fix** — the Weather screen was always showing forecast data for
   **Mountain View, California** (Google's HQ) instead of the device's actual location.
   The root cause was that `FusedLocationProviderClient.lastLocation` returns a
   system-level cached fix that Android emulators seed with California coordinates by
   default. This cache persists across app reinstalls because it is owned by Google Play
   Services, not the app. The fix reorders the location strategy so a **fresh one-shot
   GPS fix** is always attempted first, bypassing the stale cache.

---

### New Files
| File | Purpose |
|---|---|
| `gradlew.bat` | Standard Windows Gradle wrapper batch script — was missing from the repository; required to run any `.\gradlew.bat` command on Windows |
| `local.properties` | Gradle SDK path file; sets `sdk.dir=C:\Users\User\AppData\Local\Android\Sdk` so the build system can locate the Android SDK |

### Modified Files
| File | What Changed |
|---|---|
| `ui/viewmodel/WeatherViewModel.kt` | Rewrote `getBestLocation()`: now requests a **fresh one-shot high-accuracy fix** (5-second `withTimeoutOrNull` window) **before** falling back to `lastLocation`. Previously, `lastLocation` was tried first — returning the emulator's stale California cache — with a fresh request only as a fallback. The new order ensures the emulator's Extended Controls location is always used when it is available. Added `kotlinx.coroutines.withTimeoutOrNull` import. |

### Why Reinstalling the App Did Not Fix the Location
The `lastLocation` cache is stored inside **Google Play Services** (`com.google.android.gms`),
a system-level process that is completely independent of the app. Uninstalling and reinstalling
`com.floapp.agriflo` has no effect on Play Services' cache. Only requesting a live
`requestLocationUpdates` fix (or rebooting the emulator after setting a new GPS location in
Extended Controls) clears the stale value.

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
