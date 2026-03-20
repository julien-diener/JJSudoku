package org.jjgame.sudoku

data class CellIndex(
    val value: Int,
) {
    constructor(row: Int, column: Int) : this(toIndex(row, column))

    init {
        require(value in 0..80) { "value must be between 0 and 80" }
    }

    fun row(): Int = value / 9

    fun column(): Int = value % 9

    companion object {
        private fun toIndex(row: Int, column: Int): Int {
            require(row in 0..8) { "row must be between 0 and 8" }
            require(column in 0..8) { "column must be between 0 and 8" }
            return row * 9 + column
        }
    }
}

/**
 * One Sudoku cell.
 */
data class SudokuCell(
    val value: Int,
    val state: CellState,
    val candidateValues: BooleanArray = BooleanArray(9) { true },
) {
    init {
        require(value in 0..9) { "value must be between 0 and 9" }
        require(candidateValues.size == 9) {
            "candidateValues must contain 9 flags"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuCell

        if (value != other.value) return false
        if (state != other.state) return false
        if (!candidateValues.contentEquals(other.candidateValues)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + state.hashCode()
        result = 31 * result + candidateValues.contentHashCode()
        return result
    }
}

enum class CellState {
    GIVEN,
    NOT_FOUND,
    FOUND,

    ;

    fun isFixed(): Boolean = this != NOT_FOUND
}

