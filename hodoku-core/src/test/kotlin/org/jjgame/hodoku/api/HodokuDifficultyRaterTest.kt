package org.jjgame.hodoku.api

import org.jjgame.hodoku.HodokuDifficultyRater
import kotlin.test.Test
import kotlin.test.assertTrue

class HodokuDifficultyRaterTest {
    @Test
    fun rate_returnsSolvedAndNonIncompleteDifficulty_forKnownValidPuzzle() {
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

        assertTrue(rating.solved)
        assertTrue(rating.score >= 0)
    }
}


