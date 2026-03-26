package org.jjgame.hodoku

import generator.SudokuGeneratorFactory
import solver.SudokuSolver
import sudoku.DifficultyType
import sudoku.Sudoku2

/**
 * Result of a puzzle generation attempt.
 *
 * [clues] is a 81-element int array (0 = empty, 1-9 = given).
 * [solution] is the solved 81-cell grid (1-9 values).
 * [rating] is the HoDoKu difficulty rating of the generated puzzle.
 */
data class GeneratedPuzzle(
    val clues: IntArray,
    val solution: IntArray,
    val rating: HodokuRating,
) {
    // IntArray does not implement structural equals by default
    override fun equals(other: Any?): Boolean =
        other is GeneratedPuzzle &&
            clues.contentEquals(other.clues) &&
            solution.contentEquals(other.solution) &&
            rating == other.rating

    override fun hashCode(): Int =
        31 * (31 * clues.contentHashCode() + solution.contentHashCode()) + rating.hashCode()
}

/**
 * Outcome of [HodokuPuzzleGenerator.generate].
 */
sealed class GenerationResult {
    /** A puzzle matching the requested difficulty was found within the attempt budget. */
    data class Success(val puzzle: GeneratedPuzzle) : GenerationResult()

    /**
     * No puzzle matching the requested difficulty was found within [attempts] tries.
     * [best] carries the closest result found (by score distance to the target band),
     * so the caller can decide whether to use it as a fallback.
     */
    data class BestEffort(val best: GeneratedPuzzle, val attempts: Int) : GenerationResult()
}

/**
 * Generates Sudoku puzzles rated by HoDoKu's human-technique solver.
 *
 * Uses rejection sampling: generate → rate → keep if difficulty matches.
 * If no matching puzzle is found within [maxAttempts], returns [GenerationResult.BestEffort]
 * with the closest puzzle found, so callers are never left empty-handed.
 *
 * Difficulty mapping (from HoDoKu score bands):
 * - EASY:    score ≤ 800   (singles only)
 * - MEDIUM:  score ≤ 1000  (basic subsets/intersections)
 * - HARD:    score ≤ 1600  (chains, fish, uniqueness)
 * - UNFAIR:  score ≤ 1800  (complex chains, ALS)
 * - EXTREME: score > 1800  (templates, forcing nets)
 *
 * Expected cost per puzzle (measured empirically on a desktop JVM):
 * - EASY:    ~2 attempts   (~40ms)
 * - MEDIUM:  ~4 attempts   (~80ms)
 * - HARD:    ~6 attempts   (~120ms)
 * - UNFAIR:  ~10 attempts  (~200ms)
 * - EXTREME: ~14 attempts  (~280ms)
 */
object HodokuPuzzleGenerator {

    /**
     * Generates a puzzle at the requested [difficulty].
     *
     * @param difficulty  target HoDoKu difficulty level
     * @param maxAttempts maximum number of generate+rate cycles before giving up (default 100)
     * @return [GenerationResult.Success] if a matching puzzle is found,
     *         [GenerationResult.BestEffort] with the closest puzzle otherwise
     */
    fun generate(
        difficulty: DifficultyType,
        maxAttempts: Int = 100,
    ): GenerationResult {
        require(difficulty != DifficultyType.INCOMPLETE) {
            "Cannot request a puzzle with difficulty INCOMPLETE"
        }
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }

        val generator = SudokuGeneratorFactory.getInstance()
        var best: GeneratedPuzzle? = null
        var bestDistance = Int.MAX_VALUE

        try {
            repeat(maxAttempts) {
                val puzzle = generator.generateSudoku(false)
                val clues = puzzle.values.copyOf()
                val generated = rateAndSolve(clues)

                if (generated.rating.difficulty == difficulty) {
                    return GenerationResult.Success(generated)
                }

                // Track closest puzzle by score distance to the target band midpoint
                val distance = scoreDistance(generated.rating, difficulty)
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = generated
                }
            }
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }

        return GenerationResult.BestEffort(best!!, maxAttempts)
    }

    /**
     * Distance between a rating's score and the midpoint of the target difficulty band.
     * Used to pick the best fallback when no exact match is found within the attempt budget.
     */
    private fun scoreDistance(rating: HodokuRating, target: DifficultyType): Int {
        val targetMidpoint = when (target) {
            DifficultyType.EASY    -> 400
            DifficultyType.MEDIUM  -> 900
            DifficultyType.HARD    -> 1300
            DifficultyType.UNFAIR  -> 1700
            DifficultyType.EXTREME -> 2500
            DifficultyType.INCOMPLETE -> 0
        }
        return Math.abs(rating.score - targetMidpoint)
    }

    private fun rateAndSolve(clues: IntArray): GeneratedPuzzle {
        val grid = Sudoku2().apply { setSudoku(toHodokuGridString(clues), true) }
        val solver = SudokuSolver().apply { setSudoku(grid) }
        val solved = solver.solve(false)
        val rating = HodokuRating(
            solved = solved,
            difficulty = solver.level.type,
            score = solver.score,
        )
        return GeneratedPuzzle(
            clues = clues.copyOf(),
            solution = grid.values.copyOf(),
            rating = rating,
        )
    }

    private fun toHodokuGridString(clues: IntArray): String = buildString(81) {
        for (value in clues) {
            append(if (value == 0) '.' else ('0' + value))
        }
    }
}

