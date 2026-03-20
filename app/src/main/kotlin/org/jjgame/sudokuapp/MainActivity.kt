package org.jjgame.sudokuapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.lifecycle.ViewModelProvider
import org.jjgame.sudoku.Difficulty

class MainActivity : AppCompatActivity() {

    private lateinit var boardView: SudokuBoardView
    private lateinit var editCandidatesToggle: AppCompatImageButton
    private lateinit var statusText: TextView
    private lateinit var gameViewModel: SudokuGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView  = findViewById(R.id.boardView)
        editCandidatesToggle = findViewById(R.id.btnToggleEditCandidates)
        statusText = findViewById(R.id.statusText)
        gameViewModel = ViewModelProvider(this)[SudokuGameViewModel::class.java]

        init()

        editCandidatesToggle.setOnClickListener {
            editCandidatesToggle.isSelected = !editCandidatesToggle.isSelected
            updateEditCandidateToggleUi()
            boardView.setEditCandidateMode(editCandidatesToggle.isSelected)
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
        boardView.init(gameViewModel.gameSession, { gameViewModel.persistGameSession() })
        boardView.setEditCandidateMode(editCandidatesToggle.isSelected)
        updateEditCandidateToggleUi()
        statusText.text = gameViewModel.currentDifficulty.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun updateEditCandidateToggleUi() {
        val active = editCandidatesToggle.isSelected
        editCandidatesToggle.imageTintList = ColorStateList.valueOf(
            if (active) Color.parseColor("#FB8C00") else Color.parseColor("#555555"),
        )
        editCandidatesToggle.alpha = if (active) 1f else 0.7f
    }

    private fun checkWin() {
        if (boardView.isSolved()) {
            Toast.makeText(this, "🎉 Solved!", Toast.LENGTH_LONG).show()
        }
    }
}
