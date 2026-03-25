package org.jjgame.hodoku

import solver.SudokuSolver
import sudoku.DifficultyType
import sudoku.Sudoku2

data class HodokuRating(
    val solved: Boolean,
    val difficulty: DifficultyType,
    val score: Int,
)

object HodokuDifficultyRater {
    fun rate(clues: IntArray): HodokuRating {
        require(clues.size == 81) { "clues must have 81 entries" }
        require(clues.all { it in 0..9 }) { "clues must contain values between 0 and 9" }

        val grid = Sudoku2().apply {
            setSudoku(toHodokuGridString(clues), true)
        }

        val solver = SudokuSolver().apply {
            setSudoku(grid)
        }

        val solved = solver.solve(false)
        return HodokuRating(
            solved = solved,
            difficulty = solver.level.type,
            score = solver.score,
        )
    }

    private fun toHodokuGridString(clues: IntArray): String = buildString(81) {
        for (value in clues) {
            append(if (value == 0) '.' else ('0' + value))
        }
    }
}

