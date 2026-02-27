package com.floapp.agriflo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private enum class WeatherTab { FORECAST_7, CLIMATOLOGY_30 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = hiltViewModel()) {
    val forecasts        by viewModel.forecasts.collectAsStateWithLifecycle()
    val climatology      by viewModel.climatology.collectAsStateWithLifecycle()
    val criticalAdvisory by viewModel.criticalAdvisory.collectAsStateWithLifecycle()
    val isLoading        by viewModel.isLoading.collectAsStateWithLifecycle()
    val locationError    by viewModel.locationError.collectAsStateWithLifecycle()
    val locationName     by viewModel.locationName.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(WeatherTab.FORECAST_7) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Weather",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        locationName?.let {
                            Text(
                                "📍 $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FloGreen100),
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Filled.Refresh, "Refresh weather")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
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
                        colors = CardDefaults.cardColors(containerColor = FloGold100),
                        shape  = RoundedCornerShape(12.dp),
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
                        items(forecasts) { forecast -> WeatherDayCard(forecast = forecast) }
                    }
                }
                WeatherTab.CLIMATOLOGY_30 -> {
                    item { ClimatologyInfoBanner() }
                    if (climatology.isEmpty()) {
                        item { EmptyClimatologyCard(isLoading = isLoading) }
                    } else {
                        items(climatology) { day -> ClimatologyDayCard(day = day) }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WeatherTabRow(activeTab: WeatherTab, onTabSelected: (WeatherTab) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
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
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                color = bgColor
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ClimatologyInfoBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = FloBlue50),
        shape = RoundedCornerShape(12.dp),
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
private fun ClimatologyDayCard(day: WeatherData) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(FloBlue50, Color.White)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1.5f)) {
                Text(day.date, style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Text("5-yr avg  ·  ${day.advisory.displayNameEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FloGreen700, fontWeight = FontWeight.Medium)
                Text(day.advisory.actionSuggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("🌡 ${day.tempMinC.toInt()}–${day.tempMaxC.toInt()}°C",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (day.rainfallMm > 0)
                    Text("💧 %.1fmm".format(day.rainfallMm),
                        style = MaterialTheme.typography.bodySmall, color = FloBlue700)
                if (day.windSpeedKph > 0)
                    Text("💨 %.0f km/h".format(day.windSpeedKph),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (day.uvIndex > 0)
                    Text("☀ UV ${day.uvIndex.toInt()}",
                        style = MaterialTheme.typography.bodySmall, color = FloGold700)
            }
        }
    }
}

@Composable
private fun EmptyClimatologyCard(isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                CircularProgressIndicator(color = FloGreen700)
                Spacer(Modifier.height(12.dp))
                Text("Fetching 5-year history…", style = MaterialTheme.typography.bodyLarge)
            } else {
                Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("No outlook cached", style = MaterialTheme.typography.bodyLarge)
                Text("Tap refresh to fetch the 30-day outlook from 5-year history",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun PermissionRequiredCard(onRequest: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FloBlue50),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.LocationOn, null, modifier = Modifier.size(40.dp), tint = FloGreen700)
            Spacer(Modifier.height(8.dp))
            Text("Location Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("To show accurate weather for your farm, please grant location permission.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = FloGreen700)) {
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
        WeatherAdvisory.DROUGHT_RISK         -> FloGold200 to Icons.Filled.WbSunny
        WeatherAdvisory.PEST_RISK_HIGH       -> FloRed100  to Icons.Filled.BugReport
        WeatherAdvisory.STRONG_WIND_WARNING  -> FloRed100  to Icons.Filled.Air
        else -> FloGreen50 to Icons.Filled.CheckCircle
    }
    Card(shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()) {
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
private fun WeatherDayCard(forecast: WeatherData) {
    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(FloGreen50, Color.White)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1.4f)) {
                Text(forecast.date, style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Text(forecast.advisory.displayNameEn, style = MaterialTheme.typography.bodySmall,
                    color = FloGreen700, fontWeight = FontWeight.Medium)
                Text(forecast.advisory.actionSuggestion, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("🌡 ${forecast.tempMinC.toInt()}–${forecast.tempMaxC.toInt()}°C",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (forecast.rainfallMm  > 0) Text("💧 %.1fmm".format(forecast.rainfallMm),
                    style = MaterialTheme.typography.bodySmall, color = FloBlue700)
                if (forecast.windSpeedKph > 0) Text("💨 %.0f km/h".format(forecast.windSpeedKph),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (forecast.uvIndex     > 0) Text("☀ UV ${forecast.uvIndex.toInt()}",
                    style = MaterialTheme.typography.bodySmall, color = FloGold700)
            }
        }
    }
}

@Composable
private fun EmptyWeatherCard(isLoading: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                CircularProgressIndicator(color = FloGreen700)
                Spacer(Modifier.height(12.dp))
                Text("Fetching weather…", style = MaterialTheme.typography.bodyLarge)
            } else {
                Icon(Icons.Filled.CloudOff, null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Text("No weather cached", style = MaterialTheme.typography.bodyLarge)
                Text("Tap refresh or grant location access",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}
