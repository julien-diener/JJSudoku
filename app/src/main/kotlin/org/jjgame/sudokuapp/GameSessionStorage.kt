package org.jjgame.sudokuapp

import android.content.Context
import org.jjgame.sudoku.CellState
import org.jjgame.sudoku.Difficulty
import org.jjgame.sudoku.SudokuCell
import org.jjgame.sudoku.SudokuGameSession
import org.jjgame.sudoku.SudokuPuzzle
import org.json.JSONArray
import org.json.JSONObject

class GameSessionStorage(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(difficulty: Difficulty, session: SudokuGameSession) {
        preferences.edit()
            .putString(KEY_SAVED_GAME, encode(difficulty, session))
            .apply()
    }

    fun load(): RestoredGameSession? {
        val payload = preferences.getString(KEY_SAVED_GAME, null) ?: return null
        return decode(payload)
    }

    data class RestoredGameSession(
        val difficulty: Difficulty,
        val session: SudokuGameSession,
    )

    companion object {
        const val PREFS_NAME = "sudoku_game"
        const val KEY_SAVED_GAME = "saved_game"

        const val KEY_VERSION = "version"
        const val KEY_DIFFICULTY = "difficulty"
        const val KEY_CELLS = "cells"
        const val KEY_VALUE = "value"
        const val KEY_STATE = "state"

        const val SCHEMA_VERSION = 1
        const val GRID_SIZE = 81

        internal fun encode(difficulty: Difficulty, session: SudokuGameSession): String {
            val root = JSONObject()
                .put(KEY_VERSION, SCHEMA_VERSION)
                .put(KEY_DIFFICULTY, difficulty.name)
                .put(KEY_CELLS, session.puzzle.cells.toJson())
            return root.toString()
        }

        internal fun decode(payload: String): RestoredGameSession? = runCatching {
            val root = JSONObject(payload)
            if (root.optInt(KEY_VERSION) != SCHEMA_VERSION) return null

            val difficulty = Difficulty.valueOf(root.getString(KEY_DIFFICULTY))
            val cellsJson = root.getJSONArray(KEY_CELLS)
            if (cellsJson.length() != GRID_SIZE) return null

            val cells = Array(GRID_SIZE) { index ->
                val cellJson = cellsJson.getJSONObject(index)
                val value = cellJson.getInt(KEY_VALUE)
                val state = CellState.valueOf(cellJson.getString(KEY_STATE))
                SudokuCell(value = value, state = state)
            }

            RestoredGameSession(
                difficulty = difficulty,
                session = SudokuGameSession(SudokuPuzzle(cells)),
            )
        }.getOrNull()

        private fun Array<SudokuCell>.toJson(): JSONArray = JSONArray().also { array ->
            forEach { cell ->
                array.put(
                    JSONObject()
                        .put(KEY_VALUE, cell.value)
                        .put(KEY_STATE, cell.state.name),
                )
            }
        }
    }
}

