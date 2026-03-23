package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SudokuCandidateTest {
    @Test
    fun `default constructor initializes all candidates as disabled`() {
        val candidates = SudokuCandidate()

        assertEquals(false, candidates.valuesAt(CellIndex(0, 0)).any { it })
        assertEquals(false, candidates.valuesAt(CellIndex(8, 8)).any { it })
    }

    @Test
    fun `switchCandidate toggles candidate value and keeps original unchanged`() {
        val index = CellIndex(0, 0)
        val candidates = SudokuCandidate()

        val toggledOn = candidates.switchCandidate(index, 3)
        val toggledOffAgain = toggledOn.switchCandidate(index, 3)

        assertEquals(true, toggledOn.valuesAt(index)[2])
        assertEquals(false, toggledOffAgain.valuesAt(index)[2])
        assertEquals(false, candidates.valuesAt(index)[2])
    }

    @Test
    fun `switchCandidate rejects values outside one to nine`() {
        val candidates = SudokuCandidate()
        val index = CellIndex(1, 1)

        val tooLow = runCatching { candidates.switchCandidate(index, 0) }.exceptionOrNull()
        val tooHigh = runCatching { candidates.switchCandidate(index, 10) }.exceptionOrNull()

        assertTrue(tooLow is IllegalArgumentException)
        assertTrue(tooHigh is IllegalArgumentException)
    }

    @Test
    fun `removeCandidateFrom clears value in row column and box only`() {
        val index = CellIndex(4, 4)
        val candidates = SudokuCandidate.allEnabled()

        val updated = candidates.removeCandidateFrom(index, 5)

        // Same row
        assertEquals(false, updated.valuesAt(CellIndex(4, 0))[4])
        // Same column
        assertEquals(false, updated.valuesAt(CellIndex(0, 4))[4])
        // Same box
        assertEquals(false, updated.valuesAt(CellIndex(3, 3))[4])
        // Outside row, column and box stays unchanged
        assertEquals(true, updated.valuesAt(CellIndex(0, 0))[4])
    }

    @Test
    fun `removeCandidateFrom rejects values outside one to nine`() {
        val candidates = SudokuCandidate()
        val index = CellIndex(1, 1)

        val tooLow = runCatching { candidates.removeCandidateFrom(index, 0) }.exceptionOrNull()
        val tooHigh = runCatching { candidates.removeCandidateFrom(index, 10) }.exceptionOrNull()

        assertTrue(tooLow is IllegalArgumentException)
        assertTrue(tooHigh is IllegalArgumentException)
    }
}

