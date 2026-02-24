package com.floapp.agriflo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.floapp.agriflo.domain.model.CropType
import com.floapp.agriflo.ui.viewmodel.AddCropViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropScreen(
    onCropCreated: () -> Unit,
    viewModel: AddCropViewModel = hiltViewModel()
) {
    var cropName by remember { mutableStateOf("") }
    var variety by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var selectedCropType by remember { mutableStateOf(CropType.RICE) }
    var plantingDateText by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bagong Pananim", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize().padding(paddingValues)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Impormasyon ng Pananim", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text("Crop Information", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

            // Crop type selector (large tiles)
            Text("Uri ng Pananim (Crop Type):", style = MaterialTheme.typography.bodyLarge)
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCropType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    label = { Text("Uri ng Pananim") },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                ExposedDropdownMenu(expanded = showTypeDropdown, onDismissRequest = { showTypeDropdown = false }) {
                    CropType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName, style = MaterialTheme.typography.bodyLarge) },
                            onClick = { selectedCropType = type; showTypeDropdown = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cropName,
                onValueChange = { cropName = it },
                label = { Text("Pangalan ng Pananim (Crop Name)") },
                placeholder = { Text("e.g. Palay sa Bukid") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = variety,
                onValueChange = { variety = it },
                label = { Text("Variety") },
                placeholder = { Text("e.g. NSIC Rc222") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = landArea,
                onValueChange = { landArea = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Sukat ng Lupa (Land Area, ha)") },
                placeholder = { Text("e.g. 0.5") },
                suffix = { Text("ha") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = plantingDateText,
                onValueChange = { plantingDateText = it },
                label = { Text("Petsa ng Pagtatanim (Planting Date)") },
                placeholder = { Text("YYYY-MM-DD") },
                leadingIcon = { Icon(Icons.Filled.DateRange, null) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.createCrop(
                        name = cropName.ifBlank { selectedCropType.displayName },
                        variety = variety.ifBlank { "Standard" },
                        cropType = selectedCropType,
                        landAreaHa = landArea.toDoubleOrNull() ?: 0.5,
                        plantingDateIso = plantingDateText,
                        onSuccess = onCropCreated
                    )
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.Agriculture, null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("I-save ang Pananim", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
