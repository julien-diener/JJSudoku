package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SudokuGeneratorTest {
    @Test
    fun `generated puzzle has expected number of empty cells`() {
        val puzzle = SudokuGenerator.generate(Difficulty.MEDIUM)
        val notFound = puzzle.cells.count { !it.state.isFixed() }
        assertEquals(Difficulty.MEDIUM.emptyCells, notFound)
    }

    @Test
    fun `all cells are in valid range`() {
        val puzzle = SudokuGenerator.generate(Difficulty.HARD)
        puzzle.cells.forEach { cell ->
            assertTrue(cell.value in 1..9)
        }
    }
}

