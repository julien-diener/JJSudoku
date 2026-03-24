package org.jjgame.sudokuapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.jjgame.sudoku.Difficulty
import org.jjgame.sudoku.SudokuGameSession
import org.jjgame.sudoku.SudokuGenerator

class SudokuGameViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = GameSessionStorage(application)

    var currentDifficulty: Difficulty
        private set

    var gameSession: SudokuGameSession
        private set

    init {
        val restored = storage.load()
        if (restored != null) {
            // TODO: add error management (log + UI)
            currentDifficulty = restored.difficulty
            gameSession = restored.session
        } else {
            currentDifficulty = Difficulty.EASY
            gameSession = SudokuGameSession(SudokuGenerator.generate(currentDifficulty))
            persistGameSession()
        }
    }

    fun startGame(difficulty: Difficulty = currentDifficulty) {
        currentDifficulty = difficulty
        gameSession = SudokuGameSession(SudokuGenerator.generate(difficulty))
        persistGameSession()
    }

    fun hasSavedGame(): Boolean {
        return storage.load() != null
    }

    fun loadSavedGame() {
        val restored = storage.load()
        if (restored != null) {
            currentDifficulty = restored.difficulty
            gameSession = restored.session
        }
    }

    fun persistGameSession() {
        // selectedNumber and cellError are transient UI state and intentionally not saved.
        storage.save(currentDifficulty, gameSession)
    }
}

