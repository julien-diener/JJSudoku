package org.jjgame.hodoku.api

import generator.SudokuGeneratorFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import sudoku.Sudoku2

class GeneratorApiContractTest {
    @Test
    fun uniqueness_counter_and_validity_detect_invalid_grid() {
        val generator = SudokuGeneratorFactory.getInstance()
        try {
            val invalid = Sudoku2().apply {
                setSudoku("11" + ".".repeat(79), true)
            }

            val count = generator.getNumberOfSolutions(invalid, 2)

            assertEquals(0, count)
            assertTrue(!generator.validSolution(invalid))
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }
    }

    @Test
    fun generated_sudoku_is_non_null_and_unique() {
        val generator = SudokuGeneratorFactory.getInstance()
        try {
            val puzzle = generator.generateSudoku(false)

            assertNotNull(puzzle)
            assertTrue(puzzle.fixedCellsAnz >= 17)

            val numberOfSolutions = generator.getNumberOfSolutions(puzzle, 1)
            assertEquals(1, numberOfSolutions)
        } finally {
            SudokuGeneratorFactory.giveBack(generator)
        }
    }
}


