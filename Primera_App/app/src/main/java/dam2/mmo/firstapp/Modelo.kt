package dam2.mmo.firstapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ---------------------------------------------------------------------------
// MODELO (M de MVC) - Juego del Trilero con Jetpack Compose
//
// Responsabilidad del Modelo: guardar el ESTADO del juego y aplicar las REGLAS.
// NO sabe nada de pantallas, Composables ni clics (eso es Vista / Controlador).
//
// Por qué usamos mutableStateOf / mutableIntStateOf aquí dentro:
//  - Compose "observa" estos valores y redibuja solo la parte de la UI que los lee.
//  - Con Views (XML) hacía falta un listener + setText(); con Compose basta con
//    que el estado sea observable. Alternativa descartada: StateFlow (más potente,
//    pero para empezar es más código; se puede migrar luego).
// ---------------------------------------------------------------------------

/** Estados posibles de una partida. Un enum evita el típico par de booleanos confusos. */
enum class EstadoJuego {
    PARADO,      // Aún no se ha pulsado Start
    BARAJANDO,   // Los cubiletes se están moviendo, no se puede elegir
    ELIGIENDO,   // Esperando que el jugador pulse un cubilete
    ACIERTO,     // El jugador ha elegido el cubilete con la bola
    FALLO        // El jugador ha elegido otro cubilete
}

class Model {

    // ----------------------------- ESTADO ----------------------------------

    /** false = 3 cubiletes, true = 6 cubiletes (mismo criterio que en Controlador). */
    var modo by mutableStateOf(false)

    /** Estado actual de la partida. La Vista lo lee para decidir qué mostrar. */
    var estado by mutableStateOf(EstadoJuego.PARADO)

    /** Posición (1..n) del cubilete que esconde la bola. -1 = ninguna todavía. */
    var posicionBola by mutableIntStateOf(-1)

    /** Posición (1..n) elegida por el jugador. -1 = ninguna todavía. */
    var seleccion by mutableIntStateOf(-1)

    /** Aciertos acumulados. Es lo que mostrará el texto/contador de la Vista. */
    var aciertos by mutableIntStateOf(0)

    /** Fallos acumulados. */
    var fallos by mutableIntStateOf(0)

    // --------------------------- DATOS DERIVADOS ---------------------------

    /** Nº de cubiletes según el modo. Propiedad calculada: nunca queda desincronizada. */
    val numCubiletes: Int
        get() = if (modo) 6 else 3

    // ------------------------------ REGLAS ---------------------------------

    /**
     * Inicia una partida nueva.
     * TODO("Implementar: 1) poner estado = BARAJANDO, 2) reiniciar seleccion a -1,
     *       3) elegir posicionBola aleatoria entre 1..numCubiletes (pista: (1..numCubiletes).random()).")
     * Ojo: el barajado visual (animación) lo dispara la Vista; aquí solo se decide el dato.
     */
    fun iniciarPartida() {
        TODO("Implementar iniciarPartida()")
    }

    /**
     * Marca el fin del barajado y permite al jugador elegir.
     * TODO("Implementar: pasar de BARAJANDO a ELIGIENDO (solo si estado == BARAJANDO).")
     * Por qué comprobar el estado: evita que un clic o llamada fuera de turno rompa el juego.
     */
    fun terminarBarajado() {
        TODO("Implementar terminarBarajado()")
    }

    /**
     * Registra la elección del jugador y resuelve la jugada.
     * TODO("Implementar: 1) ignorar la llamada si estado != ELIGIENDO,
     *       2) guardar seleccion, 3) usar resultado(seleccion, posicionBola),
     *       4) si acierta: aciertos++ y estado = ACIERTO; si no: fallos++ y estado = FALLO.")
     */
    fun elegir(posicion: Int) {
        TODO("Implementar elegir(posicion)")
    }

    /**
     * Cambia entre 3 y 6 cubiletes.
     * TODO("Implementar: invertir 'modo' y volver a PARADO (reiniciar posicionBola y seleccion).
     *       Pista: no permitir cambiar de modo mientras se está BARAJANDO.")
     */
    fun cambiarModo() {
        TODO("Implementar cambiarModo()")
    }

    /**
     * Reinicia marcadores y estado.
     * TODO("Implementar: aciertos = 0, fallos = 0, estado = PARADO, posicionBola = -1, seleccion = -1.")
     */
    fun reiniciar() {
        TODO("Implementar reiniciar()")
    }

    /**
     * Regla pura: ¿ha acertado el jugador?
     * Ya implementada (venía de tu versión anterior). Se mantiene como función pura,
     * sin tocar el estado, para que sea fácil de testear con un test unitario.
     */
    fun resultado(seleccion: Int, posicion: Int): Boolean =
        (seleccion == posicion)

}
