package com.example.cipherkeyboard

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.settings_activity)

            val editText = findViewById<EditText>(R.id.key_input)
            val saveButton = findViewById<Button>(R.id.save_button)
            val enableButton = findViewById<Button>(R.id.btn_enable_keyboard)
            val switchButton = findViewById<Button>(R.id.btn_switch_keyboard)
            statusText = findViewById(R.id.status_text)

            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            editText.setText(prefs.getString("cipher_key", "KEY"))

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
            Toast.makeText(this, "CREATE ERROR: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            updateStatus()
        } catch (e: Throwable) {
            Toast.makeText(this, "RESUME ERROR: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun updateStatus() {
        val enabledMethods = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_INPUT_METHODS
        ) ?: ""

        val currentMethod = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: ""

        when {
            currentMethod.contains("CipherInputMethodService") -> {
                statusText.text = "✅ Cipher Keyboard active widiyata select wela"
                statusText.setTextColor(0xFF4CAF50.toInt())
            }
            enabledMethods.contains("CipherInputMethodService") -> {
                statusText.text = "⚠ Enable wela, ethnam select karanna one (Step 2)"
                statusText.setTextColor(0xFFFFA726.toInt())
            }
            else -> {
                statusText.text = "⚠ Keyboard tama enable karala na (Step 1 karanna)"
                statusText.setTextColor(0xFFFFA726.toInt())
            }
        }
    }
}
