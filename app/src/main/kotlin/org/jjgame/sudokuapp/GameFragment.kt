package org.jjgame.sudokuapp

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class GameFragment : Fragment() {

    private lateinit var boardView: SudokuBoardView
    private lateinit var findCandidates: Button
    private lateinit var editCandidates: AppCompatImageButton
    private lateinit var statusText: TextView
    private lateinit var btnHome: Button
    private lateinit var gameViewModel: SudokuGameViewModel
    private lateinit var digitButtons: List<Button>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardView = view.findViewById(R.id.boardView)
        findCandidates = view.findViewById(R.id.btnFindCandidates)
        editCandidates = view.findViewById(R.id.btnToggleEditCandidates)
        statusText = view.findViewById(R.id.statusText)
        btnHome = view.findViewById(R.id.btnHome)
        gameViewModel = ViewModelProvider(requireActivity())[SudokuGameViewModel::class.java]

        val digitIds = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
        )
        digitButtons = digitIds.map { id -> view.findViewById(id) }

        init()

        btnHome.setOnClickListener {
            gameViewModel.persistGameSession()
            parentFragmentManager.popBackStack()
        }

        findCandidates.setOnClickListener {
            gameViewModel.gameSession.fastFillCandidate()
            gameViewModel.persistGameSession()
            updateCandidatesUi()
            updateNumberButtonsUi()
            boardView.invalidate()
        }

        editCandidates.setOnClickListener {
            editCandidates.isSelected = !editCandidates.isSelected
            updateEditCandidateToggleUi()
            boardView.setEditCandidateMode(editCandidates.isSelected)
        }

        digitButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                boardView.selectNumber(index + 1)
            }
        }

        view.findViewById<Button>(R.id.btnClear).setOnClickListener {
            boardView.clearSelection()
        }
    }

    private fun init() {
        boardView.init(gameViewModel.gameSession) {
            gameViewModel.persistGameSession()
            updateNumberButtonsUi()
            checkWin()
        }
        boardView.setEditCandidateMode(editCandidates.isSelected)
        updateCandidatesUi()
        updateEditCandidateToggleUi()
        updateNumberButtonsUi()
        statusText.text = gameViewModel.currentDifficulty.name.lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    private fun updateCandidatesUi() {
        editCandidates.isEnabled = true
        boardView.setEditCandidateMode(editCandidates.isSelected)
    }

    private fun updateEditCandidateToggleUi() {
        val active = editCandidates.isSelected
        editCandidates.imageTintList = ColorStateList.valueOf(
            if (active) "#FB8C00".toColorInt() else "#555555".toColorInt(),
        )
        editCandidates.alpha = if (active) 1f else 0.7f
    }

    private fun updateNumberButtonsUi() {
        val remainingByValue = gameViewModel.gameSession.puzzle.remainingCountsByValue()

        digitButtons.forEachIndexed { index, button ->
            val value = index + 1
            val remaining = remainingByValue[index]
            button.isAllCaps = false
            button.text = formatDigitButtonText(value, remaining)
            // Keep the slot occupied so other buttons do not shift when one is finished.
            button.visibility = if (remaining == 0) View.INVISIBLE else View.VISIBLE
        }

        autoSelectNextUnfinishedIfCurrentIsDone(remainingByValue)
    }

    private fun formatDigitButtonText(value: Int, remaining: Int): CharSequence {
        val text = "$value\n$remaining"
        val spannable = SpannableString(text)
        val secondLineStart = text.indexOf('\n') + 1
        val end = text.length

        spannable.setSpan(
            ForegroundColorSpan("#666666".toColorInt()),
            secondLineStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            StyleSpan(Typeface.NORMAL),
            secondLineStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            RelativeSizeSpan(0.78f),
            secondLineStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        return spannable
    }

    private fun autoSelectNextUnfinishedIfCurrentIsDone(remainingByValue: IntArray) {
        val selected = gameViewModel.gameSession.selectedNumber ?: return
        if (remainingByValue[selected - 1] > 0) return

        // Continue from selected+1, wrap after 9, and pick the first unfinished value.
        val next = (1..9)
            .map { step -> ((selected - 1 + step) % 9) + 1 }
            .firstOrNull { value -> remainingByValue[value - 1] > 0 }

        if (next == null) {
            boardView.clearSelection()
        } else {
            boardView.selectNumber(next)
        }
    }

    private fun checkWin() {
        if (!boardView.isSolved()) return
        gameViewModel.clearSavedGame()
        AlertDialog.Builder(requireContext())
            .setTitle("🎉 You Win!")
            .setMessage("Congratulations, you solved the puzzle!")
            .setPositiveButton("New Game") { _, _ ->
                gameViewModel.startGame()
                init()
            }
            .setNegativeButton("Home") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .show()
    }
}
