package com.floapp.agriflo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.floapp.agriflo.domain.model.WeatherAdvisory
import com.floapp.agriflo.domain.model.WeatherData
import com.floapp.agriflo.ui.theme.*
import com.floapp.agriflo.ui.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class WeatherTab { FORECAST_7, CLIMATOLOGY_30 }

// ── Date helpers ──────────────────────────────────────────────────────────────

/** Returns Pair(dayName, "Month Day") e.g. ("Monday", "February 27") */
private fun formatForecastDate(iso: String): Pair<String, String> = try {
    val date = LocalDate.parse(iso)
    val dayName   = date.format(DateTimeFormatter.ofPattern("EEEE",  Locale.ENGLISH))
    val monthDay  = date.format(DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH))
    Pair(dayName, monthDay)
} catch (_: Exception) { Pair(iso, "") }

/** Returns "February 27, 2026" */
private fun formatClimatologyDate(iso: String): String = try {
    LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
} catch (_: Exception) { iso }

/** Returns "Today", "Tomorrow", or null */
private fun dayLabel(iso: String): String? {
    val date  = try { LocalDate.parse(iso) } catch (_: Exception) { return null }
    val today = LocalDate.now()
    return when (date) {
        today            -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else             -> null
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = hiltViewModel()) {
    val forecasts        by viewModel.forecasts.collectAsStateWithLifecycle()
    val climatology      by viewModel.climatology.collectAsStateWithLifecycle()
    val criticalAdvisory by viewModel.criticalAdvisory.collectAsStateWithLifecycle()
    val isLoading        by viewModel.isLoading.collectAsStateWithLifecycle()
    val locationError    by viewModel.locationError.collectAsStateWithLifecycle()
    val locationName     by viewModel.locationName.collectAsStateWithLifecycle()

    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val listState  = rememberLazyListState()

    var activeTab        by remember { mutableStateOf(WeatherTab.FORECAST_7) }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) { permissionDenied = false; viewModel.onPermissionGranted() }
        else permissionDenied = true
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine) permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Reset scroll position when switching tabs so the scroll-to-top button state is correct
    LaunchedEffect(activeTab) { listState.scrollToItem(0) }

    val isScrolled = listState.firstVisibleItemIndex > 0

    Scaffold(
        topBar = {
            WeatherHeader(
                locationName = locationName,
                isScrolled   = isScrolled
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Main list ─────────────────────────────────────────────────────
            LazyColumn(
                state              = listState,
                modifier           = Modifier.fillMaxSize(),
                contentPadding     = PaddingValues(start = 16.dp, end = 16.dp,
                                                   top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (permissionDenied) {
                    item { PermissionRequiredCard { permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) } }
                }

                locationError?.let { err ->
                    item {
                        Card(
                            colors   = CardDefaults.cardColors(containerColor = FloGold100),
                            shape    = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, null, tint = FloGold700, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(err, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (activeTab == WeatherTab.FORECAST_7) {
                    criticalAdvisory?.let { advisory ->
                        item { AdvisoryBanner(advisory = advisory) }
                    }
                }

                item { WeatherTabRow(activeTab = activeTab, onTabSelected = { activeTab = it }) }

                when (activeTab) {
                    WeatherTab.FORECAST_7 -> {
                        if (forecasts.isEmpty()) {
                            item { EmptyWeatherCard(isLoading = isLoading) }
                        } else {
                            items(forecasts) { forecast ->
                                WeatherDayCard(
                                    forecast  = forecast,
                                    highlight = dayLabel(forecast.date) != null,
                                    label     = dayLabel(forecast.date)
                                )
                            }
                        }
                    }
                    WeatherTab.CLIMATOLOGY_30 -> {
                        item { ClimatologyInfoBanner() }
                        if (climatology.isEmpty()) {
                            item { EmptyClimatologyCard(isLoading = isLoading) }
                        } else {
                            items(climatology) { day ->
                                ClimatologyDayCard(
                                    day       = day,
                                    highlight = dayLabel(day.date) != null,
                                    label     = dayLabel(day.date)
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            // ── Scroll-to-top button ──────────────────────────────────────────
            AnimatedVisibility(
                visible = isScrolled,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                exit  = fadeOut(tween(200)) + scaleOut(tween(200))
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    containerColor = FloGreen700,
                    contentColor   = FloWhite,
                    shape          = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            // ── Custom Refresh FAB (circular, icon-only) ──────────────────────
            FloatingActionButton(
                onClick         = { if (!isLoading) viewModel.refresh() },
                modifier        = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape           = androidx.compose.foundation.shape.CircleShape,
                containerColor  = if (isLoading) FloGreen200 else FloGreen700,
                contentColor    = FloWhite
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = FloWhite,
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh weather",
                        modifier           = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ── Compressed / Sticky Header ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherHeader(locationName: String?, isScrolled: Boolean) {
    // Only constrain height when compressed; expanded state uses TopAppBar's
    // natural height (with built-in internal padding) to match other tabs.
    val compressedHeight by animateDpAsState(
        targetValue   = 56.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label         = "weatherHeaderHeight"
    )

    TopAppBar(
        modifier = if (isScrolled) Modifier.height(compressedHeight) else Modifier,
        colors   = TopAppBarDefaults.topAppBarColors(containerColor = FloGreen100),
        title = {
            if (!isScrolled) {
                // Expanded: title stacked above location
                Column {
                    Text(
                        "Weather",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    locationName?.let {
                        Text(
                            "📍 $it",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            } else {
                // Compressed: "Weather" left, location right
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Weather",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    locationName?.let {
                        Text(
                            "📍 $it",
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines  = 1,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    )
}

// ── Tab row ───────────────────────────────────────────────────────────────────

@Composable
private fun WeatherTabRow(activeTab: WeatherTab, onTabSelected: (WeatherTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val tabs = listOf(
            WeatherTab.FORECAST_7     to "🌤 7-Day Forecast",
            WeatherTab.CLIMATOLOGY_30 to "📊 30-Day Outlook"
        )
        tabs.forEach { (tab, label) ->
            val selected = tab == activeTab
            val bgColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                tween(200), label = "tabBg"
            )
            val textColor by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                tween(200), label = "tabText"
            )
            Surface(
                onClick  = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                color    = bgColor
            ) {
                Text(
                    label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = textColor,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                )
            }
        }
    }
}

// ── 7-Day Forecast Card ───────────────────────────────────────────────────────

@Composable
private fun WeatherDayCard(forecast: WeatherData, highlight: Boolean, label: String?) {
    val (dayName, monthDay) = formatForecastDate(forecast.date)
    val borderModifier = if (highlight)
        Modifier.border(2.dp, FloGreen500, RoundedCornerShape(16.dp))
    else Modifier

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (highlight) listOf(FloGreen100, FloWhite)
                        else           listOf(FloGreen50,  FloWhite)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1.4f)) {
                // Today / Tomorrow chip
                if (label != null) {
                    Surface(
                        shape  = RoundedCornerShape(6.dp),
                        color  = FloGreen100,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            label,
                            style     = MaterialTheme.typography.labelSmall,
                            color     = FloGreen700,
                            fontWeight = FontWeight.Bold,
                            modifier  = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                // Day name + month-day in one row
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        dayName,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        monthDay,
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    forecast.advisory.displayNameEn,
                    style      = MaterialTheme.typography.bodySmall,
                    color      = FloGreen700,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    forecast.advisory.actionSuggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "🌡 ${forecast.tempMinC.toInt()}–${forecast.tempMaxC.toInt()}°C",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (forecast.rainfallMm > 0)
                    Text(
                        "💧 %.1fmm".format(forecast.rainfallMm),
                        style = MaterialTheme.typography.bodySmall, color = FloBlue700
                    )
                if (forecast.windSpeedKph > 0)
                    Text(
                        "💨 %.0f km/h".format(forecast.windSpeedKph),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                if (forecast.uvIndex > 0)
                    Text(
                        "☀ UV ${forecast.uvIndex.toInt()}",
                        style = MaterialTheme.typography.bodySmall, color = FloGold700
                    )
            }
        }
    }
}

// ── 30-Day Climatology Card ───────────────────────────────────────────────────

@Composable
private fun ClimatologyDayCard(day: WeatherData, highlight: Boolean, label: String?) {
    val formattedDate  = formatClimatologyDate(day.date)
    val borderModifier = if (highlight)
        Modifier.border(2.dp, FloGreen500, RoundedCornerShape(16.dp))
    else Modifier

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (highlight) listOf(FloGreen100, FloWhite)
                        else           listOf(FloBlue50,   FloWhite)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1.5f)) {
                // Today / Tomorrow chip
                if (label != null) {
                    Surface(
                        shape    = RoundedCornerShape(6.dp),
                        color    = FloGreen100,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            label,
                            style      = MaterialTheme.typography.labelSmall,
                            color      = FloGreen700,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    formattedDate,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "5-yr avg  ·  ${day.advisory.displayNameEn}",
                    style      = MaterialTheme.typography.bodySmall,
                    color      = FloGreen700,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    day.advisory.actionSuggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "🌡 ${day.tempMinC.toInt()}–${day.tempMaxC.toInt()}°C",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (day.rainfallMm > 0)
                    Text(
                        "💧 %.1fmm".format(day.rainfallMm),
                        style = MaterialTheme.typography.bodySmall, color = FloBlue700
                    )
                if (day.windSpeedKph > 0)
                    Text(
                        "💨 %.0f km/h".format(day.windSpeedKph),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                if (day.uvIndex > 0)
                    Text(
                        "☀ UV ${day.uvIndex.toInt()}",
                        style = MaterialTheme.typography.bodySmall, color = FloGold700
                    )
            }
        }
    }
}

// ── Supporting Composables (unchanged logic, same style) ──────────────────────

@Composable
private fun ClimatologyInfoBanner() {
    Card(
        colors   = CardDefaults.cardColors(containerColor = FloBlue50),
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, null, tint = FloGreen700, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Based on 5-year historical averages for the same 30-day window. " +
                "Use this for long-term crop planning.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun EmptyClimatologyCard(isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(
            Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = FloGreen700)
                Spacer(Modifier.height(12.dp))
                Text("Fetching 5-year history…", style = MaterialTheme.typography.bodyLarge)
            } else {
                Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("No outlook cached", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Tap Refresh to fetch the 30-day outlook from 5-year history",
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PermissionRequiredCard(onRequest: () -> Unit) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = FloBlue50),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(40.dp), tint = FloGreen700)
            Spacer(Modifier.height(8.dp))
            Text("Location Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "To show accurate weather for your farm, please grant location permission.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRequest,
                colors  = ButtonDefaults.buttonColors(containerColor = FloGreen700)
            ) {
                Icon(Icons.Filled.MyLocation, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun AdvisoryBanner(advisory: WeatherAdvisory) {
    val (bgColor, icon) = when (advisory) {
        WeatherAdvisory.HIGH_RAIN_RISK, WeatherAdvisory.DELAY_FERTILIZATION ->
            FloBlue100 to Icons.Filled.Water
        WeatherAdvisory.DROUGHT_RISK        -> FloGold200 to Icons.Filled.WbSunny
        WeatherAdvisory.PEST_RISK_HIGH      -> FloRed100  to Icons.Filled.BugReport
        WeatherAdvisory.STRONG_WIND_WARNING -> FloRed100  to Icons.Filled.Air
        else                                -> FloGreen50 to Icons.Filled.CheckCircle
    }
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(40.dp), tint = FloGreen700)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(advisory.displayNameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(advisory.actionSuggestion, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmptyWeatherCard(isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(
            Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = FloGreen700)
                Spacer(Modifier.height(12.dp))
                Text("Fetching weather…", style = MaterialTheme.typography.bodyLarge)
            } else {
                Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("No weather cached", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Tap Refresh or grant location access",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
