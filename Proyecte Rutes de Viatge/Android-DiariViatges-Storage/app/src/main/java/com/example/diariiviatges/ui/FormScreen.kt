package com.example.diariiviatges.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diariiviatges.model.Difficulty
import com.example.diariiviatges.viewmodel.TravelDiaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * PANTALLA 1: formulari de nova ruta amb validació visual en temps real.
 * Components: OutlinedTextField, FilterChip, DatePickerDialog, Switch i botons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: TravelDiaryViewModel,
    onNavigateToPostal: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // DatePickerDialog de Material 3: el valor és un millis -> el formatem a String.
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // FIX: el DatePicker devuelve millis a medianoche UTC. Si formateamos con la
                        // zona local, en husos negativos (p. ej. América) sale el día anterior.
                        // Por eso el formateador se fuerza a UTC.
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        viewModel.onDateChanged(formatter.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel·la") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nova ruta") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // --- Nom de la ruta (obligatori) ---
            OutlinedTextField(
                value = state.routeName,
                onValueChange = viewModel::onRouteNameChanged,
                label = { Text("Nom de la ruta") },
                isError = state.routeNameError != null,
                supportingText = {
                    state.routeNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Destí (obligatori) ---
            OutlinedTextField(
                value = state.destination,
                onValueChange = viewModel::onDestinationChanged,
                label = { Text("Destí") },
                isError = state.destinationError != null,
                supportingText = {
                    state.destinationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Dificultat: selector d'opció única amb FilterChips ---
            Text("Nivell de dificultat", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { level ->
                    FilterChip(
                        selected = state.difficulty == level,
                        onClick = { viewModel.onDifficultySelected(level) },
                        label = { Text(level.label) }
                    )
                }
            }

            // --- Pressupost estimat (numèric, KeyboardType.Decimal) ---
            OutlinedTextField(
                value = state.estimatedBudget,
                onValueChange = viewModel::onBudgetChanged,
                label = { Text("Pressupost estimat (€)") },
                isError = state.budgetError != null,
                supportingText = {
                    state.budgetError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // --- Data: camp de lectura que obre el DatePickerDialog ---
            OutlinedTextField(
                value = state.date,
                onValueChange = {},
                label = { Text("Data de la ruta") },
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("Tria") }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // --- Viatge en solitari: Switch amb etiqueta ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = state.isSolo, onCheckedChange = viewModel::onSoloChanged)
                Spacer(Modifier.width(8.dp))
                Text("Ruta feta en solitari")
            }

            // --- Notes del relat (multilínia) ---
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChanged,
                label = { Text("Notes de l'experiència") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // --- Acció principal: desactivada si el formulari no és vàlid ---
            Button(
                onClick = {
                    viewModel.onCreatePostal()
                    onNavigateToPostal()
                },
                enabled = state.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Generar Postal / Crònica")
            }

            // --- Accés a l'historial personal ---
            TextButton(onClick = onNavigateToHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Consultar historial de rutes (${state.history.size})")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
