package org.jjgame.sudoku

fun main() {
    val puzzle = SudokuGenerator.generate(Difficulty.EASY)
    println("Generated Sudoku (easy):")
    println(puzzle.asPrettyString())
}

