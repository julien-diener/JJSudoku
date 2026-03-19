package org.jjgame.sudokuapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.jjgame.sudoku.Difficulty

class MainActivity : AppCompatActivity() {

    private lateinit var boardView: SudokuBoardView
    private lateinit var statusText: TextView
    private lateinit var gameViewModel: SudokuGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boardView  = findViewById(R.id.boardView)
        statusText = findViewById(R.id.statusText)
        gameViewModel = ViewModelProvider(this)[SudokuGameViewModel::class.java]

        boardView.gameSession = gameViewModel.gameSession
        boardView.onSessionChanged = { gameViewModel.persistGameSession() }
        statusText.text = gameViewModel.currentDifficulty.name.lowercase().replaceFirstChar { it.uppercase() }

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
        boardView.gameSession = gameViewModel.gameSession
        statusText.text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun checkWin() {
        if (boardView.isSolved()) {
            Toast.makeText(this, "🎉 Solved!", Toast.LENGTH_LONG).show()
        }
    }
}
