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
    fun `decode reads v1 fixture json (backward compatibility)`() {
        val restored = GameSessionStorage.decode(readFixture("game-session-v1.json"))

        assertNotNull(restored)
        assertEquals(Difficulty.MEDIUM, restored?.difficulty)
        val cellCount = restored!!.session.puzzle.mapCells { _, _ -> Unit }.size
        assertEquals(81, cellCount)

        val puzzle = restored.session.puzzle
        assertEquals(CellState.FOUND, puzzle.cellAt(CellIndex(0)).state)
        assertEquals(1, puzzle.cellAt(CellIndex(0)).value)
        assertEquals(CellState.NOT_FOUND, puzzle.cellAt(CellIndex(1)).state)
        assertEquals(CellState.GIVEN, puzzle.cellAt(CellIndex(80)).state)
        assertEquals(9, puzzle.cellAt(CellIndex(80)).value)

        // v1 has no timing fields; defaults should be used
        assertEquals(0L, restored.session.elapsedSeconds)
        assertNull(restored.session.finishedAt)
    }

    @Test
    fun `decode reads v2 fixture json with timing fields`() {
        val restored = GameSessionStorage.decode(readFixture("game-session-v2.json"))

        assertNotNull(restored)
        assertEquals(Difficulty.MEDIUM, restored?.difficulty)
        assertEquals(1742812800000L, restored!!.session.startedAt)
        assertEquals(742L, restored.session.elapsedSeconds)
        assertEquals(1742813542000L, restored.session.finishedAt)
    }

    @Test
    fun `decode returns null on unsupported schema version`() {
        val json = readFixture("game-session-v2.json").replace("\"version\": 2", "\"version\": 999")

        val restored = GameSessionStorage.decode(json)

        assertNull(restored)
    }

    @Test
    fun `encode and decode preserve difficulty, puzzle cells and timing fields`() {
        val cells = Array(81) { index ->
            val value = (index % 9) + 1
            val state = when (index) {
                3 -> CellState.NOT_FOUND
                7 -> CellState.FOUND
                else -> CellState.GIVEN
            }
            SudokuCell(value = value, state = state)
        }
        val session = SudokuGameSession(
            puzzle = SudokuPuzzle(cells),
            startedAt = 1742812800000L,
            elapsedSeconds = 300L,
            finishedAt = 1742813100000L,
        )

        val encoded = GameSessionStorage.encode(Difficulty.HARD, session)
        val restored = GameSessionStorage.decode(encoded)

        assertNotNull(restored)
        assertEquals(Difficulty.HARD, restored?.difficulty)
        assertEquals(session.puzzle, restored?.session?.puzzle)
        assertEquals(1742812800000L, restored?.session?.startedAt)
        assertEquals(300L, restored?.session?.elapsedSeconds)
        assertEquals(1742813100000L, restored?.session?.finishedAt)
    }

    private fun readFixture(name: String): String {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) {
            "Fixture not found: $name"
        }
        return stream.bufferedReader().use { it.readText() }
    }
}

