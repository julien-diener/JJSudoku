package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.random.Random

class SudokuGeneratorTest {
    @Test
    fun `generated puzzle has expected number of empty cells`() {
        val puzzle = SudokuGenerator.generate(Difficulty.MEDIUM)
        val notFound = puzzle.mapCells { _, cell -> cell }
            .count { !it.state.isFixed() }
        assertEquals(Difficulty.MEDIUM.emptyCells, notFound)
    }

    @Test
    fun `all cells are in valid range`() {
        val puzzle = SudokuGenerator.generate(Difficulty.HARD)
        puzzle.mapCells { _, cell -> cell }
            .forEach { cell -> assertTrue(cell.value in 1..9) }
    }

    @Test
    fun `generated easy puzzle has a unique solution`() {
        assertGeneratedPuzzleIsUnique(Difficulty.EASY, seed = 11)
    }

    @Test
    fun `generated medium puzzle has a unique solution`() {
        assertGeneratedPuzzleIsUnique(Difficulty.MEDIUM, seed = 22)
    }

    @Test
    fun `generated hard puzzle has a unique solution`() {
        assertGeneratedPuzzleIsUnique(Difficulty.HARD, seed = 33)
    }

    private fun assertGeneratedPuzzleIsUnique(difficulty: Difficulty, seed: Int) {
        val puzzle = SudokuGenerator.generate(difficulty, Random(seed))
        val clues = IntArray(81) { idx ->
            val cell = puzzle.cellAt(CellIndex(idx))
            if (cell.state.isFixed()) cell.value else 0
        }
        assertEquals(1, SudokuSolutionCounter.countSolutions(clues, limit = 2))
    }
}

