package org.jjgame.hodoku.algorithm

import generator.SudokuGeneratorFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import solver.SudokuSolver
import sudoku.SolutionType
import sudoku.Sudoku2

class InternalAlgorithmContractTest {
    @Test
    fun generator_backtracking_solves_known_grid_to_expected_solution() {
        val puzzle =
            "...26.7.1" +
                "68..7..9." +
                "19...45.." +
                "82.1...4." +
                "..46.29.." +
                ".5...3.28" +
                "..93...74" +
                ".4..5..36" +
                "7.3.18..."

        val expectedSolution =
            "435269781" +
                "682571493" +
                "197834562" +
                "826195347" +
                "374682915" +
                "951743628" +
                "519326874" +
                "248957136" +
                "763418259"

        val generator = SudokuGeneratorFactory.getInstance()
        try {
            generator.solve(puzzle, 1)

            assertEquals(1, generator.solutionCount)
            assertEquals(expectedSolution, generator.solutionAsString)
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }
    }

    @Test
    fun solver_hint_then_doStep_progresses_grid() {
        val solved =
            "435269781" +
                "682571493" +
                "197834562" +
                "826195347" +
                "374682915" +
                "951743628" +
                "519326874" +
                "248957136" +
                "763418259"

        val oneMissing = "." + solved.substring(1)
        val sudoku = Sudoku2().apply { setSudoku(oneMissing, true) }

        val solver = SudokuSolver()
        val hint = solver.getHint(sudoku, true)

        assertNotNull(hint)
        assertTrue(
            hint.type == SolutionType.FULL_HOUSE ||
                hint.type == SolutionType.NAKED_SINGLE ||
                hint.type == SolutionType.HIDDEN_SINGLE,
        )

        solver.doStep(sudoku, hint)

        assertTrue(sudoku.isSolved)
        assertEquals(4, sudoku.getValue(0))
    }

    @Test
    fun symmetric_generation_keeps_rotationally_symmetric_clue_positions() {
        val generator = SudokuGeneratorFactory.getInstance()
        try {
            val puzzle = generator.generateSudoku(true)
            val values = puzzle.values

            for (index in 0 until 81) {
                val mirror = 9 * (8 - index / 9) + (8 - index % 9)
                val hasClue = values[index] != 0
                val mirrorHasClue = values[mirror] != 0
                assertEquals(hasClue, mirrorHasClue)
            }
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }
    }
}


