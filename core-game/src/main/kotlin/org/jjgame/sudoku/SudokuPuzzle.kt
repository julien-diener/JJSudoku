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
class SudokuPuzzle private constructor(
    private val values: IntArray,
    private val states: ByteArray,
) {

    constructor(cells: Array<SudokuCell>) : this(
        values = IntArray(cells.size) { index -> cells[index].value },
        states = ByteArray(cells.size) { index -> stateToCode(cells[index].state) },
    )

    init {
        // note: apart from the array sizes, those are guarantied by the public constructor
        // and internal call to private constructor. We keep them for safe guard only
        require(values.size == 81) { "values must have 81 entries" }
        require(states.size == 81) { "states must have 81 entries" }
        require(values.all { it in 0..9 }) { "values must be between 0 and 9" }
        require(states.all { code -> codeToState(code) != null }) { "states contain invalid values" }
    }

    fun cellAt(index: CellIndex): SudokuCell = SudokuCell(
        value = values[index.value],
        state = checkNotNull(codeToState(states[index.value])),
    )

    fun <T> mapCells(transform: (index: CellIndex, cell: SudokuCell) -> T): List<T> =
        (0 until 81).map { idx ->
            val index = CellIndex(idx)
            transform(index, cellAt(index))
        }

    fun isSolved(): Boolean = states.all { code ->
        checkNotNull(codeToState(code)).isFixed()
    }

    /**
     * Returns remaining unresolved cells per digit.
     * Index 0 is digit 1, index 8 is digit 9.
     */
    fun remainingCountsByValue(): IntArray {
        val counts = IntArray(9)
        for (idx in states.indices) {
            if (checkNotNull(codeToState(states[idx])) != CellState.NOT_FOUND) continue
            val value = values[idx]
            if (value in 1..9) {
                counts[value - 1] += 1
            }
        }
        return counts
    }

    fun setValue(index: CellIndex, value: Int): SetCellValueResult {
        if (value !in 1..9) {
            return SetCellValueResult.Error(ErrorReason.OUT_OF_RANGE)
        }

        val state = checkNotNull(codeToState(states[index.value]))
        if (state.isFixed()) {
            return SetCellValueResult.CellAlreadyFixed
        }
        if (value != values[index.value]) {
            return SetCellValueResult.Error(ErrorReason.WRONG_VALUE)
        }

        val updatedStates = states.copyOf()
        updatedStates[index.value] = stateToCode(CellState.FOUND)
        return SetCellValueResult.Success(SudokuPuzzle(values, updatedStates))
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
                val value = values[CellIndex(row, col).value]
                append(if (value == 0) ". " else "$value ")
            }
            appendLine()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuPuzzle

        if (!values.contentEquals(other.values)) return false
        if (!states.contentEquals(other.states)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = values.contentHashCode()
        result = 31 * result + states.contentHashCode()
        return result
    }

    companion object {
        private fun stateToCode(state: CellState): Byte = when (state) {
            CellState.GIVEN -> 0
            CellState.NOT_FOUND -> 1
            CellState.FOUND -> 2
        }

        private fun codeToState(code: Byte): CellState? = when (code.toInt()) {
            0 -> CellState.GIVEN
            1 -> CellState.NOT_FOUND
            2 -> CellState.FOUND
            else -> null // todo: don't like this null. how to avoid it?
        }
    }
}
