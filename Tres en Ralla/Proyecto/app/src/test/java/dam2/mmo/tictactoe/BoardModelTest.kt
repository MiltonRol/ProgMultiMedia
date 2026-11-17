package dam2.mmo.tictactoe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del Model. Como BoardModel no depende de Android, corren en la JVM
 * en segundos: ./gradlew :app:testDebugUnitTest
 */
class BoardModelTest {

    /** Aplica jugadas alternando X y O, empezando por X (el humano). */
    private fun boardAfter(vararg moves: Int): BoardModel =
        moves.fold(BoardModel.createInitialBoard()) { board, position -> board.makeMove(position) }

    @Test
    fun `el tablero inicial tiene 9 casillas vacías y empieza X`() {
        val board = BoardModel.createInitialBoard()
        assertEquals(9, board.cells.size)
        assertTrue(board.cells.all { it.isEmpty })
        assertEquals(Symbol.X, board.currentTurn)
        assertEquals(GameResult.PLAYING, board.result)
    }

    @Test
    fun `makeMove coloca el símbolo y pasa el turno`() {
        val board = boardAfter(4)
        assertEquals(Symbol.X, board.cells[4].symbol)
        assertEquals(Symbol.O, board.currentTurn)
    }

    @Test
    fun `pulsar una casilla ocupada devuelve el mismo objeto`() {
        val board = boardAfter(4)
        // assertSame comprueba la REFERENCIA (===): el ViewModel se basa en esto.
        assertSame(board, board.makeMove(4))
    }

    @Test
    fun `una posición fuera de rango se ignora`() {
        val board = BoardModel.createInitialBoard()
        assertSame(board, board.makeMove(9))
        assertSame(board, board.makeMove(-1))
    }

    @Test
    fun `tres X en fila es victoria del humano`() {
        val board = boardAfter(0, 3, 1, 4, 2)
        assertEquals(Symbol.X, board.winner)
        assertEquals(GameResult.HUMAN_WINS, board.result)
        assertTrue(board.isGameOver)
    }

    @Test
    fun `tres O en fila es derrota`() {
        val board = boardAfter(0, 3, 1, 4, 8, 5)
        assertEquals(GameResult.CPU_WINS, board.result)
    }

    @Test
    fun `tablero lleno sin ganador es empate`() {
        // X | O | X
        // X | O | O
        // O | X | X
        val board = boardAfter(0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertNull(board.winner)
        assertTrue(board.isDraw)
        assertEquals(GameResult.DRAW, board.result)
    }

    @Test
    fun `después de acabar no se puede seguir jugando`() {
        val board = boardAfter(0, 3, 1, 4, 2)
        assertSame(board, board.makeMove(8))
    }

    @Test
    fun `la CPU gana si puede`() {
        // O tiene 3 y 4; le toca a O y la casilla 5 le da la victoria.
        val board = boardAfter(0, 3, 1, 4, 8)
        assertEquals(5, board.bestMoveFor(Symbol.O))
    }

    @Test
    fun `la CPU bloquea al humano`() {
        // X tiene 0 y 1; O debe tapar la 2.
        val board = boardAfter(0, 4, 1)
        assertEquals(2, board.bestMoveFor(Symbol.O))
    }

    @Test
    fun `la CPU coge el centro si está libre`() {
        val board = boardAfter(0)
        assertEquals(4, board.bestMoveFor(Symbol.O))
    }

    @Test
    fun `BoardModel vacío no cuenta como empate`() {
        assertFalse(BoardModel().checkWinner().isDraw)
    }
}
