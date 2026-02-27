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
import com.floapp.agriflo.ui.viewmodel.DigitalResiboViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalResiboScreen(viewModel: DigitalResiboViewModel = hiltViewModel()) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Resibo", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startOcrCapture() },
                icon = { Icon(Icons.Filled.CameraAlt, null) },
                text = { Text("Scan Receipt", style = MaterialTheme.typography.bodyLarge) }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Photograph your fertilizer receipt",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Generate a PDF for insurance or DA verification",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            if (isScanning) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Reading your receipt…", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            if (receipts.isEmpty() && !isScanning) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧾", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("No receipts yet", style = MaterialTheme.typography.titleMedium)
                        Text("Tap \"Scan Receipt\" to photograph your first fertilizer receipt.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                receipts.forEach { receipt ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(receipt.productName, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                if (receipt.validated) {
                                    Icon(Icons.Filled.Verified, "DA Approved",
                                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Text(receipt.purchaseDate, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("PHP ${String.format("%.2f", receipt.costPhp)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (receipt.pdfExportUri != null) {
                                OutlinedButton(onClick = { viewModel.openPdf(receipt.id) },
                                    modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Filled.PictureAsPdf, null)
                                    Spacer(Modifier.width(8.dp))
                                     Text("Open PDF")
                                }
                            } else {
                                Button(onClick = { viewModel.generatePdf(receipt.id) },
                                    modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Filled.Download, null)
                                    Spacer(Modifier.width(8.dp))
                                     Text("Generate PDF")
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(80.dp)) // FAB clearance
        }
    }
}
