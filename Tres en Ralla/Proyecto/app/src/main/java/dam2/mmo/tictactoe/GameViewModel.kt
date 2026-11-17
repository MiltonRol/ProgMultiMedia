package dam2.mmo.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =====================================================================
//  VIEWMODEL · El "cable" entre la View y el Model.
//   - Guarda el estado que observa la pantalla (el BoardModel actual).
//   - Recibe los eventos de la View: onCellClick y restartGame.
//   - Organiza los turnos: después de tu jugada, lanza la de la CPU.
//   - NO tiene reglas del juego (casilla ocupada, 3 en raya...): las delega en BoardModel.
//   - NO conoce @Composable, Modifier ni remember.
//
//  DIFERENCIA CON JUEGOPAREJAS: allí se usó mutableStateOf. Aquí el enunciado
//  pide la opción B: MutableStateFlow privado + StateFlow público.
//  StateFlow es de Kotlin (kotlinx.coroutines), no de Compose, así que el
//  ViewModel ya no importa NADA de androidx.compose.
// =====================================================================
class GameViewModel : ViewModel() {

    /**
     * Estado privado y MODIFICABLE. El guion bajo es la convención para
     * "versión mutable interna de board".
     * MutableStateFlow siempre tiene un valor (.value) y avisa a quien lo
     * escuche cuando se le asigna uno DISTINTO (compara con equals()).
     */
    private val _board = MutableStateFlow(BoardModel.createInitialBoard())

    /**
     * Estado público de SOLO LECTURA.
     * asStateFlow() envuelve _board para que la View no pueda hacer un cast
     * a MutableStateFlow y cambiarlo: el ViewModel es el único dueño del estado.
     */
    val board: StateFlow<BoardModel> = _board.asStateFlow()

    /**
     * La corrutina del turno de la CPU, guardada para poder CANCELARLA.
     * Trampa que evita: si pulsas "Reiniciar" mientras la CPU "piensa",
     * sin cancelar, la CPU jugaría en el tablero nuevo.
     */
    private var cpuJob: Job? = null

    /**
     * El usuario ha pulsado la casilla [position] (0..8).
     */
    fun onCellClick(position: Int) {
        val current = _board.value

        // Si es el turno de la CPU (o la partida acabó), se ignora el clic.
        // Sin esto, el humano podría colocar la O de la CPU pulsando rápido,
        // porque makeMove() pone siempre el símbolo de currentTurn.
        if (!current.isHumanTurn) return

        val afterHuman = current.makeMove(position)

        // === compara REFERENCIAS (¿es el mismo objeto?), no contenido.
        // makeMove() devuelve "this" si la jugada no es válida (casilla ocupada),
        // así que en ese caso no hay nada que hacer y la CPU no debe jugar.
        if (afterHuman === current) return

        // Asignar un valor nuevo a .value hace que StateFlow emita y la View se repinte.
        _board.value = afterHuman

        if (!afterHuman.isGameOver) playCpuTurn()
    }

    /**
     * Turno de la CPU con un pequeño retraso para que se vea "pensar".
     *
     * viewModelScope: ámbito de corrutinas atado a la vida del ViewModel.
     * Se cancela solo cuando el ViewModel se destruye (al cerrar la pantalla),
     * así no quedan corrutinas sueltas.
     * launch: arranca la corrutina sin bloquear el hilo principal;
     * delay() suspende sin congelar la interfaz (Thread.sleep sí la congelaría).
     */
    private fun playCpuTurn() {
        cpuJob = viewModelScope.launch {
            delay(CPU_DELAY_MS)
            // Se vuelve a leer _board.value DESPUÉS del delay: es el estado actual,
            // no el de hace medio segundo.
            val board = _board.value
            // return@launch: sale de la lambda de launch (no de playCpuTurn).
            val position = board.bestMoveFor(BoardModel.CPU) ?: return@launch
            _board.value = board.makeMove(position)
        }
    }

    /**
     * Nueva partida. Como el modelo es inmutable, no hay que "limpiar"
     * casillas: se sustituye por uno recién creado.
     */
    fun restartGame() {
        cpuJob?.cancel()   // ?. -> solo si hay un turno de CPU pendiente
        _board.value = BoardModel.createInitialBoard()
    }

    private companion object {
        const val CPU_DELAY_MS = 600L   // L: delay() recibe un Long
    }
}
