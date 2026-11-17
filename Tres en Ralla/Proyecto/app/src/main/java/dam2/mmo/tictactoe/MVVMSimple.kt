package com.example.mvvm_esquema

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

// --- 1. MODEL ---
// Estructura de datos simple que representa el estado de la pantalla
data class SimpleState(
    val counter: Int = 0,
    val message: String = "¡Hola Compose!"
)

// --- 2. VIEWMODEL ---
class SimpleViewModel : ViewModel() {
    // Estado privado modificable (sin StateFlow ni corrutinas)
    private val _state = mutableStateOf(SimpleState())
    
    // Estado público de solo lectura expuesto a la vista como State<SimpleState>
    val state: State<SimpleState> = _state

    // Eventos/Acciones lanzados desde la interfaz de usuario
    fun incrementCounter() {
        val count = _state.value.counter + 1
        _state.value = _state.value.copy(
            counter = count,
            message = "Has pulsado el botón $count veces"
        )
    }

    fun reset() {
        _state.value = SimpleState()
    }
}

// --- 3. VIEW (ACTIVITY & COMPOSABLE) ---
class MainActivity : ComponentActivity() {
    private val viewModel: SimpleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                SimpleScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun SimpleScreen(
    viewModel: SimpleViewModel,
    modifier: Modifier = Modifier
) {
    // Lectura directa del estado (.value) sin necesidad de collectAsState()
    val state = viewModel.state.value

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.message,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.incrementCounter() }) {
            Text(text = "Incrementar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.reset() }) {
            Text(text = "Reiniciar")
        }
    }
}