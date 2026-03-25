package org.jjgame.hodoku.api

import org.jjgame.hodoku.HodokuDifficultyRater
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import solver.SudokuSolver
import sudoku.ClipboardMode
import sudoku.SolutionType
import sudoku.Sudoku2

class CoreApiContractTest {
    @Test
    fun rating_and_solver_agree_on_known_valid_puzzle() {
        val clues = intArrayOf(
            0, 0, 0, 2, 6, 0, 7, 0, 1,
            6, 8, 0, 0, 7, 0, 0, 9, 0,
            1, 9, 0, 0, 0, 4, 5, 0, 0,
            8, 2, 0, 1, 0, 0, 0, 4, 0,
            0, 0, 4, 6, 0, 2, 9, 0, 0,
            0, 5, 0, 0, 0, 3, 0, 2, 8,
            0, 0, 9, 3, 0, 0, 0, 7, 4,
            0, 4, 0, 0, 5, 0, 0, 3, 6,
            7, 0, 3, 0, 1, 8, 0, 0, 0,
        )

        val rating = HodokuDifficultyRater.rate(clues)

        val grid = Sudoku2().apply { setSudoku(toGridString(clues), true) }
        val solver = SudokuSolver().apply { setSudoku(grid) }
        val solved = solver.solve(false)

        assertTrue(rating.solved)
        assertTrue(solved)
        assertEquals(solver.score, rating.score)
        assertEquals(solver.level.type, rating.difficulty)
        assertTrue(grid.isSolved())
    }

    @Test
    fun rating_rejects_invalid_input_shape_and_values() {
        assertFailsWith<IllegalArgumentException> {
            HodokuDifficultyRater.rate(IntArray(80) { 0 })
        }

        val invalidValues = IntArray(81) { 0 }.also { it[10] = 42 }
        assertFailsWith<IllegalArgumentException> {
            HodokuDifficultyRater.rate(invalidValues)
        }
    }

    @Test
    fun sudoku2_roundtrip_preserves_grid_layout() {
        val gridString =
            "...26.7.1" +
            "68..7..9." +
            "19...45.." +
            "82.1...4." +
            "..46.29.." +
            ".5...3.28" +
            "..93...74" +
            ".4..5..36" +
            "7.3.18..."

        val sudoku = Sudoku2().apply { setSudoku(gridString, true) }
        val exported = sudoku.getSudoku(ClipboardMode.CLUES_ONLY)

        assertEquals(81, exported.length)
        assertEquals(gridString, exported)
    }

    @Test
    fun solver_hint_api_returns_next_step_for_unsolved_valid_grid() {
        val sudoku = Sudoku2().apply {
            setSudoku(
                "53..7...." +
                    "6..195..." +
                    ".98....6." +
                    "8...6...3" +
                    "4..8.3..1" +
                    "7...2...6" +
                    ".6....28." +
                    "...419..5" +
                    "....8..79",
                true,
            )
        }

        val solver = SudokuSolver()
        val hint = solver.getHint(sudoku, false)

        assertNotNull(hint)
        assertTrue(hint.type != SolutionType.GIVE_UP)
    }

    private fun toGridString(clues: IntArray): String = buildString(81) {
        for (value in clues) {
            append(if (value == 0) '.' else ('0' + value))
        }
    }
}



