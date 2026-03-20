package org.jjgame.sudoku

sealed interface SetCellValueResult {
    data class Success(val puzzle: SudokuPuzzle) : SetCellValueResult
    data object CellAlreadyFixed : SetCellValueResult
    data class Error(val reason: ErrorReason) : SetCellValueResult
}

enum class ErrorReason {
    OUT_OF_RANGE,
    WRONG_VALUE,
}

/**
 * 9x9 Sudoku puzzle where 0 means empty.
 */
data class SudokuPuzzle(
    val cells: Array<SudokuCell>,
) {

    init {
        require(cells.size == 81) { "cells must have 81 values" }
    }

    fun cellAt(index: CellIndex): SudokuCell = cells[index.value]

    fun switchCandidate(index: CellIndex, value: Int): SudokuPuzzle {
        require(value in 1..9) { "value must be between 1 and 9" }

        val currentCell = cellAt(index)
        if (currentCell.state.isFixed()) {
            return this
        }

        val updatedCandidateValues = currentCell.candidateValues.copyOf()
        updatedCandidateValues[value - 1] = !updatedCandidateValues[value - 1]

        val updatedCells = cells.copyOf()
        updatedCells[index.value] = currentCell.copy(candidateValues = updatedCandidateValues)
        return copy(cells = updatedCells)
    }

    fun setValue(index: CellIndex, value: Int): SetCellValueResult {
        if (value !in 1..9) {
            return SetCellValueResult.Error(ErrorReason.OUT_OF_RANGE)
        }

        val currentCell = cellAt(index)
        if (currentCell.state.isFixed()) {
            return SetCellValueResult.CellAlreadyFixed
        }
        if (value != currentCell.value) {
            return SetCellValueResult.Error(ErrorReason.WRONG_VALUE)
        }

        val updatedCells = cells.copyOf()
        updatedCells[index.value] = currentCell.copy(state = CellState.FOUND)
        return SetCellValueResult.Success(copy(cells = updatedCells))
    }

    fun asPrettyString(): String = buildString {
        for (row in 0 until 9) {
            if (row != 0 && row % 3 == 0) {
                appendLine("------+-------+------")
            }
            for (col in 0 until 9) {
                if (col != 0 && col % 3 == 0) {
                    append("| ")
                }
                val value = cellAt(CellIndex(row, col)).value
                append(if (value == 0) ". " else "$value ")
            }
            appendLine()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuPuzzle

        if (!cells.contentEquals(other.cells)) return false

        return true
    }

    override fun hashCode(): Int = cells.contentHashCode()
}

