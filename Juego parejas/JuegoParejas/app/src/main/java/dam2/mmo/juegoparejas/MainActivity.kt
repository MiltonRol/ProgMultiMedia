package dam2.mmo.juegoparejas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dam2.mmo.juegoparejas.ui.theme.JuegoParejasTheme

// En MVVM la Activity es solo el punto de entrada: no guarda estado ni tiene lógica.
// Todo eso vive en GameViewModel (estado + eventos) y ModelGame (reglas).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dibuja detrás de las barras del sistema, para que el fondo verde ocupe toda
        // la pantalla como en el enunciado. Los márgenes (insets) los gestionará
        // GameScreen en la Fase 3, por ejemplo con Modifier.safeDrawingPadding().
        enableEdgeToEdge()
        setContent {
            // El tema aplica colores y tipografía de Material 3 a todo lo de dentro.
            JuegoParejasTheme {
                // Hemos quitado el Scaffold de la plantilla porque no usamos TopBar
                // ni FAB, y el fondo con imagen lo pondrá la propia pantalla.
                // OJO: no compilará hasta que GameScreen sea una @Composable fun (Fase 3).
                GameScreen()
            }
        }
    }
}
