package com.example.diariiviatges.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.diariiviatges.util.formatAsMarkdown
import com.example.diariiviatges.viewmodel.TravelDiaryViewModel

/**
 * PANTALLA 2: postal / crònica visual de la ruta seleccionada.
 * Inclou l'exportació amb SAF (CreateDocument) i el botó opcional de compartir.
 */
// FIX: TopAppBar es API experimental de Material 3. Sin @OptIn el compilador da ERROR
// ("This material API is experimental..."). Se usa @OptIn (y no @ExperimentalMaterial3Api)
// para que la "experimentalidad" no se propague a quien llama a esta función.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostalScreen(
    viewModel: TravelDiaryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val entry = state.selectedEntry

    // Storage Access Framework: el sistema obre el selector i retorna una Uri on escriure.
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let { viewModel.exportSelectedEntry(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Postal / Crònica") },
                navigationIcon = { TextButton(onClick = onNavigateBack) { Text("Enrere") } }
            )
        }
    ) { padding ->
        if (entry == null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Encara no hi ha cap ruta seleccionada.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(entry.date.ifBlank { "Sense data" }, style = MaterialTheme.typography.labelLarge)
                    Text(
                        entry.destination,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(entry.routeName, style = MaterialTheme.typography.titleMedium)

                    // Insígnia segons la dificultat.
                    AssistChip(
                        onClick = {},
                        label = { Text("Dificultat: ${entry.difficulty.label}") }
                    )

                    Text("Pressupost estimat: ${entry.estimatedBudget} €")
                    Text(if (entry.isSolo) "Viatge en solitari" else "Viatge acompanyat")

                    HorizontalDivider()
                    Text("Relat", style = MaterialTheme.typography.titleMedium)
                    Text(entry.notes.ifBlank { "Sense notes." })
                }
            }

            Button(
                onClick = {
                    // FIX: el guion va al final de la clase ("_-") para que sea literal y no
                    // se interprete como un rango "9-_" (ambiguo según el motor de regex).
                    val safeName = entry.routeName.replace(Regex("[^A-Za-z0-9_-]"), "_")
                    createDocumentLauncher.launch("cronica_${safeName}.txt")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exportar Crònica (.md / .txt)")
            }

            // Opcional a l'enunciat: compartir el text amb WhatsApp / correu, etc.
            OutlinedButton(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, formatAsMarkdown(entry))
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartir crònica"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Compartir (opcional)")
            }
        }
    }
}
