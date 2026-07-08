package com.example.cipherkeyboard

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.preference.PreferenceManager
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
    private lateinit var clipScroll: HorizontalScrollView
    private lateinit var clipRow: LinearLayout
    private lateinit var kv: KeyboardView

    private var isSymbols = false
    private var isShifted = false
    private var currentTheme: KeyboardTheme = KeyboardThemes.THEMES[0]

    private val clipHistory = mutableListOf<String>()
    private lateinit var clipboardManager: ClipboardManager

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        addCurrentClipToHistory()
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipListener)
    }

    private fun addCurrentClipToHistory() {
        val clip = clipboardManager.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString()
            if (!text.isNullOrEmpty()) {
                clipHistory.remove(text)
                clipHistory.add(0, text)
                if (clipHistory.size > 15) clipHistory.removeAt(clipHistory.size - 1)
                if (::clipRow.isInitialized) refreshClipRow()
            }
        }
    }

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        keyboardRoot = root

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themeIndex = prefs.getInt("selected_theme", 0)
        currentTheme = KeyboardThemes.THEMES.getOrElse(themeIndex) { KeyboardThemes.THEMES[0] }

        kv = root.findViewById(R.id.keyboard_view)
        kv.keyboard = Keyboard(this, R.xml.qwerty)
        kv.setOnKeyboardActionListener(this)
        kv.isPreviewEnabled = false

        cipherRow = root.findViewById(R.id.cipher_row)
        emojiScroll = root.findViewById(R.id.emoji_scroll)
        clipScroll = root.findViewById(R.id.clip_scroll)
        clipRow = root.findViewById(R.id.clip_row)

        root.findViewById<Button>(R.id.btn_cipher).setOnClickListener {
            togglePanel(cipherRow, listOf(emojiScroll, clipScroll))
        }

        root.findViewById<Button>(R.id.btn_emoji).setOnClickListener {
            togglePanel(emojiScroll, listOf(cipherRow, clipScroll))
        }

        root.findViewById<Button>(R.id.btn_clip).setOnClickListener {
            addCurrentClipToHistory()
            togglePanel(clipScroll, listOf(cipherRow, emojiScroll))
        }

        root.findViewById<Button>(R.id.btn_encode).setOnClickListener {
            processText(true)
            closePanel(cipherRow)
        }

        root.findViewById<Button>(R.id.btn_decode).setOnClickListener {
            processText(false)
            closePanel(cipherRow)
        }

        setupEmojiRow(root)
        refreshClipRow()
        applyTheme(root)

        return root
    }

    private fun applyTheme(root: LinearLayout) {
        root.setBackgroundColor(currentTheme.background)
        kv.setBackgroundColor(currentTheme.background)

        try {
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
                setColor(currentTheme.keyBackground)
            }
            val field = KeyboardView::class.java.getDeclaredField("mKeyBackground")
            field.isAccessible = true
            field.set(kv, bgDrawable)
            kv.invalidateAllKeys()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val specialButtons = listOf(
            root.findViewById<Button>(R.id.btn_cipher),
            root.findViewById<Button>(R.id.btn_clip),
            root.findViewById<Button>(R.id.btn_emoji),
            root.findViewById<Button>(R.id.btn_encode),
            root.findViewById<Button>(R.id.btn_decode)
        )
        for (btn in specialButtons) {
            btn.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
                setColor(currentTheme.specialKeyBackground)
            }
        }
    }

    private fun refreshClipRow() {
        clipRow.removeAllViews()

        if (clipHistory.isEmpty()) {
            val emptyBtn = Button(this)
            emptyBtn.text = "Clipboard eka empty"
            emptyBtn.textSize = 14f
            emptyBtn.isEnabled = false
            emptyBtn.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
                setColor(currentTheme.keyBackground)
            }
            emptyBtn.setTextColor(Color.parseColor("#888888"))
            clipRow.addView(emptyBtn)
            return
        }

        for (item in clipHistory) {
            val label = if (item.length > 20) item.substring(0, 20) + "…" else item
            val btn = Button(this)
            btn.text = label
            btn.textSize = 14f
            btn.isAllCaps = false
            btn.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
                setColor(currentTheme.keyBackground)
            }
            btn.setTextColor(Color.WHITE)
            btn.setPadding(24, 0, 24, 0)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(4, 0, 4, 0)
            btn.layoutParams = params
            btn.setOnClickListener {
                currentInputConnection?.commitText(item, 1)
            }
            clipRow.addView(btn)
        }

        val clearBtn = Button(this)
        clearBtn.text = "🗑 Clear"
        clearBtn.textSize = 14f
        clearBtn.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * resources.displayMetrics.density
            setColor(currentTheme.specialKeyBackground)
        }
        clearBtn.setTextColor(Color.parseColor("#FF6B6B"))
        val clearParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        clearParams.setMargins(4, 0, 4, 0)
        clearBtn.layoutParams = clearParams
        clearBtn.setOnClickListener {
            clipHistory.clear()
            refreshClipRow()
        }
        clipRow.addView(clearBtn)
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
            btn.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f * resources.displayMetrics.density
                setColor(currentTheme.keyBackground)
            }
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

    private fun togglePanel(panel: View, others: List<View>) {
        for (other in others) {
            if (other.visibility == View.VISIBLE) closePanel(other)
        }

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
        clipboardManager.removePrimaryClipChangedListener(clipListener)
    }

    override fun onPress(p0: Int) {}
    override fun onRelease(p0: Int) {}
    override fun onText(p0: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
