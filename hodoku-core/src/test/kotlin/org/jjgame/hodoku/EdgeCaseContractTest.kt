package org.jjgame.hodoku

import generator.SudokuGeneratorFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import solver.SudokuSolver
import sudoku.Sudoku2

class EdgeCaseContractTest {
    @Test
    fun solver_returns_false_when_grid_has_less_than_10_clues() {
        val sparse = Sudoku2().apply {
            setSudoku(
                "123456789" +
                    "........." +
                    "........." +
                    "........." +
                    "........." +
                    "........." +
                    "........." +
                    "........." +
                    ".........",
                true,
            )
        }

        val solver = SudokuSolver().apply { setSudoku(sparse) }
        val solved = solver.solve(false)

        assertFalse(solved)
    }

    @Test
    fun generator_validSolution_returns_true_and_populates_solution_for_unique_grid() {
        val puzzle = Sudoku2().apply {
            setSudoku(
                "...26.7.1" +
                    "68..7..9." +
                    "19...45.." +
                    "82.1...4." +
                    "..46.29.." +
                    ".5...3.28" +
                    "..93...74" +
                    ".4..5..36" +
                    "7.3.18...",
                true,
            )
        }

        val generator = SudokuGeneratorFactory.getInstance()
        try {
            val unique = generator.validSolution(puzzle)

            assertTrue(unique)
            assertTrue(puzzle.solution.all { it in 1..9 })
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }
    }

    @Test
    fun rater_marks_contradictory_clues_as_unsolved() {
        val contradictory = IntArray(81) { 0 }.also {
            it[0] = 1
            it[1] = 1 // same row contradiction
        }

        val rating = HodokuDifficultyRater.rate(contradictory)

        assertFalse(rating.solved)
        assertTrue(rating.score >= 0)
    }

    @Test
    fun generator_solution_count_is_one_for_known_unique_grid() {
        val puzzle = Sudoku2().apply {
            setSudoku(
                "...26.7.1" +
                    "68..7..9." +
                    "19...45.." +
                    "82.1...4." +
                    "..46.29.." +
                    ".5...3.28" +
                    "..93...74" +
                    ".4..5..36" +
                    "7.3.18...",
                true,
            )
        }

        val generator = SudokuGeneratorFactory.getInstance()
        try {
            val count = generator.getNumberOfSolutions(puzzle, 2)
            assertEquals(1, count)
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }
    }
}

