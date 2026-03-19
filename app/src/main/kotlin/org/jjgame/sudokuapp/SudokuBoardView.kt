package org.jjgame.sudokuapp

import android.content.Context
import androidx.core.graphics.toColorInt
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.jjgame.sudoku.CellIndex
import org.jjgame.sudoku.CellState
import org.jjgame.sudoku.SudokuGameSession

class SudokuBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    // ── State ─────────────────────────────────────────────────────────────────
    var gameSession: SudokuGameSession? = null
        set(value) {
            field = value
            invalidate()
        }

    var onSessionChanged: (() -> Unit)? = null

    // ── Paints ────────────────────────────────────────────────────────────────
    private val paintBackground = Paint().apply { color = Color.WHITE }
    private val paintSelected   = Paint().apply { color = "#ADD8E6".toColorInt() }  // light blue
    private val paintError      = Paint().apply { color = "#FFCDD2".toColorInt() }  // light red

    private val paintThinLine = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    private val paintThickLine = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val paintGivenDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }
    private val paintUserDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#1565C0".toColorInt()  // dark blue
        textAlign = Paint.Align.CENTER
    }
    private val paintErrorDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#B71C1C".toColorInt()  // dark red
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // ── Sizing ────────────────────────────────────────────────────────────────
    private var cellSize = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Always square: use the smaller of the two dimensions
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        cellSize = w / 9f
        val textSize = cellSize * 0.55f
        paintGivenDigit.textSize = textSize
        paintUserDigit.textSize  = textSize
        paintErrorDigit.textSize = textSize
    }

    // ── Drawing ───────────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val session = gameSession ?: return

        for (renderState in session.parseGridCells()) {
            val row = renderState.index.row()
            val col = renderState.index.column()
            val rect = cellRect(row, col)

            // Cell background
            val bg = if (renderState.errorValue != null) paintError
                     else if (renderState.selected) paintSelected
                     else paintBackground
            canvas.drawRect(rect, bg)

            // Digit
            val value = displayedValue(renderState)
            if (value != 0) {
                val textPaint = when {
                    renderState.errorValue != null -> paintErrorDigit
                    renderState.cell.state == CellState.FOUND -> paintUserDigit
                    renderState.cell.state == CellState.GIVEN -> paintGivenDigit
                    else -> paintErrorDigit // should not happen
                }
                val x = rect.left + cellSize / 2f
                val y = rect.top + cellSize / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(value.toString(), x, y, textPaint)
            }
        }

        // Grid lines
        for (i in 0..9) {
            val linePaint = if (i % 3 == 0) paintThickLine else paintThinLine
            val pos = i * cellSize
            canvas.drawLine(pos, 0f, pos, 9 * cellSize, linePaint)
            canvas.drawLine(0f, pos, 9 * cellSize, pos, linePaint)
        }
    }

    // ── Touch ─────────────────────────────────────────────────────────────────
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN) return false
        val session = gameSession ?: return false
        val col = (event.x / cellSize).toInt().coerceIn(0, 8)
        val row = (event.y / cellSize).toInt().coerceIn(0, 8)
        val idx = CellIndex(row, col)
        session.clickCell(idx)
        performClick()
        onSessionChanged?.invoke()
        invalidate()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // ── Public API ────────────────────────────────────────────────────────────
    fun selectNumber(value: Int) {
        gameSession?.selectNumber(value)
        invalidate()
    }

    fun clearSelection() {
        gameSession?.selectedNumber = null
        gameSession?.cellError = null
        invalidate()
    }

    fun isSolved(): Boolean {
        val s = gameSession ?: return false
        return s.cellError == null && s.puzzle.cells.all { it.state.isFixed() && it.value != 0 }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun cellRect(row: Int, col: Int) = RectF(
        col * cellSize, row * cellSize,
        (col + 1) * cellSize, (row + 1) * cellSize,
    )

    private fun displayedValue(renderState: org.jjgame.sudoku.RenderCell): Int {
        renderState.errorValue?.let { return it }
        return when (renderState.cell.state) {
            CellState.NOT_FOUND -> 0
            else -> renderState.cell.value
        }
    }
}

