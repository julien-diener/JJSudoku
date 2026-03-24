package org.jjgame.sudoku

import kotlin.random.Random

enum class Difficulty(val emptyCells: Int) {
    EASY(36),
    MEDIUM(50),
    HARD(60),
}

object SudokuGenerator {
    fun generate(difficulty: Difficulty = Difficulty.EASY, random: Random = Random.Default): SudokuPuzzle {
        val targetEmptyCells = difficulty.emptyCells
        repeat(MAX_GENERATION_ATTEMPTS) {
            val solved = shuffledSolvedBoard(random)
            val clues = solved.copyOf()
            var removed = 0

            for (pos in (0 until 81).shuffled(random)) {
                if (removed == targetEmptyCells) break

                val previous = clues[pos]
                clues[pos] = 0

                if (SudokuSolutionCounter.countSolutions(clues, limit = 2) == 1) {
                    removed++
                } else {
                    clues[pos] = previous
                }
            }

            if (removed == targetEmptyCells) {
                return SudokuPuzzle(Array(81) { index ->
                    SudokuCell(
                        value = solved[index],
                        state = if (clues[index] == 0) CellState.NOT_FOUND else CellState.GIVEN,
                    )
                })
            }
        }

        error("Could not generate a unique puzzle for $difficulty after $MAX_GENERATION_ATTEMPTS attempts")
    }

    private fun shuffledSolvedBoard(random: Random): IntArray {
        val board = IntArray(81)

        fun base(row: Int, col: Int): Int = (row * 3 + row / 3 + col) % 9

        val rowGroups = listOf(0, 1, 2).shuffled(random)
        val colGroups = listOf(0, 1, 2).shuffled(random)
        val rows = rowGroups.flatMap { g -> listOf(0, 1, 2).shuffled(random).map { g * 3 + it } }
        val cols = colGroups.flatMap { g -> listOf(0, 1, 2).shuffled(random).map { g * 3 + it } }
        val nums = (1..9).shuffled(random)

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val value = nums[base(rows[r], cols[c])]
                board[r * 9 + c] = value
            }
        }

        return board
    }

    private const val MAX_GENERATION_ATTEMPTS = 40
}

