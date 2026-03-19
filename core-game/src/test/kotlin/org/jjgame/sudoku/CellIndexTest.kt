package org.jjgame.sudoku

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CellIndexTest {
    @Test
    fun `constructs index from row and column`() {
        assertEquals(0, CellIndex(0, 0).value)
        assertEquals(24, CellIndex(2, 6).value)
        assertEquals(80, CellIndex(8, 8).value)
    }

    @Test
    fun `rejects row and column outside sudoku bounds`() {
        assertFailsWith<IllegalArgumentException> { CellIndex(-1, 0) }
        assertFailsWith<IllegalArgumentException> { CellIndex(9, 0) }
        assertFailsWith<IllegalArgumentException> { CellIndex(0, -1) }
        assertFailsWith<IllegalArgumentException> { CellIndex(0, 9) }
    }

    @Test
    fun `accepts boundary values`() {
        assertEquals(0, CellIndex(0).value)
        assertEquals(80, CellIndex(80).value)
    }

    @Test
    fun `rejects values outside sudoku bounds`() {
        assertFailsWith<IllegalArgumentException> { CellIndex(-1) }
        assertFailsWith<IllegalArgumentException> { CellIndex(81) }
    }

    @Test
    fun `returns expected row and column`() {
        val topLeft = CellIndex(0)
        assertEquals(0, topLeft.row())
        assertEquals(0, topLeft.column())

        val middle1 = CellIndex(24)
        assertEquals(2, middle1.row())
        assertEquals(6, middle1.column())

        val middle = CellIndex(40)
        assertEquals(4, middle.row())
        assertEquals(4, middle.column())

        val bottomRight = CellIndex(80)
        assertEquals(8, bottomRight.row())
        assertEquals(8, bottomRight.column())
    }
}

