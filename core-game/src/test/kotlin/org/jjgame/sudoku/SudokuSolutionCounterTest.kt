package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals

class SudokuSolutionCounterTest {
    @Test
    fun `returns one solution for solved grid`() {
        val solved = intArrayOf(
            5, 3, 4, 6, 7, 8, 9, 1, 2,
            6, 7, 2, 1, 9, 5, 3, 4, 8,
            1, 9, 8, 3, 4, 2, 5, 6, 7,
            8, 5, 9, 7, 6, 1, 4, 2, 3,
            4, 2, 6, 8, 5, 3, 7, 9, 1,
            7, 1, 3, 9, 2, 4, 8, 5, 6,
            9, 6, 1, 5, 3, 7, 2, 8, 4,
            2, 8, 7, 4, 1, 9, 6, 3, 5,
            3, 4, 5, 2, 8, 6, 1, 7, 9,
        )

        assertEquals(1, SudokuSolutionCounter.countSolutions(solved, limit = 2))
    }

    @Test
    fun `returns zero solutions for conflicting clues`() {
        val conflicting = IntArray(81)
        conflicting[CellIndex(0, 0).value] = 5
        conflicting[CellIndex(0, 1).value] = 5

        assertEquals(0, SudokuSolutionCounter.countSolutions(conflicting, limit = 2))
    }

    @Test
    fun `returns at least two solutions for empty grid with limit two`() {
        val empty = IntArray(81)

        assertEquals(2, SudokuSolutionCounter.countSolutions(empty, limit = 2))
    }
}

