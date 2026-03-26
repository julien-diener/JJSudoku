package org.jjgame.sudoku

import org.jjgame.hodoku.GenerationResult
import org.jjgame.hodoku.HodokuPuzzleGenerator
import sudoku.DifficultyType

/**
 * Public game difficulty levels used by the app.
 *
 * Mirrors HoDoKu levels but stays defined in core-game so app modules do not depend on
 * hodoku-core internals.
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD,
    UNFAIR,
    EXTREME,
}

object SudokuGenerator {
    /**
     * Generates a puzzle rated by HoDoKu at the requested [difficulty].
     *
     * Uses a bounded attempt budget and returns best-effort when exact target level is not
     * found in time, so generation remains responsive.
     */
    fun generate(difficulty: Difficulty = Difficulty.EASY, maxAttempts: Int = 100): SudokuPuzzle {
        val target = difficulty.toHodokuDifficulty()
        val result = HodokuPuzzleGenerator.generate(target, maxAttempts)
        val generated = when (result) {
            is GenerationResult.Success -> result.puzzle
            is GenerationResult.BestEffort -> result.best
        }

        return SudokuPuzzle(Array(81) { index ->
            SudokuCell(
                value = generated.solution[index],
                state = if (generated.clues[index] == 0) CellState.NOT_FOUND else CellState.GIVEN,
            )
        })
    }

    private fun Difficulty.toHodokuDifficulty(): DifficultyType = when (this) {
        Difficulty.EASY -> DifficultyType.EASY
        Difficulty.MEDIUM -> DifficultyType.MEDIUM
        Difficulty.HARD -> DifficultyType.HARD
        Difficulty.UNFAIR -> DifficultyType.UNFAIR
        Difficulty.EXTREME -> DifficultyType.EXTREME
    }
}
