package dam2.mmo.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dam2.mmo.tictactoe.ui.theme.TicTacToeTheme

// En MVVM la Activity solo es el punto de entrada: sin estado ni lógica.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dibuja detrás de las barras del sistema para que el fondo ocupe
        // toda la pantalla. Los márgenes los pone safeDrawingPadding() en GameContent.
        enableEdgeToEdge()
        setContent {
            // Sin Scaffold: no hay TopBar ni FAB y el fondo lo pone la pantalla.
            TicTacToeTheme {
                GameScreen()   // viewModel() crea el GameViewModel por dentro
            }
        }
    }
}
