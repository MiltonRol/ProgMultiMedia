package dam2.mmo.firstapp

import androidx.lifecycle.ViewModel

// ---------------------------------------------------------------------------
// CONTROLADOR (C de MVC) implementado como ViewModel
//
// Responsabilidad: recibir los EVENTOS de la Vista (clic en Start, clic en un
// cubilete...) y traducirlos en llamadas al Modelo. No dibuja nada.
//
// Por qué un ViewModel y no una clase normal:
//  - Sobrevive a rotaciones de pantalla: el estado del juego no se pierde.
//  - Una clase normal creada dentro de la Activity se destruiría con ella.
//
// Por qué la Vista NO guarda estado propio del juego:
//  - Antes 'counter' y 'textBoton' vivían dentro del Composable (remember).
//    Ahora el estado vive en el Modelo y la Vista solo lo LEE.
//    Flujo: Vista --evento--> Controlador --> Modelo --estado observable--> Vista
//
// Se eliminaron ImageView / View.OnClickListener: son de las Views clásicas (XML)
// y en Compose el clic se pasa directamente como lambda ({ controlador.onX() }).
// ---------------------------------------------------------------------------
class Controlador : ViewModel() {

    /**
     * El Modelo vive dentro del ViewModel para que comparta su ciclo de vida.
     * Es público (val) para que la Vista pueda LEER su estado (modelo.estado, ...),
     * pero las modificaciones deben pasar siempre por los métodos de este Controlador.
     */
    val modelo: Model = Model()

    // ------------------------- EVENTOS DE LA VISTA -------------------------

    /**
     * Se llama al pulsar el botón Start.
     * TODO("Implementar: si modelo.estado es PARADO, ACIERTO o FALLO -> modelo.iniciarPartida().
     *       Si ya está BARAJANDO o ELIGIENDO, no hacer nada (evita reiniciar a mitad de jugada).")
     */
    fun onStartClick() {
        TODO("Implementar onStartClick()")
    }

    /**
     * Se llama cuando termina la animación de barajado en la Vista.
     * TODO("Implementar: delegar en modelo.terminarBarajado().
     *       Pista: la Vista avisa cuando acaba su animación; el Controlador no usa delays ni UI.")
     */
    fun onBarajadoTerminado() {
        TODO("Implementar onBarajadoTerminado()")
    }

    /**
     * Se llama al pulsar un cubilete.
     * @param posicion número del cubilete pulsado, de 1 a modelo.numCubiletes.
     * TODO("Implementar: validar que posicion esté en 1..modelo.numCubiletes
     *       y llamar a modelo.elegir(posicion). Ignorar posiciones fuera de rango.")
     * Por qué validar aquí: el Modelo asume datos correctos; el Controlador es la frontera
     * con el mundo exterior (la UI).
     */
    fun onCubileteClick(posicion: Int) {
        TODO("Implementar onCubileteClick(posicion)")
    }

    /**
     * Se llama al pulsar el botón/interruptor de 3 <-> 6 cubiletes.
     * TODO("Implementar: delegar en modelo.cambiarModo().")
     */
    fun onCambiarModoClick() {
        TODO("Implementar onCambiarModoClick()")
    }

    /**
     * Se llama al pulsar "Reiniciar" marcadores.
     * TODO("Implementar: delegar en modelo.reiniciar().")
     */
    fun onReiniciarClick() {
        TODO("Implementar onReiniciarClick()")
    }
}
