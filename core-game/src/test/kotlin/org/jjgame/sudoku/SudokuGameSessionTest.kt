package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SudokuGameSessionTest {
    @Test
    fun `numberSelection has nine visible cells from one to nine`() {
        val game = SudokuGameSession(puzzle = puzzleWithSingleEditableCell(CellIndex(0, 0), 5))

        assertEquals(9, game.numberSelection.size)
        game.numberSelection.forEachIndexed { index, cell ->
            assertEquals(index + 1, cell.value)
            assertEquals(CellState.GIVEN, cell.state)
        }
    }

    @Test
    fun `clickCell on fixed cell selects its number and clears error`() {
        val fixedIndex = CellIndex(0, 0)
        val puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(1, 1), editableValue = 4)
        val game = SudokuGameSession(puzzle = puzzle)
        game.cellError = CellError(CellIndex(2, 2), 3)

        game.clickCell(fixedIndex)

        assertEquals(puzzle.cellAt(fixedIndex).value, game.selectedNumber)
        assertNull(game.cellError)
    }

    @Test
    fun `clickCell updates puzzle when editable cell and selected number are correct`() {
        val editableIndex = CellIndex(3, 3)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 8),
        )
        game.selectNumber(8)
        game.cellError = CellError(CellIndex(0, 0), 1)

        game.clickCell(editableIndex)

        assertEquals(8, game.selectedNumber)
        assertNull(game.cellError)
        assertEquals(CellState.FOUND, game.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `clickCell in candidate mode toggles candidate and keeps selected number`() {
        val editableIndex = CellIndex(3, 4)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 8),
        )
        game.selectNumber(6)
        game.cellError = CellError(CellIndex(0, 0), 1)

        game.clickCell(editableIndex, editCandidate = true)

        assertEquals(6, game.selectedNumber)
        assertNull(game.cellError)
        assertEquals(true, game.candidates.valuesAt(editableIndex)[5])
        assertEquals(CellState.NOT_FOUND, game.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `clickCell clears candidate from peers when value is found`() {
        val editableIndex = CellIndex(4, 4)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 8),
        )
        game.fastFillCandidate()
        game.selectNumber(8)

        game.clickCell(editableIndex)

        assertEquals(false, game.candidates.valuesAt(CellIndex(4, 0))[7])
        assertEquals(false, game.candidates.valuesAt(CellIndex(0, 4))[7])
        assertEquals(false, game.candidates.valuesAt(CellIndex(3, 3))[7])
    }

    @Test
    fun `clickCell sets cellError and unsets selected number when value is incorrect`() {
        val editableIndex = CellIndex(4, 4)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 6),
        )
        game.selectNumber(2)

        game.clickCell(editableIndex)

        assertNull(game.selectedNumber)
        val error = assertNotNull(game.cellError)
        assertEquals(editableIndex, error.index)
        assertEquals(2, error.value)
        assertEquals(CellState.NOT_FOUND, game.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `clickCell does nothing on editable cell when no selected number`() {
        val editableIndex = CellIndex(5, 5)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 9),
        )

        game.clickCell(editableIndex)

        assertNull(game.selectedNumber)
        assertNull(game.cellError)
        assertEquals(CellState.NOT_FOUND, game.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `clickCell in candidate mode does nothing when no selected number`() {
        val editableIndex = CellIndex(5, 6)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 9),
        )
        val before = game.puzzle

        game.clickCell(editableIndex, editCandidate = true)

        assertEquals(before, game.puzzle)
        assertNull(game.selectedNumber)
        assertNull(game.cellError)
    }

    @Test
    fun `fastFillCandidate initializes all candidates then removes fixed peers`() {
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(4, 4), editableValue = 7),
        )

        game.fastFillCandidate()

        assertEquals(false, game.candidates.valuesAt(CellIndex(0, 1))[0])
        assertEquals(false, game.candidates.valuesAt(CellIndex(1, 0))[0])
        assertEquals(false, game.candidates.valuesAt(CellIndex(1, 1))[0])
        // Value 7 is unaffected by fixed value-1 givens in this fixture.
        assertEquals(true, game.candidates.valuesAt(CellIndex(4, 4))[6])
    }

    @Test
    fun `selectNumber sets selected number and clears existing error`() {
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(6, 6), editableValue = 7),
        )
        game.cellError = CellError(CellIndex(0, 0), 4)

        game.selectNumber(7)

        assertEquals(7, game.selectedNumber)
        assertNull(game.cellError)
    }

    @Test
    fun `selectNumber rejects values outside one to nine`() {
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(7, 7), editableValue = 3),
        )

        val thrownLow = runCatching { game.selectNumber(0) }.exceptionOrNull()
        val thrownHigh = runCatching { game.selectNumber(10) }.exceptionOrNull()

        assertTrue(thrownLow is IllegalArgumentException)
        assertTrue(thrownHigh is IllegalArgumentException)
    }

    @Test
    fun `parseGridCells returns all 81 cells with correct indices`() {
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(0, 0), editableValue = 5),
        )

        game.selectNumber(5)

        val cells = game.parseGridCells()

        assertEquals(81, cells.size)
        for (idx in 0 until 81) {
            val render = cells[idx]
            val cell = game.puzzle.cellAt(render.index)
            assertEquals(idx, render.index.value)
            assertEquals(cell, render.cell)
            assertEquals(cell.value == 5 && cell.state.isFixed(), render.selected)
        }
    }

    @Test
    fun `parseGridCells exposes error value for error cell`() {
        val errorIndex = CellIndex(1, 1)
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = errorIndex, editableValue = 9),
        )
        game.selectNumber(3)
        game.clickCell(errorIndex)

        val cells = game.parseGridCells()

        val errorCell = cells.find { it.index == errorIndex }
        assertNotNull(errorCell)
        assertEquals(CellState.NOT_FOUND, errorCell.cell.state)
        assertEquals(3, errorCell.errorValue)
    }

    @Test
    fun `parseNumberSelectionCell returns correct render state`() {
        val game = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(2, 2), editableValue = 4),
        )

        val rendered = game.parseNumberSelectionCell(7)

        assertEquals(CellState.GIVEN, rendered.cell.state)
        assertEquals(7, rendered.cell.value)
        assertNull(rendered.errorValue)
    }

    private fun puzzleWithSingleEditableCell(editableIndex: CellIndex, editableValue: Int): SudokuPuzzle {
        val cells = Array(81) { SudokuCell(value = 1, state = CellState.GIVEN) }
        cells[editableIndex.value] = SudokuCell(value = editableValue, state = CellState.NOT_FOUND)
        return SudokuPuzzle(cells)
    }
}


