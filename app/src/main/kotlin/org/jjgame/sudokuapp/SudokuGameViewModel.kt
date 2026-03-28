package org.jjgame.sudokuapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun startGameAsync(difficulty: Difficulty = currentDifficulty) {
        val puzzle = withContext(Dispatchers.Default) {
            SudokuGenerator.generate(difficulty)
        }
        currentDifficulty = difficulty
        gameSession = SudokuGameSession(puzzle)
        persistGameSession()
    }

    fun hasSavedGame(): Boolean {
        val restored = storage.load() ?: return false
        return !restored.session.puzzle.isSolved()
    }

    fun clearSavedGame() {
        storage.clear()
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

    /** Called every second while the game screen is in the foreground. */
    fun tickElapsed() {
        if (gameSession.finishedAt == null) {
            gameSession.elapsedSeconds++
        }
    }

    /** Records the finish time and persists. Call when the puzzle is solved. */
    fun recordFinish() {
        if (gameSession.finishedAt == null) {
            gameSession.finishedAt = System.currentTimeMillis()
            persistGameSession()
        }
    }
}

