package org.jjgame.sudoku

import kotlin.random.Random

enum class Difficulty(val emptyCells: Int) {
    EASY(36),
    MEDIUM(46),
    HARD(54),
}

object SudokuGenerator {
    fun generate(difficulty: Difficulty = Difficulty.EASY, random: Random = Random.Default): SudokuPuzzle {
        val solved = shuffledSolvedBoard(random)
        val puzzle = solved.copyOf()
        val given = BooleanArray(81) { true }

        val positions = (0 until 81).shuffled(random)
        repeat(difficulty.emptyCells) { index ->
            val pos = positions[index]
            given[pos] = false
        }

        return SudokuPuzzle(Array(81) { index ->
            SudokuCell(
                value = puzzle[index],
                state = if (given[index]) CellState.GIVEN else CellState.NOT_FOUND,
            )
        })
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
}

