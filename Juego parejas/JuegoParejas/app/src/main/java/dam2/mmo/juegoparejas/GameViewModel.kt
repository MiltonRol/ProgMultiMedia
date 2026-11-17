package dam2.mmo.juegoparejas

// Imports: solo los del runtime de Compose para el estado observable + ViewModel.
// No hay Composable ni remember: en un ViewModel no hay composables,
// y remember solo funciona DENTRO de una @Composable fun.
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// =====================================================================
//  VIEWMODEL · El "cable" entre la View y el Model.
//   - Guarda el estado que la pantalla observa (el ModelGame actual).
//   - Recibe los eventos de la View (onCardClick, onValidate...).
//   - NO tiene reglas del juego: las delega en ModelGame.
//   - NO conoce @Composable, Modifier ni remember.
//
//  DECISIÓN DE DISEÑO: Opción A del seguimiento (mutableStateOf + private set).
//  ¿Por qué y no StateFlow? GameScreen lee vm.model directamente, y para una
//  sola pantalla es lo más sencillo. La Opción B (StateFlow) está explicada
//  en "MVVM Compose Kotlin.md".
//
//  ¿Por qué hereda de ViewModel()? Android lo conserva al girar la pantalla:
//  la partida no se pierde aunque la Activity se destruya y se vuelva a crear.
// =====================================================================
class GameViewModel : ViewModel() {

    /**
     * Estado de la partida que observa la View.
     *
     * - mutableStateOf: Compose vigila el valor. Cuando le asignas un ModelGame
     *   NUEVO, todos los composables que leen vm.model se recomponen.
     * - SIN remember: el ViewModel ya sobrevive a las recomposiciones por sí mismo.
     * - by: delegación, para escribir "model" en vez de "model.value"
     *   (necesita los imports getValue y setValue).
     * - private set: la View puede LEER model, pero solo el ViewModel puede
     *   ASIGNARLO. Así el estado tiene un único dueño.
     * - Valor inicial: nuevaPartida() (la fábrica del companion object).
     *   Con ModelGame() a secas la lista de cartas estaría vacía y la
     *   pantalla no mostraría nada.
     */
    var model by mutableStateOf(ModelGame.nuevaPartida())
        private set

    // -----------------------------------------------------------------
    //  EVENTOS · Uno por cada acción del usuario.
    //  Patrón de todos ellos:
    //      model = model.algunaFuncionDelModelo(...)
    //  Le pides al modelo actual que te devuelva uno NUEVO y lo asignas.
    //  Como ModelGame es inmutable, sin la asignación el resultado se
    //  perdería y Compose no se enteraría de nada.
    // -----------------------------------------------------------------

    /**
     * El usuario ha pulsado la carta de la posición [index] (0..5).
     *
     * Las comprobaciones (carta ya girada, ya hay 2 giradas...) NO van aquí:
     * ya están en ModelGame.girarCarta(). El ViewModel solo delega.
     *
     * Si girarCarta devuelve "this" (jugada no válida), asignamos el mismo
     * objeto: mutableStateOf compara el valor nuevo con el anterior, ve que
     * es igual y NO recompone. Por eso no hace falta ningún if aquí.
     */
    fun onCardClick(index: Int) {
        model = model.girarCarta(index)
    }

    /**
     * El usuario ha pulsado VALIDATE.
     * El modelo decide si las 2 cartas giradas son pareja o se vuelven a tapar.
     */
    fun onValidate() {
        model = model.validar()
    }

    /**
     * Empezar una partida nueva, por ejemplo desde un botón "Nueva partida"
     * o desde el AlertDialog de victoria.
     *
     * No hay que "limpiar" nada: como el modelo es inmutable, basta con
     * sustituirlo por uno recién creado.
     * Dos trampas que evita esta línea:
     *  - No se envuelve en otro mutableStateOf: model YA es el estado observable;
     *    uno nuevo dentro de la función sería un estado local que nadie observa.
     *  - No se usa ModelGame(model.cartas): eso copiaría las MISMAS cartas
     *    (con su estado de girada/emparejada), no empezaría una partida.
     */
    fun onNuevaPartida() {
        model = ModelGame.nuevaPartida()
    }

    // ¿Y juegoTerminado? No hace falta un evento ni otra variable de estado:
    // es un dato DERIVADO. La View lo pregunta con vm.model.juegoTerminado()
    // y siempre está sincronizado con las cartas. Si lo guardaras en otro
    // mutableStateOf tendrías que acordarte de actualizarlo en cada evento.
}
