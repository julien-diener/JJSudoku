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

    fun clear() {
        preferences.edit().remove(KEY_SAVED_GAME).apply()
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
        const val KEY_STARTED_AT = "startedAt"
        const val KEY_ELAPSED_SECONDS = "elapsedSeconds"
        const val KEY_FINISHED_AT = "finishedAt"
        const val KEY_ERROR_COUNT = "errorCount"

        const val SCHEMA_VERSION = 2
        const val GRID_SIZE = 81

        internal fun encode(difficulty: Difficulty, session: SudokuGameSession): String {
            val cellsJson = JSONArray().also { array ->
                session.puzzle
                    .mapCells { _, cell ->
                        JSONObject()
                            .put(KEY_VALUE, cell.value)
                            .put(KEY_STATE, cell.state.name)
                    }
                    .forEach(array::put)
            }

            val root = JSONObject()
                .put(KEY_VERSION, SCHEMA_VERSION)
                .put(KEY_DIFFICULTY, difficulty.name)
                .put(KEY_CELLS, cellsJson)
                .put(KEY_STARTED_AT, session.startedAt)
                .put(KEY_ELAPSED_SECONDS, session.elapsedSeconds)
                .put(KEY_ERROR_COUNT, session.errorCount)
            session.finishedAt?.let { root.put(KEY_FINISHED_AT, it) }
            return root.toString()
        }

        internal fun decode(payload: String): RestoredGameSession? = runCatching {
            val root = JSONObject(payload)
            val version = root.optInt(KEY_VERSION)
            if (version != SCHEMA_VERSION && version != 1) return null

            val difficulty = Difficulty.valueOf(root.getString(KEY_DIFFICULTY))
            val cellsJson = root.getJSONArray(KEY_CELLS)
            if (cellsJson.length() != GRID_SIZE) return null

            val cells = Array(GRID_SIZE) { index ->
                val cellJson = cellsJson.getJSONObject(index)
                val value = cellJson.getInt(KEY_VALUE)
                val state = CellState.valueOf(cellJson.getString(KEY_STATE))
                SudokuCell(value = value, state = state)
            }

            // Timing fields added in v2; v1 saves get safe defaults
            val startedAt = root.optLong(KEY_STARTED_AT, System.currentTimeMillis())
            val elapsedSeconds = root.optLong(KEY_ELAPSED_SECONDS, 0L)
            val finishedAt = if (root.has(KEY_FINISHED_AT)) root.getLong(KEY_FINISHED_AT) else null
            val errorCount = root.optInt(KEY_ERROR_COUNT, 0)

            RestoredGameSession(
                difficulty = difficulty,
                session = SudokuGameSession(
                    puzzle = SudokuPuzzle(cells),
                    startedAt = startedAt,
                    elapsedSeconds = elapsedSeconds,
                    finishedAt = finishedAt,
                    errorCount = errorCount,
                ),
            )
        }.getOrNull()

    }
}

