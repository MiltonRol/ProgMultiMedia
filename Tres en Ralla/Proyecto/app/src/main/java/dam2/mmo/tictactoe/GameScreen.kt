package dam2.mmo.tictactoe

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam2.mmo.tictactoe.ui.theme.GridLose
import dam2.mmo.tictactoe.ui.theme.GridNeutral
import dam2.mmo.tictactoe.ui.theme.GridWin
import dam2.mmo.tictactoe.ui.theme.TicTacToeTheme

// =====================================================================
//  VIEW · Pinta el estado y avisa de los clics. NO tiene reglas del juego.
//
//  Igual que en JuegoParejas, se separa en niveles (state hoisting):
//   GameScreen    -> CON estado: el único que conoce el ViewModel.
//   GameContent   -> SIN estado: recibe datos + lambdas y pinta la pantalla.
//   BoardGrid     -> la rejilla de fondo + las 9 casillas.
//   CellItemView  -> una casilla: cruz, círculo o vacía.
//  Ventaja: las @Preview usan GameContent con tableros inventados, sin ViewModel.
// =====================================================================

/** Tamaño del tablero. Cada casilla mide un tercio: 100 dp. */
private val BOARD_SIZE = 300.dp

/** Fondo oscuro para que la rejilla translúcida se vea bien encima. */
private val BACKGROUND = Brush.verticalGradient(listOf(Color(0xFF1B2735), Color(0xFF090A0F)))

/**
 * Pantalla principal.
 *
 * viewModel() como valor por defecto del PARÁMETRO: crea el GameViewModel
 * la primera vez y luego devuelve SIEMPRE el mismo (incluso al girar la
 * pantalla). GameViewModel() a mano crearía uno nuevo en cada recomposición.
 */
@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    // collectAsState(): convierte el StateFlow en un State de Compose.
    // Cada vez que el ViewModel emite un BoardModel nuevo, "board" cambia
    // y Compose vuelve a llamar a esta función (recomposición).
    // by: para escribir "board" en vez de "board.value" (necesita import getValue).
    val board by vm.board.collectAsState()

    GameContent(
        board = board,                  // el estado BAJA
        onCellClick = vm::onCellClick,  // los eventos SUBEN (referencia a función)
        onRestart = vm::restartGame,
    )
}

/** Toda la pantalla, sin estado propio: solo lo que recibe por parámetro. */
@Composable
fun GameContent(
    board: BoardModel,
    onCellClick: (Int) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,   // el último: es el único con valor por defecto
) {
    Column(
        // El fondo va ANTES que safeDrawingPadding() para que llegue detrás de las
        // barras del sistema (MainActivity activa enableEdgeToEdge()); el contenido
        // sí queda dentro del margen seguro.
        modifier = modifier
            .fillMaxSize()
            .background(BACKGROUND)
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Tres en Ratlla", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Indicador de turno / mensaje de victoria o empate.
        Text(
            text = statusMessage(board),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 20.sp,
        )
        Spacer(Modifier.height(32.dp))

        BoardGrid(cells = board.cells, result = board.result, onCellClick = onCellClick)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onRestart) { Text("Reiniciar") }
    }
}

/**
 * Texto de estado. Es PRESENTACIÓN (qué frase mostrar), por eso está en la View;
 * la decisión de quién ha ganado ya la tomó el Model en board.result.
 * El when sobre un enum obliga a cubrir todos los casos: si añadieras uno nuevo
 * a GameResult, esto no compilaría hasta tratarlo.
 */
private fun statusMessage(board: BoardModel): String = when (board.result) {
    GameResult.HUMAN_WINS -> "¡Has ganado! 🎉"
    GameResult.CPU_WINS -> "Has perdido 😢"
    GameResult.DRAW -> "Empate"
    GameResult.PLAYING ->
        if (board.isHumanTurn) "Tu turno (X)" else "La CPU está pensando… (O)"
}

/**
 * Tablero: la rejilla de fondo y, ENCIMA, las 9 casillas.
 */
@Composable
fun BoardGrid(
    cells: List<CellModel>,
    result: GameResult,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Color objetivo según el resultado. Los tres conservan la translucidez
    // (ver Color.kt): solo cambia el tono.
    val targetColor = when (result) {
        GameResult.HUMAN_WINS -> GridWin
        GameResult.CPU_WINS -> GridLose
        GameResult.DRAW, GameResult.PLAYING -> GridNeutral
    }

    // animateColorAsState: en vez de saltar de golpe al color nuevo, Compose
    // va pasando por los colores intermedios durante 600 ms (tween = transición
    // lineal con duración). Devuelve un State, por eso se lee con "by".
    val gridColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 600),
        label = "gridColor",   // nombre que sale en el inspector de animaciones
    )

    // Box apila sus hijos: el primero (rejilla) DEBAJO, el segundo (casillas) ENCIMA.
    Box(modifier = modifier.size(BOARD_SIZE)) {
        Image(
            painter = painterResource(R.drawable.ic_grid),
            contentDescription = null,   // decorativa: el lector de pantalla la ignora
            modifier = Modifier.fillMaxSize(),
            // ColorFilter.tint(): sustituye el color del dibujo (blanco) por gridColor,
            // INCLUIDO su alfa. Así un único drawable sirve para gris, verde y rojo.
            colorFilter = ColorFilter.tint(gridColor),
        )

        // LazyVerticalGrid: rejilla que coloca los elementos en columnas.
        // GridCells.Fixed(3) -> siempre 3 columnas; con 9 casillas salen 3 filas.
        // Calcula el índice por nosotros (no hace falta fila * 3 + columna como en JuegoParejas).
        LazyVerticalGrid(
            columns = GridCells.Fixed(BoardModel.SIZE),
            modifier = Modifier.fillMaxSize(),
            // Es un tablero fijo: no tiene sentido que se pueda desplazar con el dedo.
            userScrollEnabled = false,
        ) {
            // items(lista, key): una celda por elemento. key da a cada casilla una
            // identidad estable (su id), para que Compose no confunda unas con otras.
            items(cells, key = { it.id }) { cell ->
                CellItemView(cell = cell, onClick = { onCellClick(cell.position) })
            }
        }
    }
}

/**
 * Una casilla. Las ocupadas NO se bloquean aquí: makeMove() ya las ignora.
 * La View no decide reglas.
 */
@Composable
fun CellItemView(cell: CellModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Qué dibujo toca. null = casilla vacía, no se pinta nada.
    val icon = when (cell.symbol) {
        Symbol.X -> R.drawable.ic_cross
        Symbol.O -> R.drawable.ic_circle
        Symbol.EMPTY -> null
    }

    Box(
        modifier = modifier
            // aspectRatio(1f): alto = ancho -> casillas cuadradas de 100 dp,
            // que encajan justo con las líneas de la rejilla (en 100 y 200).
            .aspectRatio(1f)
            .clickable(onClick = onClick)   // primero clickable: toda la casilla es pulsable...
            .padding(20.dp),                // ...y luego el margen, que solo encoge el dibujo
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(icon),
                contentDescription = "Casilla ${cell.position}: ${cell.symbol}",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// =====================================================================
//  PREVIEWS · Tableros fijos construidos con makeMove(), sin ViewModel.
// =====================================================================

/** Aplica una lista de jugadas alternando X y O, empezando por X. */
private fun boardAfter(vararg moves: Int): BoardModel =
    // fold: empieza con el tablero inicial y le aplica cada jugada al resultado anterior.
    moves.fold(BoardModel.createInitialBoard()) { board, position -> board.makeMove(position) }

@Preview(showBackground = true)
@Composable
private fun GamePlayingPreview() {
    TicTacToeTheme {
        GameContent(board = boardAfter(4, 0, 8), onCellClick = {}, onRestart = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun GameWinPreview() {
    // X: 0, 1, 2 (primera fila) -> ganas, rejilla verde.
    TicTacToeTheme {
        GameContent(board = boardAfter(0, 3, 1, 4, 2), onCellClick = {}, onRestart = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun GameLosePreview() {
    // O: 3, 4, 5 (fila del medio) -> pierdes, rejilla roja.
    TicTacToeTheme {
        GameContent(board = boardAfter(0, 3, 1, 4, 8, 5), onCellClick = {}, onRestart = {})
    }
}
