package com.example.cipherkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.ExtractedTextRequest
import android.widget.Button
import android.widget.LinearLayout

class CipherInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardRoot: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var hue = 0f

    private val rgbAnimation = object : Runnable {
        override fun run() {
            hue = (hue + 10) % 360
            keyboardRoot.setBackgroundColor(Color.HSVToColor(floatArrayOf(hue, 0.7f, 0.5f)))
            handler.postDelayed(this, 50)
        }
    }

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        keyboardRoot = root
        
        val kv = root.findViewById<KeyboardView>(R.id.keyboard_view)
        kv.keyboard = Keyboard(this, R.xml.qwerty)
        kv.setOnKeyboardActionListener(this)

        root.findViewById<Button>(R.id.btn_encode).setOnClickListener { processText(true) }
        root.findViewById<Button>(R.id.btn_decode).setOnClickListener { processText(false) }
        root.findViewById<Button>(R.id.btn_clip).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                currentInputConnection?.commitText(clipData.getItemAt(0).text.toString(), 1)
            }
        }

        handler.post(rgbAnimation)
        return root
    }

    private fun processText(isEncode: Boolean) {
        val ic = currentInputConnection ?: return
        val text = ic.getExtractedText(ExtractedTextRequest(), 0)?.text?.toString() ?: ""
        if (text.isEmpty()) return
        
        val shift = if (isEncode) 3 else -3
        val result = text.map { 
            if (it in 'a'..'z' || it in 'A'..'Z') it + shift else it 
        }.joinToString("")
        
        ic.deleteSurroundingText(text.length, 0)
        ic.commitText(result, 1)
    }

    override fun onKey(p0: Int, p1: IntArray?) {
        val ic = currentInputConnection ?: return
        if (p0 == -5) ic.deleteSurroundingText(1, 0) else ic.commitText(p0.toChar().toString(), 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rgbAnimation)
    }

    override fun onPress(p0: Int) {} 
    override fun onRelease(p0: Int) {} 
    override fun onText(p0: CharSequence?) {}
    override fun swipeLeft() {} 
    override fun swipeRight() {} 
    override fun swipeDown() {} 
    override fun swipeUp() {}
}
