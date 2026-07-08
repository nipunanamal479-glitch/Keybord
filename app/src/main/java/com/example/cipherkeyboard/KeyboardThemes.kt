package com.example.cipherkeyboard

import android.graphics.Color

data class KeyboardTheme(
    val name: String,
    val background: Int,
    val keyBackground: Int,
    val specialKeyBackground: Int
)

object KeyboardThemes {
    val THEMES = listOf(
        KeyboardTheme(
            name = "Midnight",
            background = Color.parseColor("#1C1C1E"),
            keyBackground = Color.parseColor("#2C2C2E"),
            specialKeyBackground = Color.parseColor("#4A90D9")
        ),
        KeyboardTheme(
            name = "Ocean",
            background = Color.parseColor("#0A2647"),
            keyBackground = Color.parseColor("#144272"),
            specialKeyBackground = Color.parseColor("#2C74B3")
        ),
        KeyboardTheme(
            name = "Purple Dream",
            background = Color.parseColor("#1A0B2E"),
            keyBackground = Color.parseColor("#2D1B4E"),
            specialKeyBackground = Color.parseColor("#8B5CF6")
        ),
        KeyboardTheme(
            name = "Neon Green",
            background = Color.parseColor("#0D1F0D"),
            keyBackground = Color.parseColor("#173317"),
            specialKeyBackground = Color.parseColor("#39C13C")
        ),
        KeyboardTheme(
            name = "Sunset",
            background = Color.parseColor("#2E1A1A"),
            keyBackground = Color.parseColor("#4A2C2A"),
            specialKeyBackground = Color.parseColor("#FF6B35")
        )
    )
}
