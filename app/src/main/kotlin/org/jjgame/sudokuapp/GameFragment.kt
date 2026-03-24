package org.jjgame.sudokuapp

import android.content.res.ColorStateList
import android.os.Bundle
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
import org.jjgame.sudoku.Difficulty

class GameFragment : Fragment() {

    private lateinit var boardView: SudokuBoardView
    private lateinit var findCandidates: Button
    private lateinit var editCandidates: AppCompatImageButton
    private lateinit var statusText: TextView
    private lateinit var btnHome: Button
    private lateinit var gameViewModel: SudokuGameViewModel

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

        init()

        btnHome.setOnClickListener {
            gameViewModel.persistGameSession()
            parentFragmentManager.popBackStack()
        }

        findCandidates.setOnClickListener {
            gameViewModel.gameSession.fastFillCandidate()
            gameViewModel.persistGameSession()
            updateCandidatesUi()
            boardView.invalidate()
        }

        editCandidates.setOnClickListener {
            editCandidates.isSelected = !editCandidates.isSelected
            updateEditCandidateToggleUi()
            boardView.setEditCandidateMode(editCandidates.isSelected)
        }

        // Digit buttons 1–9
        val digitIds = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
        )
        digitIds.forEachIndexed { index, id ->
            view.findViewById<Button>(id).setOnClickListener {
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
            checkWin()
        }
        boardView.setEditCandidateMode(editCandidates.isSelected)
        updateCandidatesUi()
        updateEditCandidateToggleUi()
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

    private fun checkWin() {
        if (!boardView.isSolved()) return
        AlertDialog.Builder(requireContext())
            .setTitle("🎉 You Win!")
            .setMessage("Congratulations, you solved the puzzle!")
            .setPositiveButton("New Game") { _, _ ->
                gameViewModel.startGame()
                init()
            }
            .setNegativeButton("Home") { _, _ ->
                gameViewModel.persistGameSession()
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .show()
    }
}


