package org.jjgame.sudokuapp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeModeStorage {
    private const val PREFS_NAME = "theme_mode"
    private const val KEY_DARK_MODE = "dark_mode"

    fun applySavedMode(context: Context) {
        val mode = if (isDarkMode(context)) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun toggle(context: Context) {
        setDarkMode(context, !isDarkMode(context))
    }

    fun isDarkMode(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DARK_MODE, false)
    }

    private fun setDarkMode(context: Context, darkMode: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, darkMode).apply()
        val mode = if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

