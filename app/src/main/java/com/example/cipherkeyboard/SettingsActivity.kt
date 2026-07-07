package com.example.cipherkeyboard

import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val editText = findViewById<EditText>(R.id.key_input)
        val saveButton = findViewById<Button>(R.id.save_button)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        editText.setText(prefs.getString("cipher_key", "KEY"))

        saveButton.setOnClickListener {
            prefs.edit().putString("cipher_key", editText.text.toString()).apply()
            Toast.makeText(this, "Key saved", Toast.LENGTH_SHORT).show()
        }
    }
}
