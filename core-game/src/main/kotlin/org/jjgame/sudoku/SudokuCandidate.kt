package org.jjgame.sudoku

class SudokuCandidate private constructor(
    /**
     * Candidate bitmasks per grid cell index.
     *
     * `masks[cellIndex]` stores the candidate flags for the cell addressed by that `CellIndex`.
     * Bit 0 represents digit 1, bit 1 digit 2, ... , bit 8 digit 9.
     * A bit value of 1 means the digit is currently a possible candidate for the cell.
     */
    private val masks: IntArray,
) {

    constructor() : this(IntArray(81) { CANDIDATE_MASK_NONE })

    init {
        require(masks.size == 81) { "masks must have 81 entries" }
        require(masks.all { it and CANDIDATE_MASK_ALL == it }) {
            "masks must only contain bits for values 1..9"
        }
    }

    fun valuesAt(index: CellIndex): BooleanArray = maskToCandidateValues(masks[index.value])

    fun switchCandidate(index: CellIndex, value: Int): SudokuCandidate {
        require(value in 1..9) { "value must be between 1 and 9" }

        val bit = 1 shl (value - 1)
        val updatedMasks = masks.copyOf()
        updatedMasks[index.value] = updatedMasks[index.value] xor bit
        return SudokuCandidate(updatedMasks)
    }

    fun removeCandidateFrom(index: CellIndex, value: Int): SudokuCandidate {
        require(value in 1..9) { "value must be between 1 and 9" }

        val bit = 1 shl (value - 1)
        val clearMask = bit.inv()
        val updatedMasks = masks.copyOf()

        val row = index.row()
        val col = index.column()

        // Clear the digit in the full row and full column.
        for (i in 0 until 9) {
            updatedMasks[CellIndex(row, i).value] = updatedMasks[CellIndex(row, i).value] and clearMask
            updatedMasks[CellIndex(i, col).value] = updatedMasks[CellIndex(i, col).value] and clearMask
        }

        // Clear the digit in the 3x3 box containing the index.
        val boxRowStart = (row / 3) * 3
        val boxColStart = (col / 3) * 3
        for (r in boxRowStart until boxRowStart + 3) {
            for (c in boxColStart until boxColStart + 3) {
                val idx = CellIndex(r, c).value
                updatedMasks[idx] = updatedMasks[idx] and clearMask
            }
        }

        return SudokuCandidate(updatedMasks)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuCandidate

        return masks.contentEquals(other.masks)
    }

    override fun hashCode(): Int = masks.contentHashCode()

    companion object {
        fun allEnabled(): SudokuCandidate = SudokuCandidate(IntArray(81) { CANDIDATE_MASK_ALL })

        private const val CANDIDATE_MASK_ALL = 0x1FF
        private const val CANDIDATE_MASK_NONE = 0x000

        private fun maskToCandidateValues(mask: Int): BooleanArray = BooleanArray(9) { index ->
            (mask and (1 shl index)) != 0
        }
    }
}

