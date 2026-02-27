package com.floapp.agriflo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.floapp.agriflo.ui.viewmodel.HomeViewModel
import com.floapp.agriflo.domain.model.Crop
import com.floapp.agriflo.ui.theme.AppStrings
import com.floapp.agriflo.ui.theme.LocalLanguage
import com.floapp.agriflo.ui.theme.strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCropSelected: (String) -> Unit,
    onAddCrop: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val crops by viewModel.crops.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Single source of truth – every string comes from here
    val language = LocalLanguage.current
    val str = language.strings()

    // no inline dropdown state needed — Settings is a separate screen

    Scaffold(
        topBar = {
            TopAppBar(
                // 🌾 rice-field emoji — upper left
                navigationIcon = {
                    Text(
                        text = "🌾",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ani.PH",          // App name – never translated
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                // ⚙️ settings icon — navigates to Settings screen
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = str.settingsContentDesc
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddCrop,
                icon = { Icon(Icons.Filled.Add, str.fabAddCrop) },
                text = { Text(str.fabAddCrop, style = MaterialTheme.typography.bodyLarge) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            crops.isEmpty() -> {
                EmptyCropsPlaceholder(
                    modifier = Modifier.padding(paddingValues),
                    onAddCrop = onAddCrop,
                    str = str
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(crops) { crop ->
                        CropCard(crop = crop, str = str, onClick = { onCropSelected(crop.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyCropsPlaceholder(
    modifier: Modifier = Modifier,
    onAddCrop: () -> Unit,
    str: AppStrings
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🌱", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            str.emptyTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            str.emptySubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddCrop,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(str.emptyButton, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ─── Crop card ────────────────────────────────────────────────────────────────

@Composable
private fun CropCard(crop: Crop, str: AppStrings, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    crop.cropType.displayName.take(1),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    crop.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    crop.variety,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    "${crop.landAreaHa}${str.cropCardAreaSuffix}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = str.cropCardContentDesc,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
