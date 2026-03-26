package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SudokuGeneratorTest {

    @Test
    fun `generated easy puzzle cells are in valid range`() {
            val puzzle = SudokuGenerator.generate(Difficulty.EASY)
        puzzle.mapCells { _, cell -> cell }
            .forEach { cell -> assertTrue(cell.value in 0..9) }
    }

    @Test
    fun `generated easy puzzle has given and not-found cells`() {
            val puzzle = SudokuGenerator.generate(Difficulty.EASY)
        val givens = puzzle.mapCells { _, cell -> cell }.count { it.state == CellState.GIVEN }
        val empty  = puzzle.mapCells { _, cell -> cell }.count { it.state == CellState.NOT_FOUND }
        assertTrue(givens >= 17, "Expected at least 17 givens, got $givens")
        assertEquals(81, givens + empty)
    }

    @Test
    fun `generated easy puzzle has unique solution`() {
            val puzzle = SudokuGenerator.generate(Difficulty.EASY)
        val clues = IntArray(81) { idx ->
            val cell = puzzle.cellAt(CellIndex(idx))
            if (cell.state.isFixed()) cell.value else 0
        }
        assertEquals(1, SudokuSolutionCounter.countSolutions(clues, limit = 2))
    }

    @Test
    fun `generated hard puzzle has unique solution`() {
            val puzzle = SudokuGenerator.generate(Difficulty.HARD)
        val clues = IntArray(81) { idx ->
            val cell = puzzle.cellAt(CellIndex(idx))
            if (cell.state.isFixed()) cell.value else 0
        }
        assertEquals(1, SudokuSolutionCounter.countSolutions(clues, limit = 2))
    }
}
