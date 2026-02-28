package com.floapp.agriflo.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.floapp.agriflo.domain.model.FertilizerGuideline
import com.floapp.agriflo.domain.model.LandData
import com.floapp.agriflo.domain.model.SoilProfile
import com.floapp.agriflo.ui.theme.*
import com.floapp.agriflo.ui.viewmodel.LandDataUiState
import com.floapp.agriflo.ui.viewmodel.LandDataViewModel
import kotlinx.coroutines.launch

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandDataScreen(viewModel: LandDataViewModel = hiltViewModel()) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val locationName by viewModel.locationName.collectAsStateWithLifecycle()

    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val listState  = rememberLazyListState()

    // Dialog state
    var showRegionWarning  by remember { mutableStateOf(false) }
    var showRegionPicker   by remember { mutableStateOf(false) }
    var pendingRegionKey   by remember { mutableStateOf<String?>(null) }
    var pendingRegionName  by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onPermissionGranted()
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

    val isScrolled = listState.firstVisibleItemIndex > 0

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showRegionWarning) {
        RegionChangeWarningDialog(
            onConfirm = {
                showRegionWarning = false
                showRegionPicker  = true
            },
            onDismiss = { showRegionWarning = false }
        )
    }

    if (showRegionPicker) {
        RegionPickerDialog(
            regions   = viewModel.availableRegions,
            onRegionSelected = { key, name ->
                showRegionPicker  = false
                pendingRegionKey  = key
                pendingRegionName = name
                viewModel.loadRegion(key, name)
            },
            onDismiss = { showRegionPicker = false }
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            LandDataHeader(
                locationName   = locationName,
                isScrolled     = isScrolled,
                onChangeRegion = { showRegionWarning = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {

                // ── Loading ───────────────────────────────────────────────────
                is LandDataUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = FloGreen700)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Detecting your location…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // ── Downloading ───────────────────────────────────────────────
                is LandDataUiState.Downloading -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint     = FloGreen700
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Downloading data for",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            state.regionName,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress   = state.progressFraction,
                            modifier   = Modifier.fillMaxWidth(),
                            color      = FloGreen700,
                            trackColor = FloGreen100
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${(state.progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = FloGreen700
                        )
                    }
                }

                // ── Error (brief; then transitions to Success with default data) ──
                is LandDataUiState.Error -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors   = CardDefaults.cardColors(containerColor = FloGold100),
                                shape    = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = FloGold700, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(state.message, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // ── Success ───────────────────────────────────────────────────
                is LandDataUiState.Success -> {
                    LazyColumn(
                        state               = listState,
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(
                            start   = 16.dp, end = 16.dp,
                            top     = 16.dp, bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ── Crop Suitability Summary ──────────────────────────
                        item { CropSummaryCard(data = state.data) }

                        // ── Soil Profile Cards ────────────────────────────────
                        item {
                            SectionHeader(
                                icon  = "🌱",
                                title = "Soil Profiles (${state.data.soilProfiles.size} found)"
                            )
                        }
                        itemsIndexed(state.data.soilProfiles) { idx, profile ->
                            SoilProfileCard(profile = profile, index = idx)
                        }

                        // ── Fertilizer Guidelines ─────────────────────────────
                        item {
                            SectionHeader(
                                icon  = "🧪",
                                title = "Fertilizer Guidelines"
                            )
                        }
                        item {
                            DataSourceBanner()
                        }
                        itemsIndexed(state.data.fertilizerGuidelines) { _, guideline ->
                            FertilizerGuidelineCard(guideline = guideline)
                        }

                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }

            // ── Scroll-to-top button ──────────────────────────────────────────
            AnimatedVisibility(
                visible = isScrolled && uiState is LandDataUiState.Success,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp),
                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                exit  = fadeOut(tween(200)) + scaleOut(tween(200))
            ) {
                SmallFloatingActionButton(
                    onClick        = { scope.launch { listState.animateScrollToItem(0) } },
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
        }
    }
}

// ── Compressed / Sticky Header ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandDataHeader(
    locationName   : String?,
    isScrolled     : Boolean,
    onChangeRegion : () -> Unit
) {
    val compressedHeight by animateDpAsState(
        targetValue   = 56.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label         = "landHeaderHeight"
    )

    TopAppBar(
        modifier = if (isScrolled) Modifier.height(compressedHeight) else Modifier,
        colors   = TopAppBarDefaults.topAppBarColors(containerColor = FloGreen100),
        title = {
            if (!isScrolled) {
                Column {
                    Text(
                        "Land Data",
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
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Land Data",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    locationName?.let {
                        Text(
                            "📍 $it",
                            style     = MaterialTheme.typography.bodySmall,
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines  = 1,
                            textAlign = TextAlign.End,
                            modifier  = Modifier.weight(1f, fill = false).padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onChangeRegion) {
                Icon(
                    Icons.Filled.Map,
                    contentDescription = "Change region",
                    tint = FloGreen700
                )
            }
        }
    )
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

/**
 * Warning dialog shown IMMEDIATELY when the user taps "Change Region".
 * Informs them that extra data will be downloaded.
 */
@Composable
private fun RegionChangeWarningDialog(
    onConfirm : () -> Unit,
    onDismiss : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = {
            Icon(Icons.Filled.WifiTethering, null, tint = FloGold700, modifier = Modifier.size(32.dp))
        },
        title   = {
            Text("Internet Required", fontWeight = FontWeight.Bold)
        },
        text    = {
            Text(
                "Changing regions will download extra data from the internet. " +
                "An active internet connection is required.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Proceed", color = FloGreen700, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Region picker dialog — shown after the user accepts the warning.
 */
@Composable
private fun RegionPickerDialog(
    regions           : List<Pair<String, String>>,
    onRegionSelected  : (key: String, name: String) -> Unit,
    onDismiss         : () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape  = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Map, null, tint = FloGreen700, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Select Region",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Data currently limited to Nueva Ecija region.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                regions.forEach { (name, key) ->
                    TextButton(
                        onClick  = { onRegionSelected(key, name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Filled.LocationOn, null,
                            tint     = FloGreen700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            name,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.Start,
                            style     = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(icon: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Crop Summary Card ─────────────────────────────────────────────────────────

/**
 * Top-level summary card telling the farmer which crops will prosper and which
 * will struggle across ALL soil profiles in the detected region.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CropSummaryCard(data: LandData) {
    val allSuitable = data.soilProfiles
        .flatMap { it.suitableCrops }
        .distinct()
        .sorted()

    val allPoor = data.soilProfiles
        .flatMap { it.poorCrops }
        .distinct()
        .sorted()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(FloGreen100, FloWhite))
                )
                .padding(18.dp)
        ) {
            // Region info header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Landscape, null, tint = FloGreen700, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        data.regionName,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${data.subRegion}  ·  ${data.elevationRangeM}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text(
                "🌾 Ideal crops",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = FloGreen700
            )
            Spacer(Modifier.height(6.dp))

            FlowRow(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp)
            ) {
                allSuitable.forEach { crop -> CropChip(name = crop, good = true) }
            }

            if (allPoor.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "⚠ Unsuitable crops",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = FloGold700
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    allPoor.forEach { crop -> CropChip(name = crop, good = false) }
                }
            }

            if (data.isGpsDerived) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FloGreen50)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MyLocation, null, tint = FloGreen700, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Based on your GPS location",
                        style = MaterialTheme.typography.labelSmall,
                        color = FloGreen700
                    )
                }
            }
        }
    }
}

@Composable
private fun CropChip(name: String, good: Boolean) {
    val bg   = if (good) FloGreen100 else FloGold100
    val text = if (good) FloGreen700 else FloGold700
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg
    ) {
        Text(
            name,
            style    = MaterialTheme.typography.labelSmall,
            color    = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ── Soil Profile Card ─────────────────────────────────────────────────────────

/**
 * Detailed card for a single [SoilProfile] — matches the ElevatedCard
 * style used in the Weather tab.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SoilProfileCard(profile: SoilProfile, index: Int) {
    val isFirst = index == 0

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFirst) Modifier.border(2.dp, FloGreen500, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        if (isFirst) listOf(FloGreen50, FloWhite) else listOf(FloWhite, FloWhite)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // ── Card header ───────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
                modifier              = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    if (isFirst) {
                        Surface(
                            shape  = RoundedCornerShape(6.dp),
                            color  = FloGreen100,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                "Dominant Soil",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = FloGreen700,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        profile.seriesName,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        profile.texture,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                // Coverage badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FloGreen200
                ) {
                    Text(
                        "${profile.coveragePercent}% area",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = FloGreen700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Soil Composition Bar ──────────────────────────────────────────
            Text(
                "COMPOSITION",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            SoilCompositionBar(
                sand  = profile.sandPercent,
                silt  = profile.siltPercent,
                clay  = profile.clayPercent
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompositionLegendItem(color = Color(0xFFD4A843), label = "Sand ${profile.sandPercent}%")
                CompositionLegendItem(color = Color(0xFF8BC34A), label = "Silt ${profile.siltPercent}%")
                CompositionLegendItem(color = Color(0xFF6D4C41), label = "Clay ${profile.clayPercent}%")
            }

            Spacer(Modifier.height(10.dp))

            // ── pH + Organic Matter ───────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoPill(label = "pH ${profile.phValue}", sublabel = profile.phRating)
                InfoPill(label = "OM", sublabel = profile.organicMatter)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // ── Simple Terms ──────────────────────────────────────────────────
            Text(
                "IN SIMPLE TERMS",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Card(
                colors   = CardDefaults.cardColors(containerColor = FloGreen50),
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(10.dp)) {
                    Text("💬", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        profile.simpleTerms,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }

            if (profile.suitableCrops.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    "GOOD CROPS FOR THIS SOIL",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    profile.suitableCrops.forEach { crop -> CropChip(name = crop, good = true) }
                }
            }

            if (profile.poorCrops.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "CROPS TO AVOID",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = FloGold700.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement   = Arrangement.spacedBy(6.dp)
                ) {
                    profile.poorCrops.forEach { crop -> CropChip(name = crop, good = false) }
                }
            }
        }
    }
}

@Composable
private fun SoilCompositionBar(sand: Int, silt: Int, clay: Int) {
    val total = (sand + silt + clay).toFloat().coerceAtLeast(1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
    ) {
        if (sand > 0) Box(
            Modifier.weight(sand / total).fillMaxHeight()
                .background(Color(0xFFD4A843))
        )
        if (silt > 0) Box(
            Modifier.weight(silt / total).fillMaxHeight()
                .background(Color(0xFF8BC34A))
        )
        if (clay > 0) Box(
            Modifier.weight(clay / total).fillMaxHeight()
                .background(Color(0xFF6D4C41))
        )
    }
}

@Composable
private fun CompositionLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun InfoPill(label: String, sublabel: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ── Fertilizer Guideline Card ─────────────────────────────────────────────────

@Composable
private fun FertilizerGuidelineCard(guideline: FertilizerGuideline) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(FloBlue50, FloWhite)))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header row: crop + season badge
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        guideline.targetCrop,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        guideline.season,
                        style = MaterialTheme.typography.bodySmall,
                        color = FloGreen700
                    )
                }
                Icon(Icons.Filled.Science, null, tint = FloGreen700, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            // Nutrient pills row
            Text(
                "RECOMMENDED AMOUNTS (per hectare)",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NutrientBadge(element = "N", amount = guideline.nitrogenKgPerHa,   color = Color(0xFF1B5E20))
                NutrientBadge(element = "P", amount = guideline.phosphorusKgPerHa, color = Color(0xFF880E4F))
                NutrientBadge(element = "K", amount = guideline.potassiumKgPerHa,  color = Color(0xFF0D47A1))
            }
            guideline.zincKgPerHa?.let { zn ->
                Spacer(Modifier.height(6.dp))
                NutrientBadge(element = "Zn", amount = zn, color = Color(0xFF4A148C))
            }

            Spacer(Modifier.height(12.dp))

            // Application notes
            Text(
                "APPLICATION SCHEDULE",
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Card(
                colors   = CardDefaults.cardColors(containerColor = FloGreen50),
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    guideline.applicationNotes,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Source attribution
            Text(
                "Source: ${guideline.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun NutrientBadge(element: String, amount: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = color
            ) {
                Text(
                    element,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    modifier   = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                amount,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
    }
}

// ── Data Source Banner ────────────────────────────────────────────────────────

@Composable
private fun DataSourceBanner() {
    Card(
        colors   = CardDefaults.cardColors(containerColor = FloBlue50),
        shape    = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, null, tint = FloGreen700, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Fertilizer guidelines are based on BSWM Fertilizer-Guide Maps " +
                "and IRRI/DA/PhilRice recommendations for Nueva Ecija key rice areas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}
