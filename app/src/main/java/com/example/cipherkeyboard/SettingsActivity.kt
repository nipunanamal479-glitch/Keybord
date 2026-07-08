package com.example.cipherkeyboard

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var themeRow: LinearLayout
    private var selectedTheme = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.settings_activity)

            val editText = findViewById<EditText>(R.id.key_input)
            val saveButton = findViewById<Button>(R.id.save_button)
            val enableButton = findViewById<Button>(R.id.btn_enable_keyboard)
            val switchButton = findViewById<Button>(R.id.btn_switch_keyboard)
            statusText = findViewById(R.id.status_text)
            themeRow = findViewById(R.id.theme_row)

            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            editText.setText(prefs.getString("cipher_key", "KEY"))
            selectedTheme = prefs.getInt("selected_theme", 0)

            setupThemeRow(prefs)

            saveButton.setOnClickListener {
                prefs.edit().putString("cipher_key", editText.text.toString()).apply()
                Toast.makeText(this, "Key saved", Toast.LENGTH_SHORT).show()
            }

            enableButton.setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }

            switchButton.setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        } catch (e: Throwable) {
            Toast.makeText(this, "Error: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupThemeRow(prefs: android.content.SharedPreferences) {
        themeRow.removeAllViews()

        for ((index, theme) in KeyboardThemes.THEMES.withIndex()) {
            val circle = View(this)
            val size = (56 * resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(size, size)
            params.setMargins(12, 0, 12, 0)
            circle.layoutParams = params

            circle.background = buildCircleDrawable(theme.specialKeyBackground, index == selectedTheme)

            circle.setOnClickListener {
                selectedTheme = index
                prefs.edit().putInt("selected_theme", index).apply()
                Toast.makeText(this, "${theme.name} theme selected", Toast.LENGTH_SHORT).show()
                setupThemeRow(prefs)
            }

            themeRow.addView(circle)
        }
    }

    private fun buildCircleDrawable(color: Int, isSelected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
            if (isSelected) {
                setStroke((3 * resources.displayMetrics.density).toInt(), Color.WHITE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updateStatus()
        } catch (e: Throwable) {
            Toast.makeText(this, "Error: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun updateStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledList = imm.enabledInputMethodList
        val isEnabled = enabledList.any { it.packageName == packageName }

        val currentMethod = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: ""

        when {
            currentMethod.contains(packageName) -> {
                statusText.text = "✅ Cipher Keyboard is active"
                statusText.setTextColor(0xFF4CAF50.toInt())
            }
            isEnabled -> {
                statusText.text = "⚠ Enabled. Now select it (Step 2)"
                statusText.setTextColor(0xFFFFA726.toInt())
            }
            else -> {
                statusText.text = "⚠ Keyboard not enabled yet (do Step 1)"
                statusText.setTextColor(0xFFFFA726.toInt())
            }
        }
    }
}
