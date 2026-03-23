package org.jjgame.sudokuapp

import org.jjgame.sudoku.CellState
import org.jjgame.sudoku.CellIndex
import org.jjgame.sudoku.Difficulty
import org.jjgame.sudoku.SudokuCell
import org.jjgame.sudoku.SudokuGameSession
import org.jjgame.sudoku.SudokuPuzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GameSessionStorageTest {
    @Test
    fun `decode reads current schema fixture json`() {
        val restored = GameSessionStorage.decode(readFixture("game-session-v1.json"))

        assertNotNull(restored)
        assertEquals(Difficulty.MEDIUM, restored?.difficulty)
        val cellCount = restored!!.session.puzzle.mapCells { _, _ -> Unit }.size
        assertEquals(81, cellCount)

        val puzzle = restored.session.puzzle
        assertEquals(CellState.FOUND, puzzle.cellAt(CellIndex(0)).state)
        assertEquals(1, puzzle.cellAt(CellIndex(0)).value)
        assertEquals(CellState.NOT_FOUND, puzzle.cellAt(CellIndex(1)).state)
        assertEquals(2, puzzle.cellAt(CellIndex(1)).value)
        assertEquals(CellState.GIVEN, puzzle.cellAt(CellIndex(80)).state)
        assertEquals(9, puzzle.cellAt(CellIndex(80)).value)
    }

    @Test
    fun `decode returns null on unsupported schema version`() {
        val json = readFixture("game-session-v1.json").replace("\"version\": 1", "\"version\": 999")

        val restored = GameSessionStorage.decode(json)

        assertNull(restored)
    }

    @Test
    fun `encode and decode preserve difficulty and puzzle cells`() {
        val cells = Array(81) { index ->
            val value = (index % 9) + 1
            val state = when (index) {
                3 -> CellState.NOT_FOUND
                7 -> CellState.FOUND
                else -> CellState.GIVEN
            }
            SudokuCell(value = value, state = state)
        }
        val session = SudokuGameSession(SudokuPuzzle(cells))

        val encoded = GameSessionStorage.encode(Difficulty.HARD, session)
        val restored = GameSessionStorage.decode(encoded)

        assertNotNull(restored)
        assertEquals(Difficulty.HARD, restored?.difficulty)
        assertEquals(session.puzzle, restored?.session?.puzzle)
    }

    private fun readFixture(name: String): String {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) {
            "Fixture not found: $name"
        }
        return stream.bufferedReader().use { it.readText() }
    }
}

