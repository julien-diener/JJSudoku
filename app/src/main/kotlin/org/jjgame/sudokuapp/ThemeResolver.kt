package org.jjgame.sudokuapp

import android.content.Context

object ThemeResolver {
    fun resolveColor(context: Context, attrRes: Int, fallbackColor: Int = 0): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
        return try {
            typedArray.getColor(0, fallbackColor)
        } finally {
            typedArray.recycle()
        }
    }

    fun resolveDimension(context: Context, attrRes: Int, fallback: Float): Float {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
        return try {
            typedArray.getDimension(0, fallback)
        } finally {
            typedArray.recycle()
        }
    }
}

