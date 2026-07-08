package com.example.cipherkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.ExtractedTextRequest
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout

class CipherInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardRoot: LinearLayout
    private lateinit var cipherRow: LinearLayout
    private lateinit var emojiScroll: HorizontalScrollView
    private lateinit var kv: KeyboardView

    private var isSymbols = false
    private var isShifted = false

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

        kv = root.findViewById(R.id.keyboard_view)
        kv.keyboard = Keyboard(this, R.xml.qwerty)
        kv.setOnKeyboardActionListener(this)

        cipherRow = root.findViewById(R.id.cipher_row)
        emojiScroll = root.findViewById(R.id.emoji_scroll)

        root.findViewById<Button>(R.id.btn_cipher).setOnClickListener {
            togglePanel(cipherRow, emojiScroll)
        }

        root.findViewById<Button>(R.id.btn_emoji).setOnClickListener {
            togglePanel(emojiScroll, cipherRow)
        }

        root.findViewById<Button>(R.id.btn_encode).setOnClickListener {
            processText(true)
            closePanel(cipherRow)
        }

        root.findViewById<Button>(R.id.btn_decode).setOnClickListener {
            processText(false)
            closePanel(cipherRow)
        }

        root.findViewById<Button>(R.id.btn_clip).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboard.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                currentInputConnection?.commitText(clipData.getItemAt(0).text.toString(), 1)
            }
        }

        setupEmojiRow(root)

        handler.post(rgbAnimation)
        return root
    }

    private fun setupEmojiRow(root: LinearLayout) {
        val emojiRow = root.findViewById<LinearLayout>(R.id.emoji_row)
        val commonEmojis = listOf(
            "😀","😂","😍","😢","😡","👍","👎","🙏","❤️","🔥",
            "🎉","😎","😴","🤔","😭","👏","💯","✨","🙌","😅"
        )
        for (emoji in commonEmojis) {
            val btn = Button(this)
            btn.text = emoji
            btn.textSize = 18f
            btn.setBackgroundResource(R.drawable.keyboard_key_bg)
            btn.setPadding(20, 0, 20, 0)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(4, 0, 4, 0)
            btn.layoutParams = params
            btn.setOnClickListener {
                currentInputConnection?.commitText(emoji, 1)
            }
            emojiRow.addView(btn)
        }
    }

    private fun togglePanel(panel: View, otherPanel: View) {
        if (otherPanel.visibility == View.VISIBLE) closePanel(otherPanel)

        if (panel.visibility == View.VISIBLE) {
            closePanel(panel)
        } else {
            openPanel(panel)
        }
    }

    private fun openPanel(panel: View) {
        panel.visibility = View.VISIBLE
        val params = panel.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        panel.layoutParams = params

        panel.alpha = 0f
        panel.translationY = -20f
        panel.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(180)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun closePanel(panel: View) {
        panel.animate()
            .alpha(0f)
            .translationY(-20f)
            .setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                panel.visibility = View.GONE
                val params = panel.layoutParams
                params.height = 0
                panel.layoutParams = params
            }
            .start()
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
        when (p0) {
            -5 -> ic.deleteSurroundingText(1, 0)
            -2 -> {
                isSymbols = !isSymbols
                kv.keyboard = Keyboard(this, if (isSymbols) R.xml.symbols else R.xml.qwerty)
            }
            -1 -> {
                isShifted = !isShifted
                kv.isShifted = isShifted
            }
            10, -4 -> ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            else -> {
                var code = p0.toChar()
                if (isShifted && code.isLetter()) code = code.uppercaseChar()
                ic.commitText(code.toString(), 1)
                if (isShifted) {
                    isShifted = false
                    kv.isShifted = false
                }
            }
        }
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
