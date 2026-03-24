package org.jjgame.sudoku

data class CellError(
    val index: CellIndex,
    val value: Int,
) {
    init {
        require(value in 1..9) { "value must be between 1 and 9" }
    }
}

data class RenderCell(
    val index: CellIndex,
    val cell: SudokuCell,
    val candidateValues: BooleanArray? = null,
    val selected: Boolean,
    val errorValue: Int? = null,
)

class SudokuGameSession(
    var puzzle: SudokuPuzzle,
    var candidates: SudokuCandidate = SudokuCandidate(),
    /** Epoch milliseconds when the game was first started. */
    val startedAt: Long = System.currentTimeMillis(),
    /** Accumulated play time in seconds (paused when app is backgrounded). */
    var elapsedSeconds: Long = 0L,
    /** Epoch milliseconds when the puzzle was solved, or null if still in progress. */
    var finishedAt: Long? = null,
    /** Total number of wrong value attempts made during the game. */
    var errorCount: Int = 0,
) {
    val numberSelection: List<SudokuCell> = (1..9).map { value ->
        SudokuCell(value = value, state = CellState.GIVEN)
    }

    var selectedNumber: Int? = null
        set(value) {
            require(value == null || value in 1..9) { "selectedNumber must be null or between 1 and 9" }
            field = value
        }

    var cellError: CellError? = null

    fun clickCell(index: CellIndex, editCandidate: Boolean = false) {
        val cell = puzzle.cellAt(index)
        if (cell.state.isFixed()) {
            selectedNumber = cell.value
            cellError = null
            return
        }

        val currentSelectedNumber = selectedNumber ?: return

        if (editCandidate) {
            candidates = candidates.switchCandidate(index, currentSelectedNumber)
            cellError = null
            return
        }

        when (val setValueResult = puzzle.setValue(index, currentSelectedNumber)) {
            is SetCellValueResult.Success -> {
                puzzle = setValueResult.puzzle
                candidates = candidates.removeCandidateFrom(index, currentSelectedNumber)
                cellError = null
            }
            // CellAlreadyFixed is unreachable here: fixed cells are handled by the early return above
            is SetCellValueResult.CellAlreadyFixed,
            is SetCellValueResult.Error,
            -> {
                cellError = CellError(index = index, value = currentSelectedNumber)
                selectedNumber = null
                errorCount++
            }
        }
    }

    fun selectNumber(value: Int) {
        require(value in 1..9) { "value must be between 1 and 9" }
        selectedNumber = value
        cellError = null
    }

    fun parseGridCells(): List<RenderCell> = (0 until 81).map { idx ->
        val index = CellIndex(idx)
        val cell = puzzle.cellAt(index)
        val candidateValues = if (cell.state == CellState.NOT_FOUND) {
            candidates.valuesAt(index)
        } else {
            null
        }
        val error = cellError
        val errorValue = if (error?.index == index) error.value else null
        val selected = cell.state.isFixed() && cell.value == selectedNumber
        RenderCell(
            index = index,
            cell = cell,
            candidateValues = candidateValues,
            selected = selected,
            errorValue = errorValue,
        )
    }

    fun parseNumberSelectionCell(value: Int): RenderCell {
        require(value in 1..9) { "value must be between 1 and 9" }
        val cell = numberSelection[value - 1]
        val index = CellIndex(value)
        return RenderCell(index = index, cell = cell, candidateValues = null, selected = false, errorValue = null)
    }

    fun fastFillCandidate() {
        candidates = puzzle
            .mapCells { index, cell -> index to cell }
            .fold(SudokuCandidate.allEnabled()) { acc, (index, cell) ->
                if (cell.state.isFixed()) acc.removeCandidateFrom(index, cell.value) else acc
            }
    }
}

