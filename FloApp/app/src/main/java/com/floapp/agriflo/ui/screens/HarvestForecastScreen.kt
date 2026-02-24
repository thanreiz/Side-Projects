package com.floapp.agriflo.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.floapp.agriflo.domain.model.RiskLabel
import com.floapp.agriflo.ui.theme.*
import com.floapp.agriflo.ui.viewmodel.HarvestForecastViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestForecastScreen(
    cropId: String,
    onBack: () -> Unit,
    viewModel: HarvestForecastViewModel = hiltViewModel()
) {
    LaunchedEffect(cropId) { viewModel.loadForecast(cropId) }
    val forecast by viewModel.forecast.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val phpFormatter = NumberFormat.getCurrencyInstance(Locale("fil", "PH"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Harvest Forecast", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.regenerateForecast(cropId) }) {
                        Icon(Icons.Filled.Refresh, "Regenerate")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val f = forecast
        if (f == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Walang forecast pa", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { viewModel.regenerateForecast(cropId) }) {
                        Text("Gumawa ng Forecast")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Risk badge
            val (riskColor, riskEmoji) = when (f.riskLabel) {
                RiskLabel.LOW -> FloGreen200 to "🟢"
                RiskLabel.MEDIUM -> FloGold200 to "🟡"
                RiskLabel.HIGH -> FloRed100 to "🔴"
            }
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = riskColor),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("$riskEmoji ${f.riskLabel.displayNameTl}",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(f.riskLabel.displayNameEn, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("${(f.riskScore * 100).toInt()}% risk",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Financial summary
            ForecastMetricCard("🌾 Inaasahang Ani",
                "${f.projectedYieldKg.toInt()} kg",
                "Projected Yield")
            ForecastMetricCard("💰 Inaasahang Kita",
                phpFormatter.format(f.projectedRevenuePhp),
                "Projected Revenue")
            ForecastMetricCard("📦 Gastos sa Input",
                phpFormatter.format(f.projectedCostPhp),
                "Input Cost")
            ForecastMetricCard(
                if (f.netProfitPhp >= 0) "✅ Tubo (Net Profit)" else "⚠️ Pagkalugi (Loss)",
                phpFormatter.format(f.netProfitPhp),
                "Net Profit",
                highlight = true,
                containerColor = if (f.netProfitPhp >= 0) FloGreen200 else FloRed100
            )

            // Assumptions
            ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Batayan ng Forecast", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Text("Forecast Assumptions", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Divider()
                    AssumptionRow("Land Area", "${f.assumptions.landAreaHa} ha")
                    AssumptionRow("Crop Type", f.assumptions.cropType.displayName)
                    AssumptionRow("Baseline Yield", "${f.assumptions.averageYieldKgPerHa} kg/ha")
                    AssumptionRow("Farm Gate Price", phpFormatter.format(f.assumptions.farmGatePricePhpPerKg) + "/kg")
                    AssumptionRow("Weather Risk", "${(f.assumptions.weatherRiskFactor * 100).toInt()}%")
                    AssumptionRow("Fertilizer Compliance", "${(f.assumptions.fertilizerComplianceScore * 100).toInt()}%")
                }
            }
        }
    }
}

@Composable
private fun ForecastMetricCard(
    label: String, value: String, sublabel: String,
    highlight: Boolean = false,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (highlight) 4.dp else 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(sublabel, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AssumptionRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
