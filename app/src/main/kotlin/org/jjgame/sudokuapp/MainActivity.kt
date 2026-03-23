package org.jjgame.sudokuapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import org.jjgame.sudoku.Difficulty

class MainActivity : AppCompatActivity() {

    private lateinit var boardView: SudokuBoardView
    private lateinit var findCandidates: Button
    private lateinit var editCandidates: AppCompatImageButton
    private lateinit var statusText: TextView
    private lateinit var gameViewModel: SudokuGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView  = findViewById(R.id.boardView)
        findCandidates = findViewById(R.id.btnFindCandidates)
        editCandidates = findViewById(R.id.btnToggleEditCandidates)
        statusText = findViewById(R.id.statusText)
        gameViewModel = ViewModelProvider(this)[SudokuGameViewModel::class.java]

        init()

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

        // Digit buttons 1–9 + clear
        val digitIds = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
        )
        digitIds.forEachIndexed { index, id ->
            findViewById<Button>(id).setOnClickListener {
                boardView.selectNumber(index + 1)
                checkWin()
            }
        }
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            boardView.clearSelection()
        }

        // Difficulty buttons
        findViewById<Button>(R.id.btnEasy).setOnClickListener   { startGame(Difficulty.EASY) }
        findViewById<Button>(R.id.btnMedium).setOnClickListener { startGame(Difficulty.MEDIUM) }
        findViewById<Button>(R.id.btnHard).setOnClickListener   { startGame(Difficulty.HARD) }

        // New game button
        findViewById<Button>(R.id.newGameButton).setOnClickListener {
            startGame(gameViewModel.currentDifficulty)
        }
    }

    private fun startGame(difficulty: Difficulty) {
        gameViewModel.startGame(difficulty)
        init()
    }

    private fun init() {
        boardView.init(gameViewModel.gameSession) { gameViewModel.persistGameSession() }
        boardView.setEditCandidateMode(editCandidates.isSelected)
        updateCandidatesUi()
        updateEditCandidateToggleUi()
        statusText.text = gameViewModel.currentDifficulty.name.lowercase().replaceFirstChar { it.uppercase() }
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
        if (boardView.isSolved()) {
            Toast.makeText(this, "🎉 Solved!", Toast.LENGTH_LONG).show()
        }
    }
}
