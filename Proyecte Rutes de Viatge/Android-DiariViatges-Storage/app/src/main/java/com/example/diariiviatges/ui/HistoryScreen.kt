package com.example.diariiviatges.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diariiviatges.model.RouteEntry
import com.example.diariiviatges.viewmodel.TravelDiaryViewModel

/** Pantalla d'historial: llista de rutes desades a filesDir. En prémer una, obre la postal. */
// FIX: @OptIn en vez de @ExperimentalMaterial3Api: la anotación marcadora obligaba a
// AppNavigation y MainActivity a "contagiarse" también; @OptIn lo acepta aquí y se acabó.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: TravelDiaryViewModel,
    onEntrySelected: (RouteEntry) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de rutes") },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("Enrere") } }
            )
        }
    ) { padding ->
        if (state.history.isEmpty()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Encara no hi ha rutes desades.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.history) { entry ->
                Card(
                    onClick = { onEntrySelected(entry) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.routeName, style = MaterialTheme.typography.titleMedium)
                        Text("${entry.destination} · ${entry.date}")
                        Text(
                            "Dificultat: ${entry.difficulty.label}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
