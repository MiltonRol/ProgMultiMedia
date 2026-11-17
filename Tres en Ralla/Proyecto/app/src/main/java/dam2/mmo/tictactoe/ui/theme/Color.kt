package dam2.mmo.tictactoe.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ---------------------------------------------------------------------
//  Colores de la rejilla del tablero (ver BoardGrid en GameScreen.kt).
//  Los tres comparten el MISMO alfa, para que al pasar de gris a verde/rojo
//  solo cambie el tono y se mantenga la translucidez.
//  copy(alpha = ...) crea el mismo color con otra opacidad (0 = invisible, 1 = opaco).
// ---------------------------------------------------------------------
private const val GRID_ALPHA = 0.45f

val GridNeutral = Color(0xFFE0E0E0).copy(alpha = GRID_ALPHA)  // blanco/gris: jugando o empate
val GridWin = Color(0xFF4CAF50).copy(alpha = GRID_ALPHA)      // verde: ganas
val GridLose = Color(0xFFF44336).copy(alpha = GRID_ALPHA)     // rojo: pierdes
