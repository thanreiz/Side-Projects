package com.floapp.agriflo.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.floapp.agriflo.domain.model.*
import com.floapp.agriflo.ui.theme.*
import com.floapp.agriflo.ui.viewmodel.CropWheelViewModel
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropWheelScreen(
    cropId: String,
    onLogActivity: () -> Unit,
    onViewForecast: () -> Unit,
    viewModel: CropWheelViewModel = hiltViewModel()
) {
    LaunchedEffect(cropId) { viewModel.loadCrop(cropId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.cropName, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Crop Wheel
            state.cropStage?.let { stage ->
                CropWheelCanvas(stage = stage, modifier = Modifier.size(280.dp))
                Spacer(Modifier.height(16.dp))

                // Stage name
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stage.stageType.displayNameTl, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(stage.stageType.displayNameEn, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("${stage.daysRemainingInStage} araw na natitira",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("${stage.daysRemainingInStage} days remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Alerts
                stage.alerts.forEach { alert ->
                    AlertChip(alert = alert)
                    Spacer(Modifier.height(8.dp))
                }

                // Recommended actions
                Text("Mga Inirerekomendang Gawain:", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))
                stage.recommendedActions.forEach { action ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("•", color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(action, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action buttons
            Button(
                onClick = onLogActivity,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.EditNote, null)
                Spacer(Modifier.width(8.dp))
                Text("I-log ang Gawain", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onViewForecast,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Analytics, null)
                Spacer(Modifier.width(8.dp))
                Text("Harvest Forecast", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun CropWheelCanvas(stage: CropStage, modifier: Modifier = Modifier) {
    val stages = CropStageType.values()
    val totalStages = stages.size
    val sweepAngle = 360f / totalStages

    val colors = listOf(
        FloEarth500, FloGreen200, FloGreen500, FloGold500,
        FloGreen700, FloGold200, FloRed500
    )

    Canvas(modifier = modifier) {
        val diameter = min(size.width, size.height)
        val radius = diameter / 2f
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = radius * 0.28f

        stages.forEachIndexed { index, stageType ->
            val startAngle = -90f + sweepAngle * index
            val isCurrentStage = stageType == stage.stageType
            val alpha = if (isCurrentStage) 1f else 0.35f
            val color = colors.getOrElse(index) { FloGreen500 }

            drawArc(
                color = color.copy(alpha = alpha),
                startAngle = startAngle + 2f,
                sweepAngle = sweepAngle - 4f,
                useCenter = false,
                topLeft = Offset(center.x - radius + strokeWidth / 2, center.y - radius + strokeWidth / 2),
                size = Size(diameter - strokeWidth, diameter - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress fill for current stage
            if (isCurrentStage) {
                drawArc(
                    color = color,
                    startAngle = startAngle + 2f,
                    sweepAngle = (sweepAngle - 4f) * stage.progressFraction,
                    useCenter = false,
                    topLeft = Offset(center.x - radius + strokeWidth / 2, center.y - radius + strokeWidth / 2),
                    size = Size(diameter - strokeWidth, diameter - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center circle
        drawCircle(
            color = Color.White,
            radius = radius * 0.42f,
            center = center
        )
    }
}

@Composable
private fun AlertChip(alert: CropAlert) {
    val (bgColor, textColor) = when (alert.urgency) {
        AlertUrgency.HIGH -> FloRed100 to FloRed500
        AlertUrgency.MEDIUM -> FloGold200 to FloEarth700
        AlertUrgency.LOW -> FloGreen50 to FloGreen700
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = textColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = textColor)
        }
    }
}
