package dam2.mmo.juegoparejas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// =====================================================================
//  VIEW · Solo PINTA el estado y AVISA de los eventos. Cero lógica de juego.
//
//  DECISIÓN DE DISEÑO: partimos la pantalla en dos niveles (state hoisting).
//   1) GameScreen    -> "con estado": es la única que conoce el ViewModel.
//   2) TableroJuego  -> "sin estado": recibe datos + lambdas por parámetro.
//  ¿Por qué? - La @Preview no puede crear un ViewModel real, pero sí puede
//              pasarle a TableroJuego una lista de cartas inventada.
//            - TableroJuego no depende de nada: se puede reutilizar y testear.
// =====================================================================

// Cartas por fila. Constante con nombre en vez de un "3" suelto repetido:
// chunked() y el cálculo del índice DEBEN usar el mismo número. Si algún día
// quieres 4 columnas, solo cambias esta línea y no se desincronizan.
private const val COLUMNAS = 3

// Naranja del título. Constante para no tener el número mágico dentro del Text.
private val NARANJA = Color(0xFFFFA500)

/**
 * Pantalla del juego CONECTADA al ViewModel. Es la que llama MainActivity.
 *
 * viewModel() crea el GameViewModel la primera vez y luego devuelve SIEMPRE el
 * mismo, incluso si giras la pantalla. Va como valor por defecto del parámetro
 * para que MainActivity pueda llamar a GameScreen() sin argumentos.
 * Si pusieras GameViewModel() a mano, se crearía uno nuevo en cada recomposición
 * y perderías la partida.
 */
@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    // GameScreen solo hace de "cable": el estado BAJA y los eventos SUBEN.
    //
    // ¿Por qué se redibuja sola? Al leer vm.model aquí, Compose apunta que
    // GameScreen depende de ese mutableStateOf. Cuando el ViewModel asigna un
    // model nuevo, Compose vuelve a llamar a GameScreen, que vuelve a leer
    // vm.model.cartas y vm.model.juegoTerminado() con los valores nuevos.
    TableroJuego(
        cartas = vm.model.cartas,
        // Dato DERIVADO: se pregunta al modelo en cada recomposición, no se guarda.
        terminado = vm.model.juegoTerminado(),
        // Lambda con parámetro: TableroJuego nos da el índice y lo pasamos al vm.
        onCardClick = { index -> vm.onCardClick(index) },
        // Referencias de función (::): equivalen a { vm.onValidate() }, pero más cortas.
        onValidate = vm::onValidate,
        onNuevaPartida = vm::onNuevaPartida,
    )
}

/**
 * Pantalla SIN estado: pinta lo que recibe y avisa con las lambdas.
 *
 * @param cartas las 6 cartas en orden de tablero (el índice 0..5 es la posición)
 * @param terminado true si la partida ha acabado, para mostrar el mensaje de victoria
 * @param onCardClick se llama con el índice de la carta pulsada
 * @param onValidate se llama al pulsar VALIDATE
 * @param onNuevaPartida se llama al pulsar "Nueva partida" en el mensaje de victoria
 * @param modifier convención de Compose: todo composable "público" acepta un
 *                 Modifier para que quien lo llama pueda ajustarlo desde fuera
 */
@Composable
fun TableroJuego(
    cartas: List<Carta>,
    terminado: Boolean,
    onCardClick: (Int) -> Unit,
    onValidate: () -> Unit,
    onNuevaPartida: () -> Unit,
    // modifier va el ÚLTIMO por convención: es el único con valor por defecto.
    modifier: Modifier = Modifier,
) {
    // Box = apila sus hijos uno encima del otro: el fondo DEBAJO (primer hijo)
    // y la columna con el juego ENCIMA (segundo hijo). El orden importa.
    Box(modifier = modifier.fillMaxSize()) {

        // ---- 3.1 Fondo ----
        Image(
            painter = painterResource(R.drawable.fondo1),
            // null porque es decorativa: el lector de pantalla no debe leerla.
            contentDescription = null,
            // Crop: llena toda la pantalla recortando lo que sobre, sin deformar.
            // (FillBounds la estiraría y se vería deformada en móviles alargados.)
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            // safeDrawingPadding(): deja margen para la barra de estado y la de
            // navegación, porque MainActivity activa enableEdgeToEdge().
            // El fondo NO lo lleva (está fuera de la Column): así sí llega hasta
            // detrás de las barras, que es lo que queremos.
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Centra el bloque título + cartas + botón en vertical.
            verticalArrangement = Arrangement.Center,
        ) {

            // ---- 3.2 Título ----
            Text(
                text = "Joc de les Parelles",
                color = NARANJA,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(24.dp))

            // ---- 3.3 Las 2 filas de 3 cartas ----
            // chunked(COLUMNAS): [c0..c5] -> [[c0,c1,c2], [c3,c4,c5]]
            cartas.chunked(COLUMNAS).forEachIndexed { fila, cartasDeLaFila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    cartasDeLaFila.forEachIndexed { columna, carta ->
                        // Índice REAL en la lista: filas completas anteriores + columna.
                        // Fila 0 -> 0,1,2 · Fila 1 -> 3,4,5.
                        // Trampa evitada: "columna" sola va de 0 a 2 en CADA fila, así
                        // que la fila de abajo giraría las cartas de arriba.
                        val index = fila * COLUMNAS + columna
                        CartaView(carta = carta, onClick = { onCardClick(index) })
                    }
                }
                // Hueco entre filas. Se pone después de cada fila (también de la
                // última), y hace de separación con el botón.
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(8.dp))

            // ---- 3.4 Botón VALIDATE ----
            // Pasamos la lambda tal cual: TableroJuego no sabe qué hace validar.
            // No lo desactivamos con "enabled = hay 2 giradas" porque eso sería una
            // regla del juego en la View; el Model ya ignora validar con 0 o 1.
            Button(onClick = onValidate) {
                Text("VALIDATE")
            }
        }

        // ---- 3.5 Mensaje de victoria ----
        // El diálogo existe SOLO mientras terminado es true. No hace falta una
        // variable "mostrarDialogo": al pulsar "Nueva partida", el ViewModel crea
        // un modelo nuevo, juegoTerminado() pasa a false y el diálogo desaparece solo.
        if (terminado) {
            AlertDialog(
                // Se llama al tocar fuera o pulsar atrás. Vacío = no se cierra:
                // queremos obligar a elegir "Nueva partida".
                onDismissRequest = {},
                title = { Text("¡Enhorabona!") },
                text = { Text("Has trobat totes les parelles.") },
                confirmButton = {
                    TextButton(onClick = onNuevaPartida) {
                        Text("Nueva partida")
                    }
                },
            )
        }
    }
}

/**
 * Una sola carta. Muestra back.png o su cara según el estado.
 *
 * Va en su propio composable para que TableroJuego se lea mejor y para no
 * repetir el mismo Image + clickable 6 veces.
 */
@Composable
fun CartaView(
    carta: Carta,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ---- 3.6 Qué imagen se ve ----
    // validar() marca emparejada = true pero deja bocaArriba = true, así que con
    // bocaArriba bastaría. Añadimos "|| emparejada" por seguridad: si algún día el
    // Model cambiara, una carta acertada nunca se vería tapada.
    val imagen = if (carta.bocaArriba || carta.emparejada) carta.imagen else R.drawable.back

    Image(
        painter = painterResource(imagen),
        // Texto distinto según el estado: útil para accesibilidad y para tests de UI.
        contentDescription = if (carta.bocaArriba) "Carta girada" else "Carta tapada",
        contentScale = ContentScale.Crop,
        modifier = modifier
            // Orden de modifiers: primero el tamaño y luego clickable, para que la
            // zona pulsable sea exactamente la carta.
            .size(width = 100.dp, height = 150.dp)
            // No bloqueamos aquí las cartas emparejadas: el Model ya las ignora en
            // girarCarta(). La View no decide reglas.
            .clickable { onClick() },
    )
}

// =====================================================================
//  PREVIEWS · Usan TableroJuego (sin estado) con datos INVENTADOS.
//  No usamos ModelGame.nuevaPartida(): baraja al azar y la preview saldría
//  distinta cada vez. Con una lista fija controlas exactamente qué se ve.
// =====================================================================

// Lista compartida por las dos previews: tapadas, giradas y emparejadas a la vez.
private val cartasDeEjemplo = listOf(
    Carta(R.drawable.chiquito),                    // tapada
    Carta(R.drawable.chiquito, bocaArriba = true), // girada
    Carta(R.drawable.eugenio),
    Carta(R.drawable.eugenio, bocaArriba = true),
    Carta(R.drawable.faemino, bocaArriba = true, emparejada = true),
    Carta(R.drawable.faemino, bocaArriba = true, emparejada = true),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun TableroJuegoPreview() {
    TableroJuego(
        cartas = cartasDeEjemplo,
        terminado = false,
        onCardClick = {}, // en la preview los clics no hacen nada
        onValidate = {},
        onNuevaPartida = {},
    )
}

// Segunda preview: el diálogo de victoria sin tener que jugar una partida entera.
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun TableroJuegoTerminadoPreview() {
    TableroJuego(
        // Todas emparejadas, coherente con terminado = true.
        cartas = cartasDeEjemplo.map { it.copy(bocaArriba = true, emparejada = true) },
        terminado = true,
        onCardClick = {},
        onValidate = {},
        onNuevaPartida = {},
    )
}
