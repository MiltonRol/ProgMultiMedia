package dam2.mmo.juegoparejas

import androidx.annotation.DrawableRes

// =====================================================================
//  MODEL · Kotlin puro: aquí van los DATOS y las REGLAS del juego.
//  No importes nada de androidx.compose: el modelo no sabe que existe una UI.
//  (@DrawableRes es solo una anotación que comprueba que el Int es un drawable.)
//
//  DECISIÓN DE DISEÑO: modelo INMUTABLE.
//  Ninguna función cambia el objeto actual; todas devuelven un ModelGame NUEVO.
//  ¿Por qué? Compose solo recompone cuando cambia la REFERENCIA del estado.
//  Si hicieras carta.bocaArriba = true sobre la misma lista, la pantalla no
//  se enteraría. Con copy() siempre hay un objeto nuevo y Compose lo detecta.
// =====================================================================

/**
 * Una carta del tablero.
 *
 * Es una data class porque así tienes copy() gratis para crear la versión
 * modificada: carta.copy(bocaArriba = true).
 * Los campos son val (inmutables), coherente con la decisión de arriba.
 */
data class Carta(
    @DrawableRes
    val imagen: Int,    // la cara: R.drawable.chiquito, R.drawable.eugenio...
    val bocaArriba: Boolean = false, // true = se ve la cara; false = se ve back.png
    val emparejada: Boolean = false, // true = ya acertada; validar() no debe volver a taparla

){val girada: Boolean get() = bocaArriba && !emparejada}

/**
 * Estado completo de una partida: la lista de 6 cartas en el orden del tablero.
 * La posición en la lista (0..5) es lo que la View pasará como "index".
 *
 * Es una data class para poder hacer copy(cartas = nuevaLista) en cada jugada.
 */
data class ModelGame(
    val cartas: List<Carta> = emptyList(),
) {

    /**
     * Cartas boca arriba que TODAVÍA NO están emparejadas, es decir, las que
     * el jugador está intentando emparejar ahora mismo (0, 1 o 2).
     * Te sirve para limitar a 2 cartas y para saber si se puede validar.
     */
    fun cartasGiradas(): List<Carta> =
        // TODO("Fase 1: devuelve las cartas que están boca arriba Y no emparejadas")
        //  Pista: filter { ... } con las dos condiciones unidas por &&.

        cartas.filter{it.girada == true }


    /**
     * Devuelve un modelo NUEVO con la carta de la posición [index] boca arriba.
     *
     * ¿Las comprobaciones van aquí o en el ViewModel? Recomendación: AQUÍ.
     * Son reglas del juego, y así el ViewModel solo delega. Además podrás
     * probarlas con un test unitario sin arrancar Android.
     */
    fun girarCarta(index: Int): ModelGame {
        // 0. Índice fuera de rango -> no hacemos nada.
        //    ¿Por qué? cartas[index] lanzaría IndexOutOfBoundsException y cerraría la app.
        //    Una regla del juego debe ser segura ante cualquier entrada: si la View
        //    pasa un índice raro (o la lista está vacía), el modelo simplemente no cambia.
        if (index !in cartas.indices) return this

        // Guardamos la carta en una variable para no repetir cartas[index] tres veces.
        val carta = cartas[index]

        // 1. Ya está boca arriba o ya está emparejada -> no cambia nada.
        //    Devolver "this" (la MISMA referencia) es intencionado: Compose ve que el
        //    estado no ha cambiado y no recompone. Además, las Boolean ya son true/false,
        //    así que no hace falta compararlas con "== true".
        if (carta.bocaArriba || carta.emparejada) return this

        // 2. Ya hay 2 cartas giradas pendientes de validar -> no se puede girar una tercera.
        //    El jugador tiene que pulsar VALIDATE primero.
        //    Esta comprobación va DESPUÉS de la 1 porque es más cara (recorre la lista
        //    con filter) y la 1 descarta antes los casos más habituales.
        if (cartasGiradas().size >= 2) return this

        // 3. Lista NUEVA donde solo cambia la carta [index]; las demás se reutilizan tal cual.
        //    mapIndexed nos da el índice (i) además de la carta, que es justo lo que
        //    necesitamos para saber cuál es la pulsada.
        //    Alternativa descartada: cartas.toMutableList().also { it[index] = ... }
        //    también funcionaría, pero mapIndexed expresa mejor "copia transformando una".
        val nuevasCartas = cartas.mapIndexed { i, c ->
            if (i == index) c.copy(bocaArriba = true) // la pulsada: copia boca arriba
            else c                                     // el resto: la misma carta
        }

        // copy() crea un ModelGame NUEVO -> nueva referencia -> Compose recompone.
        return copy(cartas = nuevasCartas)
    }

    /**
     * Se llama al pulsar VALIDATE. Devuelve un modelo NUEVO:
     *  - si las 2 cartas giradas tienen la misma imagen -> se marcan emparejadas
     *  - si son distintas -> se vuelven a tapar (bocaArriba = false)
     */
    fun validar(): ModelGame {
        // ("Fase 1: implementa la validación")
        //  1. Si cartasGiradas().size != 2 -> return this (validar con 0 o 1 no hace nada)
        //  2. Compara las imágenes de las dos cartas giradas.
        //  3. Recorre TODAS las cartas con map { } y modifica solo las que estén
        //     giradas (boca arriba y no emparejadas):
        //       - iguales   -> carta.copy(emparejada = true)
        //       - distintas -> carta.copy(bocaArriba = false)
        //     Las demás se devuelven tal cual.
        //  Pregunta para pensar: ¿por qué no basta con comparar por índice y hay
        //  que comparar por imagen?

        val giradas = cartasGiradas()

        if (giradas.size != 2) return this
        val sonParejas = (giradas[0].imagen == giradas[1].imagen)
        val nuevas = cartas.map{carta ->
            if (carta.girada){
                if (sonParejas) carta.copy(emparejada = true)
                else carta.copy(bocaArriba = false)
            }else carta
        }

        return  copy(cartas = nuevas)

    }

    /** true cuando todas las cartas están emparejadas: fin de la partida. */
    fun juegoTerminado(): Boolean {
        // ("Fase 1: comprueba si TODAS las cartas están emparejadas")
        //  Pista: all { ... }. Ojo con la lista vacía: emptyList().all { } devuelve true.
        //

        return ( cartas.isNotEmpty() && cartas.all{ carta -> carta.emparejada })
    }

    // companion object = lo que en Java sería "static". Lo usamos como "fábrica"
    // para crear partidas sin tener que instanciar antes un ModelGame.
    companion object {

        // Las 4 caras disponibles en res/drawable. Solo necesitamos 3 (6 cartas = 3 parejas).
        // Están aquí y no "hardcodeadas" dentro de la función para poder pasar
        // otras imágenes desde un test.
        val CARAS_DISPONIBLES = listOf(
            R.drawable.chiquito,
            R.drawable.eugenio,
            R.drawable.faemino,
            R.drawable.rubianes,
        )

        /**
         * Crea una partida nueva: 3 imágenes, cada una 2 veces, barajadas y boca abajo.
         * Uso desde el ViewModel: ModelGame.nuevaPartida()
         *
         * @param caras imágenes entre las que elegir (por defecto, las 4 de drawable)
         * @param parejas cuántas parejas hay (3 para 6 cartas, como en el enunciado)
         */
        fun nuevaPartida(
            caras: List<Int> = CARAS_DISPONIBLES,
            parejas: Int = 3,
        ): ModelGame {
            // Comprobación previa: no se pueden pedir más parejas que caras distintas.
            // require lanza IllegalArgumentException con un mensaje claro si falla.
            // ¿Por qué? Sin ella, take(5) sobre 4 caras devolvería solo 4 en silencio
            // y la partida tendría menos cartas de las pedidas sin que nadie se enterase.
            require(parejas in 1..caras.size) {
                "parejas debe estar entre 1 y ${caras.size}, pero es $parejas"
            }

            val cartas = caras
                // 1. Baraja las caras y se queda con [parejas] de ellas.
                //    shuffled() antes de take() hace que cada partida use caras distintas.
                //    shuffled() devuelve una lista NUEVA: no desordena CARAS_DISPONIBLES.
                .shuffled()
                .take(parejas)
                // 2. Duplica cada imagen: [a, b, c] -> [a, a, b, b, c, c].
                //    flatMap aplana las listas pequeñas listOf(it, it) en una sola.
                //    Alternativa: elegidas + elegidas, pero entonces hace falta una variable
                //    intermedia y la cadena se rompe.
                .flatMap { imagen -> listOf(imagen, imagen) }
                // 3. Baraja las cartas: sin esto las parejas estarían siempre juntas.
                .shuffled()
                // 4. Convierte cada Int (drawable) en una Carta.
                //    bocaArriba y emparejada usan su valor por defecto (false):
                //    todas empiezan boca abajo y sin emparejar.
                .map { imagen -> Carta(imagen = imagen) }

            // Desde el companion se llama al constructor normal de ModelGame.
            return ModelGame(cartas = cartas)
        }
    }
}
