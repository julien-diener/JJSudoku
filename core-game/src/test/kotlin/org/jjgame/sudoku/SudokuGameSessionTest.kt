package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SudokuGameSessionTest {
    @Test
    fun `numberSelection has nine visible cells from one to nine`() {
        val controller = SudokuGameSession(puzzle = puzzleWithSingleEditableCell(CellIndex(0, 0), 5))

        assertEquals(9, controller.numberSelection.size)
        controller.numberSelection.forEachIndexed { index, cell ->
            assertEquals(index + 1, cell.value)
            assertEquals(CellState.GIVEN, cell.state)
        }
    }

    @Test
    fun `clickCell on fixed cell selects its number and clears error`() {
        val fixedIndex = CellIndex(0, 0)
        val puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(1, 1), editableValue = 4)
        val controller = SudokuGameSession(puzzle = puzzle)
        controller.cellError = CellError(CellIndex(2, 2), 3)

        controller.clickCell(fixedIndex)

        assertEquals(puzzle.cellAt(fixedIndex).value, controller.selectedNumber)
        assertNull(controller.cellError)
    }

    @Test
    fun `clickCell updates puzzle when editable cell and selected number are correct`() {
        val editableIndex = CellIndex(3, 3)
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 8),
        )
        controller.selectNumber(8)
        controller.cellError = CellError(CellIndex(0, 0), 1)

        controller.clickCell(editableIndex)

        assertEquals(8, controller.selectedNumber)
        assertNull(controller.cellError)
        assertEquals(CellState.FOUND, controller.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `clickCell sets cellError and unsets selected number when value is incorrect`() {
        val editableIndex = CellIndex(4, 4)
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 6),
        )
        controller.selectNumber(2)

        controller.clickCell(editableIndex)

        assertNull(controller.selectedNumber)
        val error = assertNotNull(controller.cellError)
        assertEquals(editableIndex, error.index)
        assertEquals(2, error.value)
        assertEquals(CellState.NOT_FOUND, controller.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `clickCell does nothing on editable cell when no selected number`() {
        val editableIndex = CellIndex(5, 5)
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = editableIndex, editableValue = 9),
        )

        controller.clickCell(editableIndex)

        assertNull(controller.selectedNumber)
        assertNull(controller.cellError)
        assertEquals(CellState.NOT_FOUND, controller.puzzle.cellAt(editableIndex).state)
    }

    @Test
    fun `selectNumber sets selected number and clears existing error`() {
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(6, 6), editableValue = 7),
        )
        controller.cellError = CellError(CellIndex(0, 0), 4)

        controller.selectNumber(7)

        assertEquals(7, controller.selectedNumber)
        assertNull(controller.cellError)
    }

    @Test
    fun `selectNumber rejects values outside one to nine`() {
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(7, 7), editableValue = 3),
        )

        val thrownLow = runCatching { controller.selectNumber(0) }.exceptionOrNull()
        val thrownHigh = runCatching { controller.selectNumber(10) }.exceptionOrNull()

        assertTrue(thrownLow is IllegalArgumentException)
        assertTrue(thrownHigh is IllegalArgumentException)
    }

    @Test
    fun `parseGridCells returns all 81 cells with correct indices`() {
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(0, 0), editableValue = 5),
        )

        controller.selectNumber(5);

        val cells = controller.parseGridCells()

        assertEquals(81, cells.size)
        for (idx in 0 until 81) {
            val render = cells[idx]
            val cell = controller.puzzle.cellAt(render.index)
            assertEquals(idx, render.index.value)
            assertEquals(cell, render.cell)
            assertEquals(cell.value == 5 && cell.state.isFixed(), render.selected)
        }
    }

    @Test
    fun `parseGridCells exposes error value for error cell`() {
        val errorIndex = CellIndex(1, 1)
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = errorIndex, editableValue = 9),
        )
        controller.selectNumber(3)
        controller.clickCell(errorIndex)

        val cells = controller.parseGridCells()

        val errorCell = cells.find { it.index == errorIndex }
        assertNotNull(errorCell)
        assertEquals(CellState.NOT_FOUND, errorCell.cell.state)
        assertEquals(3, errorCell.errorValue)
    }

    @Test
    fun `parseNumberSelectionCell returns correct render state`() {
        val controller = SudokuGameSession(
            puzzle = puzzleWithSingleEditableCell(editableIndex = CellIndex(2, 2), editableValue = 4),
        )

        val rendered = controller.parseNumberSelectionCell(7)

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


