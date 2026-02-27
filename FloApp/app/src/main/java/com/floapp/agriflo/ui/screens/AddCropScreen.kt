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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropScreen(
    onCropCreated: () -> Unit,
    viewModel: AddCropViewModel = hiltViewModel()
) {
    var cropName         by remember { mutableStateOf("") }
    var variety          by remember { mutableStateOf("") }
    var landArea         by remember { mutableStateOf("") }
    var selectedCropType by remember { mutableStateOf(CropType.RICE) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    // Planting date — kept as LocalDate internally, converted to ISO for the ViewModel
    var plantingDate by remember { mutableStateOf(LocalDate.now()) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Crop", style = MaterialTheme.typography.titleLarge) },
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
            Text("Crop Information", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)

            // Crop type selector
            Text("Crop Type:", style = MaterialTheme.typography.bodyLarge)
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
                    label = { Text("Crop Type") },
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
                label = { Text("Crop Name") },
                placeholder = { Text("e.g. Field Rice") },
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
                label = { Text("Land Area (ha)") },
                placeholder = { Text("e.g. 0.5") },
                suffix = { Text("ha") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            // Planting date field with Material 3 DatePickerDialog
            PlantingDateField(
                date = plantingDate,
                onDateSelected = { plantingDate = it }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.createCrop(
                        name           = cropName.ifBlank { selectedCropType.displayName },
                        variety        = variety.ifBlank { "Standard" },
                        cropType       = selectedCropType,
                        landAreaHa     = landArea.toDoubleOrNull() ?: 0.5,
                        plantingDateIso = plantingDate.toString(), // ISO YYYY-MM-DD
                        onSuccess      = onCropCreated
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
                    Text("Save Crop", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

// ── PlantingDateField ──────────────────────────────────────────────────────────
// Self-contained read-only date picker field. Clicking the calendar icon opens a
// Material 3 DatePickerDialog defaulted to today.

private val DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantingDateField(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value          = date.format(DISPLAY_FORMATTER),
        onValueChange  = {},
        readOnly       = true,
        label          = { Text("Planting Date") },
        leadingIcon    = { Icon(Icons.Filled.DateRange, contentDescription = null) },
        trailingIcon   = {
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector        = Icons.Filled.CalendarMonth,
                    contentDescription = "Pick a date",
                    tint               = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier       = modifier.fillMaxWidth(),
        textStyle      = MaterialTheme.typography.bodyLarge,
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        )
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val selected = Instant
                            .ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selected)
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state  = datePickerState,
                title  = {
                    Text("Select Planting Date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style    = MaterialTheme.typography.labelLarge)
                },
                headline = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val preview = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .format(DISPLAY_FORMATTER)
                        Text(preview,
                            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                            style    = MaterialTheme.typography.headlineMedium)
                    }
                },
                showModeToggle = true
            )
        }
    }
}
