package com.example.cipherkeyboard

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout

class CipherInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardRoot: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var rgbHue = 0f

    // 🌈 RGB බෝඩර් ඇනිමේෂන් එක රන් වන ලූප් එක
    private val rgbRunnable = object : Runnable {
        override fun run() {
            rgbHue = (rgbHue + 5) % 360
            val color = Color.HSVToColor(floatArrayOf(rgbHue, 1f, 1f))
            
            // කීබෝඩ් එක වටේට ලස්සන RGB බෝඩර් එකක් දෙනවා
            keyboardRoot.setPadding(6, 6, 6, 6)
            keyboardRoot.setBackgroundColor(color)
            
            handler.postDelayed(this, 40) // සුපිරි ස්මූත් ඇනිමේෂන් එකක් 
        }
    }

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        keyboardRoot = root.findViewById(R.id.keyboard_root)

        val kv = root.findViewById<KeyboardView>(R.id.keyboard_view)
        val k = Keyboard(this, R.xml.qwerty)
        kv.keyboard = k
        kv.setOnKeyboardActionListener(this)

        // Encode / Decode බටන්ස් වලට වැඩ දීම
        root.findViewById<Button>(R.id.btn_encode).setOnClickListener {
            runCipherAnimation(currentInputConnection, true)
        }
        root.findViewById<Button>(R.id.btn_decode).setOnClickListener {
            runCipherAnimation(currentInputConnection, false)
        }

        // RGB ඇනිමේෂන් එක ස්ටාර්ට් කිරීම
        handler.post(rgbRunnable)

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rgbRunnable) // ඇප් එක වහද්දී බැටරි බේරගන්න ඇනිමේෂන් එක නවත්වනවා
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        when (primaryCode) {
            -5 -> ic.deleteSurroundingText(1, 0)
            32 -> ic.commitText(" ", 1)
            else -> ic.commitText(primaryCode.toChar().toString(), 1)
        }
    }

    // ⚡ අකුරෙන් අකුර මාරු වෙන Cipher Animation එක
    private fun runCipherAnimation(ic: InputConnection, isEncode: Boolean) {
        val extractedText = ic.getExtractedText(ExtractedTextRequest(), 0)
        val currentText = extractedText?.text?.toString() ?: ""
        if (currentText.isEmpty()) return

        ic.deleteSurroundingText(currentText.length, currentText.length)

        val animHandler = Handler(Looper.getMainLooper())
        var currentIndex = 0
        val shift = 3 

        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex < currentText.length) {
                    val originalChar = currentText[currentIndex]
                    val processedChar = if (isEncode) {
                        cipherTransform(originalChar, shift)
                    } else {
                        cipherTransform(originalChar, 26 - shift)
                    }
                    ic.commitText(processedChar.toString(), 1)
                    currentIndex++
                    animHandler.postDelayed(this, 80) // ටයිප් වෙද්දී යන Delay එක
                }
            }
        }
        animHandler.post(runnable)
    }

    private fun cipherTransform(char: Char, shift: Int): Char {
        return when (char) {
            in 'a'..'z' -> ((char - 'a' + shift) % 26 + 'a'.toInt()).toChar()
            in 'A'..'Z' -> ((char - 'A' + shift) % 26 + 'A'.toInt()).toChar()
            else -> char
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
