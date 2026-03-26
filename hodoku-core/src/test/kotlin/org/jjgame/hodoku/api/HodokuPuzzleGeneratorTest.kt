package org.jjgame.hodoku.api

import org.jjgame.hodoku.GenerationResult
import org.jjgame.hodoku.HodokuPuzzleGenerator
import sudoku.DifficultyType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HodokuPuzzleGeneratorTest {

    @Test
    fun generate_rejects_INCOMPLETE_difficulty() {
        assertFailsWith<IllegalArgumentException> {
            HodokuPuzzleGenerator.generate(DifficultyType.INCOMPLETE)
        }
    }

    @Test
    fun generate_rejects_zero_max_attempts() {
        assertFailsWith<IllegalArgumentException> {
            HodokuPuzzleGenerator.generate(DifficultyType.EASY, maxAttempts = 0)
        }
    }

    @Test
    fun generate_EASY_returns_success_or_best_effort_within_budget() {
        // EASY is ~44% natural probability → almost always succeeds within 100 tries
        val result = HodokuPuzzleGenerator.generate(DifficultyType.EASY, maxAttempts = 100)

        assertNotNull(result)
        val puzzle = when (result) {
            is GenerationResult.Success -> result.puzzle
            is GenerationResult.BestEffort -> result.best
        }
        assertEquals(81, puzzle.clues.size)
        assertTrue(puzzle.clues.all { it in 0..9 })
        assertTrue(puzzle.rating.solved)
    }

    @Test
    fun generate_success_puzzle_matches_requested_difficulty() {
        // Run a few attempts; if we get Success the difficulty must match exactly
        val result = HodokuPuzzleGenerator.generate(DifficultyType.EASY, maxAttempts = 100)

        if (result is GenerationResult.Success) {
            assertEquals(DifficultyType.EASY, result.puzzle.rating.difficulty)
        }
        // BestEffort is valid too — no assertion needed, just that it doesn't crash
    }

    @Test
    fun generate_with_one_attempt_still_returns_a_puzzle() {
        // With maxAttempts=1 we may not hit the right difficulty, but must always get a result
        val result = HodokuPuzzleGenerator.generate(DifficultyType.HARD, maxAttempts = 1)

        val puzzle = when (result) {
            is GenerationResult.Success -> result.puzzle
            is GenerationResult.BestEffort -> {
                assertEquals(1, result.attempts)
                result.best
            }
        }
        assertEquals(81, puzzle.clues.size)
        assertTrue(puzzle.rating.solved)
    }

    @Test
    fun generated_puzzle_clues_are_valid_sudoku_givens() {
        val result = HodokuPuzzleGenerator.generate(DifficultyType.EASY, maxAttempts = 100)

        val clues = when (result) {
            is GenerationResult.Success -> result.puzzle.clues
            is GenerationResult.BestEffort -> result.best.clues
        }

        // At least 17 givens (minimum for any valid Sudoku)
        assertTrue(clues.count { it != 0 } >= 17)

        // No row contains the same non-zero digit twice
        for (row in 0 until 9) {
            val values = (0 until 9).map { col -> clues[row * 9 + col] }.filter { it != 0 }
            assertEquals(values.size, values.toSet().size, "Duplicate value in row $row")
        }

        // No column contains the same non-zero digit twice
        for (col in 0 until 9) {
            val values = (0 until 9).map { row -> clues[row * 9 + col] }.filter { it != 0 }
            assertEquals(values.size, values.toSet().size, "Duplicate value in col $col")
        }

        // No 3×3 box contains the same non-zero digit twice
        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                val values = (0 until 3).flatMap { r ->
                    (0 until 3).map { c ->
                        clues[(boxRow * 3 + r) * 9 + (boxCol * 3 + c)]
                    }
                }.filter { it != 0 }
                assertEquals(values.size, values.toSet().size, "Duplicate value in box ($boxRow,$boxCol)")
            }
        }
    }

    @Test
    fun best_effort_result_contains_attempt_count() {
        // Force BestEffort by asking for EXTREME with only 1 attempt (very likely not EXTREME)
        // We can't guarantee BestEffort since 1 attempt could theoretically land on EXTREME,
        // but we verify the result type carries useful information in both cases
        val result = HodokuPuzzleGenerator.generate(DifficultyType.EXTREME, maxAttempts = 1)

        when (result) {
            is GenerationResult.BestEffort -> {
                assertEquals(1, result.attempts)
                assertTrue(result.best.rating.score >= 0)
            }
            is GenerationResult.Success -> {
                // Unlikely but valid
                assertEquals(DifficultyType.EXTREME, result.puzzle.rating.difficulty)
            }
        }
    }
}

