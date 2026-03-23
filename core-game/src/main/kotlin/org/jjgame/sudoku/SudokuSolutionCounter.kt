package org.jjgame.sudoku

/**
 * Counts how many valid solutions exist for a Sudoku grid.
 *
 * This object is intentionally focused on *counting* (0 / 1 / 2+ solutions), not on gameplay.
 *
 * It delegates to [BacktrackingSolver] which handles the actual search and maintains mutable state.
 */
internal object SudokuSolutionCounter {
    /**
     * @param clues 81 integers, where 0 means empty and 1..9 means fixed clue.
     * @param limit stop counting as soon as this amount is reached (typically 2 for uniqueness checks).
     */
    fun countSolutions(clues: IntArray, limit: Int = 2): Int {
        require(clues.size == 81) { "clues must have 81 entries" }
        require(limit > 0) { "limit must be greater than zero" }
        require(clues.all { it in 0..9 }) { "clues must contain values between 0 and 9" }

        return BacktrackingSolver(clues).countSolutions(limit)
    }
}

/**
 * Generic Sudoku backtracking solver with **mutable** search state.
 *
 * Maintains constraint masks (rows, cols, boxes) and empty cell list during search.
 * This class is the puzzle-solving core and can be reused by:
 * - solution counters (for uniqueness checks during generation)
 * - hint tools (to find one logical deduction)
 * - explainable solvers (to provide step-by-step explanations)
 *
 * @param clues 81 integers, where 0 means empty and 1..9 means fixed clue.
 */
private class BacktrackingSolver(clues: IntArray) {
    private val cols = IntArray(9)
    // Constraint masks: one integer per row/column/box, where each bit represents
    // whether a digit is already placed. Bit 0 = digit 1, bit 1 = digit 2, ..., bit 8 = digit 9.
    // e.g. if row k (0-based) contains digits 1, 3 and 7, rows[k] = 0b01000101.
    private val rows = IntArray(9)
    private val boxes = IntArray(9)

    // empties holds the grid indices (0..80) of all cells without a known value.
    // Only indices [0, emptyCount) are valid; the rest of the array is unused.
    // emptyCount == -1 is a sentinel meaning the clues themselves are contradictory
    // (same digit twice in a row/col/box), so the puzzle has no solution at all.
    private val empties = IntArray(81)
    private var emptyCount = 0

    init {
        // Initialize constraint masks from clues and collect empty cells.
        for (index in 0 until 81) {
            val value = clues[index]
            if (value == 0) {
                empties[emptyCount++] = index
            } else {
                // Invalid givens (same number already used in row/col/box) => no solution.
                if (!place(index, value)) {
                    emptyCount = -1
                    break
                }
            }
        }
    }

    /**
     * Counts valid solutions, stopping early if [limit] is reached.
     * Returns 0 if the clues are invalid (conflicting constraints).
     */
    fun countSolutions(limit: Int): Int {
        if (emptyCount < 0) return 0
        return countSolutionsDepthFirst(found = 0, limit = limit)
    }

    /**
     * DFS over empty cells, counting valid completions.
     *
     * @param found current number of discovered solutions
     * @param limit stop as soon as this count is reached (for performance)
     * @return number of solutions found (up to [limit])
     */
    private fun countSolutionsDepthFirst(found: Int, limit: Int): Int {
        if (found >= limit) return found
        if (emptyCount == 0) return found + 1

        // Heuristic: pick the most constrained empty cell (fewest candidates).
        // This is the key speedup for both counting and solving.
        val choice = chooseMostConstrainedCell() ?: return found

        // Move chosen cell to the tail so we can work on [0, emptyCount - 1).
        swap(choice.emptyPosition, emptyCount - 1)
        val index = empties[emptyCount - 1]
        emptyCount--  // Logically exclude the chosen cell for recursive call

        var solutions = found
        var remainingMask = choice.candidateMask
        while (remainingMask != 0 && solutions < limit) {
            val bit = remainingMask and -remainingMask
            val value = bitToValue(bit)
            remainingMask = remainingMask xor bit

            place(index, value)
            solutions = countSolutionsDepthFirst(solutions, limit)
            remove(index, value)
        }

        // Restore: increment emptyCount and swap back for caller frame.
        emptyCount++
        swap(choice.emptyPosition, emptyCount - 1)
        return solutions
    }

    /**
     * Data class representing the choice of an empty cell and its available candidates.
     */
    private data class CellChoice(
        val emptyPosition: Int,
        val candidateMask: Int,
    )

    /**
     * Picks the empty cell with the fewest candidates.
     * Returns null when at least one empty cell has zero candidates (dead-end).
     */
    private fun chooseMostConstrainedCell(): CellChoice? {
        var bestPos = -1
        var bestMask = 0
        var bestCandidateCount = Int.MAX_VALUE

        for (pos in 0 until emptyCount) {
            val index = empties[pos]
            val mask = availableMask(index)
            val candidateCount = mask.countOneBits()

            if (candidateCount == 0) return null
            if (candidateCount < bestCandidateCount) {
                bestCandidateCount = candidateCount
                bestMask = mask
                bestPos = pos
                if (candidateCount == 1) break
            }
        }

        return CellChoice(emptyPosition = bestPos, candidateMask = bestMask)
    }

    /**
     * Returns the available candidates for one grid index as a 9-bit mask.
     * Bit 0 => value 1, bit 1 => value 2, ... bit 8 => value 9.
     */
    private fun availableMask(index: Int): Int {
        val row = index / 9
        val col = index % 9
        val used = rows[row] or cols[col] or boxes[boxIndex(row, col)]
        return FULL_MASK and used.inv()
    }

    /**
     * Adds one value to row/col/box masks.
     * @return false when the value is already present (constraint violation), true otherwise.
     */
    private fun place(index: Int, value: Int): Boolean {
        val row = index / 9
        val col = index % 9
        val box = boxIndex(row, col)
        val bit = valueToBit(value)

        val occupied = rows[row] or cols[col] or boxes[box]
        if ((occupied and bit) != 0) return false

        rows[row] = rows[row] or bit
        cols[col] = cols[col] or bit
        boxes[box] = boxes[box] or bit
        return true
    }

    /**
     * Reverts [place] for backtracking.
     */
    private fun remove(index: Int, value: Int) {
        val row = index / 9
        val col = index % 9
        val box = boxIndex(row, col)
        val bit = valueToBit(value)

        rows[row] = rows[row] and bit.inv()
        cols[col] = cols[col] and bit.inv()
        boxes[box] = boxes[box] and bit.inv()
    }

    /**
     * Swaps two positions in the empties array in-place.
     * Used to avoid allocating a new array per recursion level.
     */
    private fun swap(i: Int, j: Int) {
        if (i == j) return
        val tmp = empties[i]
        empties[i] = empties[j]
        empties[j] = tmp
    }

    companion object {
        private fun boxIndex(row: Int, col: Int): Int = (row / 3) * 3 + (col / 3)

        private fun valueToBit(value: Int): Int = 1 shl (value - 1)

        /**
         * Converts a one-hot bit mask to Sudoku value (e.g. 0b001000000 -> 7).
         */
        private fun bitToValue(bit: Int): Int = bit.countTrailingZeroBits() + 1

        private const val FULL_MASK = 0b1_1111_1111
    }
}

