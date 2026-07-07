package com.example.cipherkeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.View

class CipherInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    override fun onCreateInputView(): View {
        val kv = KeyboardView(this, null)
        val k = Keyboard(this, R.xml.qwerty)
        kv.keyboard = k
        kv.setOnKeyboardActionListener(this)
        return kv
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        when (primaryCode) {
            -5 -> ic.deleteSurroundingText(1, 0) // Backspace බටන් එක වැඩ කරන්න
            32 -> ic.commitText(" ", 1)        // Space බටන් එක වැඩ කරන්න
            else -> ic.commitText(primaryCode.toChar().toString(), 1)
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
