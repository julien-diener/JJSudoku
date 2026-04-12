package org.jjgame.sudokuapp

import android.content.Context
import androidx.core.content.ContextCompat
import android.graphics.Canvas
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
    private var gameSession: SudokuGameSession? = null
    private var onSessionChanged: (() -> Unit)? = null
    private var editCandidateMode = false

    fun init(session: SudokuGameSession?, callback: (() -> Unit)?) {
        gameSession = session
        onSessionChanged = callback
        invalidate()
    }

    // ── Paints ────────────────────────────────────────────────────────────────
    private val paintBackground = Paint().apply { color = ContextCompat.getColor(context, R.color.board_background) }
    private val paintSelected   = Paint().apply { color = ContextCompat.getColor(context, R.color.board_selected_bg) }
    private val paintError      = Paint().apply { color = ContextCompat.getColor(context, R.color.board_error_bg) }

    private val paintThinLine = Paint().apply {
        color = ContextCompat.getColor(context, R.color.board_grid_thin)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    private val paintThickLine = Paint().apply {
        color = ContextCompat.getColor(context, R.color.board_grid_thick)
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val paintGivenDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.digit_given)
        textAlign = Paint.Align.CENTER
    }
    private val paintCandidateDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.candidate_default)
        textAlign = Paint.Align.CENTER
    }
    private val paintCandidateEditDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.candidate_edit)
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val paintUserDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.digit_found)
        textAlign = Paint.Align.CENTER
    }
    private val paintErrorDigit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.digit_error)
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
        paintCandidateDigit.textSize = cellSize * 0.22f
        paintCandidateEditDigit.textSize = cellSize * 0.22f
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

            // Digit / candidates
            when {
                renderState.errorValue != null -> {
                    drawBigDigit(canvas, rect, renderState.errorValue!!, paintErrorDigit)
                }
                renderState.cell.state == CellState.NOT_FOUND ->
                    renderState.candidateValues?.let { candidateValues ->
                        drawCandidateValues(
                            canvas = canvas,
                            rect = rect,
                            candidateValues = candidateValues,
                            selectedValue = session.selectedNumber,
                        )
                }
                else -> {
                    val textPaint = if (renderState.cell.state == CellState.GIVEN) paintGivenDigit
                                    else paintUserDigit
                    drawBigDigit(canvas, rect, renderState.cell.value, textPaint)
                }
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
        session.clickCell(idx, editCandidate = editCandidateMode)
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

    fun setEditCandidateMode(enabled: Boolean) {
        editCandidateMode = enabled
        invalidate()
    }

    fun clearSelection() {
        gameSession?.selectedNumber = null
        gameSession?.cellError = null
        invalidate()
    }

    fun isSolved(): Boolean {
        val s = gameSession ?: return false
        return s.cellError == null && s.puzzle.isSolved()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun cellRect(row: Int, col: Int) = RectF(
        col * cellSize, row * cellSize,
        (col + 1) * cellSize, (row + 1) * cellSize,
    )

    private fun drawBigDigit(canvas: Canvas, rect: RectF, value: Int, paint: Paint) {
        val x = rect.left + cellSize / 2f
        val y = rect.top + cellSize / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(value.toString(), x, y, paint)
    }

    private fun drawCandidateValues(
        canvas: Canvas,
        rect: RectF,
        candidateValues: BooleanArray,
        selectedValue: Int?,
    ) {
        val candidatePaint = if (editCandidateMode) paintCandidateEditDigit else paintCandidateDigit
        val inset = cellSize * 0.05f
        val innerLeft   = rect.left   + inset
        val innerTop    = rect.top    + inset
        val innerWidth  = rect.width()  - inset * 2
        val innerHeight = rect.height() - inset * 2
        val miniCellW = innerWidth  / 3f
        val miniCellH = innerHeight / 3f

        for (i in 0 until 9) {
            if (!candidateValues[i]) continue
            val miniRow = i / 3
            val miniCol = i % 3
            if (selectedValue == i + 1) {
                val miniRect = RectF(
                    innerLeft + miniCol * miniCellW,
                    innerTop + miniRow * miniCellH,
                    innerLeft + (miniCol + 1) * miniCellW,
                    innerTop + (miniRow + 1) * miniCellH,
                )
                canvas.drawRect(miniRect, paintSelected)
            }
            val x = innerLeft + (miniCol + 0.5f) * miniCellW
            val y = innerTop  + (miniRow + 0.5f) * miniCellH -
                    (candidatePaint.descent() + candidatePaint.ascent()) / 2f
            canvas.drawText((i + 1).toString(), x, y, candidatePaint)
        }
    }

}

