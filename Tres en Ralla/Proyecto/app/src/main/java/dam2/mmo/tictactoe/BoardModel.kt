package dam2.mmo.tictactoe

// =====================================================================
//  MODEL · Datos y reglas PURAS del Tres en Ratlla.
//   - No importa NADA de Android ni de Compose: es Kotlin puro.
//     Por eso se puede probar con un test unitario sin emulador
//     (ver app/src/test/.../BoardModelTest.kt).
//   - Es INMUTABLE: ninguna función cambia el objeto actual; todas devuelven
//     un BoardModel nuevo con copy(). StateFlow solo avisa a la View cuando
//     recibe un valor DISTINTO, así que mutar la misma lista no repintaría nada.
// =====================================================================

/**
 * Contenido posible de una casilla.
 * Se usa un enum y no un String ("X", "O", ""): con un String podrías escribir
 * "x" o "0" por error y el compilador no se quejaría. Con el enum, sí.
 */
enum class Symbol {
    X, O, EMPTY;

    /** El rival de este símbolo. Sirve para pasar el turno y para la IA. */
    fun opponent(): Symbol = when (this) {
        X -> O
        O -> X
        EMPTY -> EMPTY   // EMPTY no tiene rival; el when debe cubrir todos los casos
    }
}

/**
 * Resultado de la partida visto desde el JUGADOR humano.
 * La View lo usa para elegir el color de la rejilla y el mensaje,
 * sin tener que saber qué símbolo es el humano y cuál la CPU.
 */
enum class GameResult { PLAYING, HUMAN_WINS, CPU_WINS, DRAW }

/**
 * Una casilla del tablero.
 *
 * @param id identidad estable de la casilla. LazyVerticalGrid la usa como `key`
 *           para saber qué celda es cuál entre recomposiciones.
 * @param position índice 0..8 en el tablero (fila * 3 + columna). Es el que
 *           viaja en los clics. En un 3x3 fijo coincide con el id, pero el
 *           enunciado pide ambos y separarlos deja claro "quién es" y "dónde está".
 * @param symbol X, O o EMPTY.
 */
data class CellModel(
    val id: Int,
    val position: Int,
    val symbol: Symbol = Symbol.EMPTY,
) {
    // Propiedad DERIVADA (se calcula, no se guarda): imposible que se desincronice.
    val isEmpty: Boolean get() = symbol == Symbol.EMPTY
}

/**
 * Estado completo de la partida.
 *
 * @param cells las 9 casillas en orden de tablero (índice = position).
 * @param currentTurn a quién le toca mover.
 * @param winner símbolo ganador, o null si nadie ha hecho 3 en raya todavía.
 * @param isDraw true si el tablero está lleno y no hay ganador.
 */
data class BoardModel(
    val cells: List<CellModel> = emptyList(),
    val currentTurn: Symbol = HUMAN,
    val winner: Symbol? = null,
    val isDraw: Boolean = false,
) {

    // -----------------------------------------------------------------
    //  DATOS DERIVADOS · Se calculan a partir de los campos de arriba.
    //  Misma lección que juegoTerminado() en JuegoParejas: si se puede
    //  calcular, no se guarda, y así no hay dos fuentes de verdad.
    // -----------------------------------------------------------------

    /** La partida ha acabado si hay ganador o empate. */
    val isGameOver: Boolean get() = winner != null || isDraw

    /** ¿Puede pulsar el humano ahora mismo? */
    val isHumanTurn: Boolean get() = !isGameOver && currentTurn == HUMAN

    /** Traduce winner/isDraw a "has ganado / has perdido / empate / jugando". */
    val result: GameResult
        get() = when {
            winner == HUMAN -> GameResult.HUMAN_WINS
            winner == CPU -> GameResult.CPU_WINS
            isDraw -> GameResult.DRAW
            else -> GameResult.PLAYING
        }

    // -----------------------------------------------------------------
    //  LÓGICA DE NEGOCIO
    // -----------------------------------------------------------------

    /**
     * Coloca el símbolo de [currentTurn] en [position], pasa el turno
     * y comprueba si alguien ha ganado.
     *
     * Si la jugada no es válida devuelve `this` (la MISMA referencia):
     * el ViewModel lo detecta y StateFlow no emite nada nuevo.
     * Las comprobaciones están aquí y no en la View porque son reglas del juego.
     */
    fun makeMove(position: Int): BoardModel {
        // 1. Partida acabada -> no se puede seguir jugando.
        if (isGameOver) return this

        // 2. Fuera de rango -> nada. Sin esto, cells[position] lanzaría
        //    IndexOutOfBoundsException (por ejemplo con BoardModel() vacío).
        if (position !in cells.indices) return this

        // 3. Casilla ocupada -> nada. Se puede usar cells[position] porque
        //    createInitialBoard() garantiza que índice == position.
        if (!cells[position].isEmpty) return this

        // 4. Lista NUEVA donde solo cambia la casilla pulsada.
        val newCells = cells.map { cell ->
            if (cell.position == position) cell.copy(symbol = currentTurn) else cell
        }

        // 5. Se pasa el turno y se encadena checkWinner(): como ambas devuelven
        //    un BoardModel, se pueden encadenar igual que map().filter().
        return copy(cells = newCells, currentTurn = currentTurn.opponent())
            .checkWinner()
    }

    /**
     * Mira las 8 líneas ganadoras y devuelve un BoardModel con
     * [winner] o [isDraw] actualizados. Si la partida sigue, devuelve `this`.
     */
    fun checkWinner(): BoardModel {
        val lineWinner = winnerOf(cells)
        return when {
            // El ganador se mira ANTES que el empate: si la última ficha
            // llena el tablero Y hace 3 en raya, es victoria, no empate.
            lineWinner != null -> copy(winner = lineWinner)
            // isNotEmpty(): emptyList().none { } devuelve true, y un
            // BoardModel() sin casillas contaría como empate.
            cells.isNotEmpty() && cells.none { it.isEmpty } -> copy(isDraw = true)
            else -> this
        }
    }

    /**
     * "IA" sencilla: decide en qué casilla jugaría [symbol].
     * Devuelve null si no queda ninguna libre.
     *
     * Prioridades (de más a menos importante):
     *  1. Ganar si puede.
     *  2. Bloquear si el rival está a punto de ganar.
     *  3. Coger el centro.
     *  4. Coger una esquina al azar.
     *  5. Cualquier casilla libre.
     * No es perfecta a propósito (no prevé las "dobles amenazas"),
     * así que se le puede ganar.
     */
    fun bestMoveFor(symbol: Symbol): Int? {
        val free = cells.filter { it.isEmpty }.map { it.position }
        if (free.isEmpty()) return null

        // ?.let { return it } = "si encuentras algo, devuélvelo y sal de la función".
        free.firstOrNull { wouldWin(it, symbol) }?.let { return it }
        free.firstOrNull { wouldWin(it, symbol.opponent()) }?.let { return it }
        if (CENTER in free) return CENTER
        free.filter { it in CORNERS }.randomOrNull()?.let { return it }
        return free.random()
    }

    /** Simula poner [symbol] en [position] SIN modificar el tablero real. */
    private fun wouldWin(position: Int, symbol: Symbol): Boolean {
        val simulated = cells.map { if (it.position == position) it.copy(symbol = symbol) else it }
        return winnerOf(simulated) == symbol
    }

    // El companion object es lo que en Java sería "static": se usa sin instancia,
    // por ejemplo BoardModel.createInitialBoard() o BoardModel.HUMAN.
    companion object {
        const val SIZE = 3                 // tablero 3x3
        const val CENTER = 4               // índice de la casilla central
        val CORNERS = listOf(0, 2, 6, 8)

        // Quién es quién. Si quisieras que el humano jugara con O,
        // solo cambiarías estas dos líneas.
        val HUMAN = Symbol.X
        val CPU = Symbol.O

        /**
         * Las 8 combinaciones ganadoras, con los índices así:
         *   0 | 1 | 2
         *   3 | 4 | 5
         *   6 | 7 | 8
         */
        val WIN_LINES = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),   // filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),   // columnas
            listOf(0, 4, 8), listOf(2, 4, 6),                    // diagonales
        )

        /**
         * Método de fábrica: tablero vacío y empieza el humano.
         * List(9) { i -> ... } crea una lista de 9 elementos llamando a la
         * lambda con cada índice: así id y position valen 0..8.
         */
        fun createInitialBoard(): BoardModel = BoardModel(
            cells = List(SIZE * SIZE) { i -> CellModel(id = i, position = i) },
            currentTurn = HUMAN,
        )

        /**
         * Devuelve el símbolo con 3 en raya en [cells], o null.
         * Está en el companion (y recibe la lista) para poder usarla tanto con
         * el tablero real como con el simulado de wouldWin().
         */
        private fun winnerOf(cells: List<CellModel>): Symbol? {
            // Sin las 9 casillas, cells[...] fallaría.
            if (cells.size != SIZE * SIZE) return null

            // firstNotNullOfOrNull: recorre las líneas y devuelve el primer
            // resultado que NO sea null (el símbolo ganador), o null si ninguna gana.
            return WIN_LINES.firstNotNullOfOrNull { line ->
                val first = cells[line[0]].symbol
                if (first != Symbol.EMPTY && line.all { cells[it].symbol == first }) first else null
            }
        }
    }
}
