package com.floapp.agriflo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.floapp.agriflo.domain.model.LogType
import com.floapp.agriflo.ui.viewmodel.LoggingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    cropId: String,
    onBack: () -> Unit,
    viewModel: LoggingViewModel = hiltViewModel()
) {
    var selectedLogType by remember { mutableStateOf<LogType?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Activity", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("What did you do today?", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Select the activity you performed.", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(24.dp))

            // 2-column icon grid — large tap targets (at least 80dp per tile)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(LogType.values()) { logType ->
                    LogTypeCard(
                        logType = logType,
                        isSelected = selectedLogType == logType,
                        onClick = { selectedLogType = logType }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    selectedLogType?.let { logType ->
                        viewModel.logActivity(cropId, logType)
                        showConfirmation = true
                    }
                },
                enabled = selectedLogType != null,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Check, null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("Save Log", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            icon = { Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)) },
            title = {
                Text("Logged!", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(selectedLogType?.displayNameEn ?: "", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Your log has been saved and will sync when connected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    selectedLogType = null
                    onBack()
                }) { Text("Done", style = MaterialTheme.typography.bodyLarge) }
            }
        )
    }
}

@Composable
private fun LogTypeCard(logType: LogType, isSelected: Boolean, onClick: () -> Unit) {
    val icon = getLogTypeIcon(logType)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = logType.displayNameEn,
                modifier = Modifier.size(36.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(6.dp))
            Text(logType.displayNameEn, style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface)
        }
    }
}

private fun getLogTypeIcon(logType: LogType): ImageVector = when (logType) {
    LogType.FERTILIZER -> Icons.Filled.Grass
    LogType.PEST -> Icons.Filled.BugReport
    LogType.RAINFALL -> Icons.Filled.Water
    LogType.IRRIGATION -> Icons.Filled.Opacity
    LogType.HARVEST -> Icons.Filled.Agriculture
    LogType.PESTICIDE -> Icons.Filled.Science
    LogType.WEEDING -> Icons.Filled.ContentCut
    LogType.OTHER -> Icons.Filled.MoreHoriz
}
