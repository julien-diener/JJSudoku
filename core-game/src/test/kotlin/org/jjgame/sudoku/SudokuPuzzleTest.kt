package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SudokuPuzzleTest {

    @Test
    fun `setValue returns updated puzzle with FOUND state when value is correct`() {
        val index = CellIndex(0, 0)
        val puzzle = puzzleWithEditableCell(index = index, value = 5)

        val result = puzzle.setValue(index, 5)
        val success = assertIs<SetCellValueResult.Success>(result)
        val updated = success.puzzle

        assertEquals(CellState.FOUND, updated.cellAt(index).state)
        assertEquals(5, updated.cellAt(index).value)
        assertEquals(CellState.NOT_FOUND, puzzle.cellAt(index).state)
    }

    @Test
    fun `setValue returns error when value is incorrect`() {
        val index = CellIndex(1, 1)
        val puzzle = puzzleWithEditableCell(index = index, value = 7)

        val result = puzzle.setValue(index, 3)

        val error = assertIs<SetCellValueResult.Error>(result)
        assertEquals(ErrorReason.WRONG_VALUE, error.reason)
    }

    @Test
    fun `setValue returns already fixed when cell is fixed`() {
        val index = CellIndex(2, 2)
        val puzzle = puzzleWithCell(index = index, value = 4, state = CellState.GIVEN)

        val result = puzzle.setValue(index, 4)

        assertEquals(SetCellValueResult.CellAlreadyFixed, result)
    }

    @Test
    fun `setValue returns error when value is outside one to nine`() {
        val index = CellIndex(3, 3)
        val puzzle = puzzleWithEditableCell(index = index, value = 6)

        val tooLow = puzzle.setValue(index, 0)
        val tooHigh = puzzle.setValue(index, 10)

        assertEquals(ErrorReason.OUT_OF_RANGE, assertIs<SetCellValueResult.Error>(tooLow).reason)
        assertEquals(ErrorReason.OUT_OF_RANGE, assertIs<SetCellValueResult.Error>(tooHigh).reason)
    }

    @Test
    fun `remainingCountsByValue counts only NOT_FOUND cells per digit`() {
        val cells = Array(81) {
            SudokuCell(value = 1, state = CellState.GIVEN)
        }
        cells[0] = SudokuCell(value = 1, state = CellState.NOT_FOUND)
        cells[1] = SudokuCell(value = 1, state = CellState.NOT_FOUND)
        cells[2] = SudokuCell(value = 2, state = CellState.NOT_FOUND)
        cells[3] = SudokuCell(value = 2, state = CellState.FOUND)

        val counts = SudokuPuzzle(cells).remainingCountsByValue()

        assertEquals(2, counts[0])
        assertEquals(1, counts[1])
        assertEquals(0, counts[2])
        assertEquals(9, counts.size)
    }

    @Test
    fun `remainingCountsByValue is updated after successful setValue`() {
        val index = CellIndex(0, 0)
        val puzzle = puzzleWithEditableCell(index = index, value = 5)

        val before = puzzle.remainingCountsByValue()[4]
        val updated = assertIs<SetCellValueResult.Success>(puzzle.setValue(index, 5)).puzzle
        val after = updated.remainingCountsByValue()[4]

        assertEquals(1, before)
        assertEquals(0, after)
    }

    private fun puzzleWithEditableCell(index: CellIndex, value: Int): SudokuPuzzle =
        puzzleWithCell(index = index, value = value, state = CellState.NOT_FOUND)

    private fun puzzleWithCell(index: CellIndex, value: Int, state: CellState): SudokuPuzzle {
        val cells = Array(81) {
            SudokuCell(value = 1, state = CellState.GIVEN)
        }
        cells[index.value] = SudokuCell(value = value, state = state)
        return SudokuPuzzle(cells)
    }
}
