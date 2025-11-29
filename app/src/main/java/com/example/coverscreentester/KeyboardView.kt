package com.example.coverscreentester

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface KeyboardListener {
        fun onKeyPress(keyCode: Int, char: Char?)
        fun onTextInput(text: String)
        fun onSpecialKey(key: SpecialKey)
    }

    enum class SpecialKey {
        BACKSPACE, ENTER, SPACE, SHIFT, CAPS_LOCK, SYMBOLS, ABC,
        TAB, ARROW_UP, ARROW_DOWN, ARROW_LEFT, ARROW_RIGHT,
        HOME, END, DELETE, ESCAPE, CTRL, ALT
    }

    enum class KeyboardState {
        LOWERCASE, UPPERCASE, CAPS_LOCK, SYMBOLS_1, SYMBOLS_2
    }

    private var listener: KeyboardListener? = null
    private var currentState = KeyboardState.LOWERCASE
    private var vibrationEnabled = true
    private var keyHeight = 40
    private var keySpacing = 2
    private var fontSize = 13f

    private val lowercaseRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BKSP"),
        listOf("SYM", ",", "SPACE", ".", "ENTER")
    )

    private val uppercaseRows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("SHIFT", "Z", "X", "C", "V", "B", "N", "M", "BKSP"),
        listOf("SYM", ",", "SPACE", ".", "ENTER")
    )

    private val symbols1Rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "\$", "%", "&", "-", "+", "(", ")"),
        listOf("SYM2", "*", "\"", "'", ":", ";", "!", "?", "BKSP"),
        listOf("ABC", ",", "SPACE", ".", "ENTER")
    )

    private val symbols2Rows = listOf(
        listOf("~", "`", "|", "^", "=", "{", "}", "[", "]", "\\"),
        listOf("<", ">", "/", "_", "©", "®", "™", "°", "•"),
        listOf("SYM1", "€", "£", "¥", "¢", "§", "¶", "∆", "BKSP"),
        listOf("ABC", "_", "SPACE", "/", "ENTER")
    )

    private val arrowRow = listOf("TAB", "CTRL", "←", "↑", "↓", "→", "ESC")

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#1A1A1A"))
        setPadding(4, 4, 4, 4)
        buildKeyboard()
    }

    fun setKeyboardListener(l: KeyboardListener) { listener = l }
    fun setVibrationEnabled(enabled: Boolean) { vibrationEnabled = enabled }

    private fun buildKeyboard() {
        removeAllViews()
        val rows = when (currentState) {
            KeyboardState.LOWERCASE -> lowercaseRows
            KeyboardState.UPPERCASE, KeyboardState.CAPS_LOCK -> uppercaseRows
            KeyboardState.SYMBOLS_1 -> symbols1Rows
            KeyboardState.SYMBOLS_2 -> symbols2Rows
        }
        for ((rowIndex, row) in rows.withIndex()) { addView(createRow(row, rowIndex)) }
        addView(createArrowRow())
    }

    private fun createRow(keys: List<String>, rowIndex: Int): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = HORIZONTAL
        row.gravity = Gravity.CENTER
        row.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(keyHeight)).apply {
            setMargins(0, dpToPx(keySpacing), 0, 0)
        }
        if (rowIndex == 1) row.setPadding(dpToPx(12), 0, dpToPx(12), 0)
        for (key in keys) { row.addView(createKey(key, getKeyWeight(key))) }
        return row
    }

    private fun createArrowRow(): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = HORIZONTAL
        row.gravity = Gravity.CENTER
        row.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(keyHeight - 4)).apply {
            setMargins(0, dpToPx(keySpacing + 2), 0, 0)
        }
        row.setBackgroundColor(Color.parseColor("#0D0D0D"))
        for (key in arrowRow) { row.addView(createKey(key, getKeyWeight(key), isArrowRow = true)) }
        return row
    }

    private fun getKeyWeight(key: String): Float = when (key) {
        "SPACE" -> 4.5f
        "SHIFT", "BKSP", "ENTER" -> 1.5f
        "SYM", "SYM1", "SYM2", "ABC" -> 1.3f
        "TAB", "CTRL", "ESC" -> 1.2f
        else -> 1f
    }

    private fun createKey(key: String, weight: Float, isArrowRow: Boolean = false): View {
        val container = FrameLayout(context)
        val params = LayoutParams(0, LayoutParams.MATCH_PARENT, weight)
        params.setMargins(dpToPx(keySpacing), 0, dpToPx(keySpacing), 0)
        container.layoutParams = params

        val keyView = TextView(context)
        keyView.gravity = Gravity.CENTER
        keyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isArrowRow) fontSize - 2 else fontSize)
        keyView.setTextColor(Color.WHITE)
        keyView.text = getDisplayText(key)

        val bg = GradientDrawable()
        bg.cornerRadius = dpToPx(6).toFloat()
        bg.setColor(getKeyColor(key, isArrowRow))
        keyView.background = bg
        keyView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        container.addView(keyView)

        container.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    bg.setColor(Color.parseColor("#4A4A4A"))
                    keyView.background = bg
                    if (vibrationEnabled) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    bg.setColor(getKeyColor(key, isArrowRow))
                    keyView.background = bg
                    if (event.action == MotionEvent.ACTION_UP) handleKeyPress(key)
                }
            }
            true
        }
        if (key == "SHIFT") container.setOnLongClickListener { toggleCapsLock(); true }
        return container
    }

    private fun getDisplayText(key: String): String = when (key) {
        "SHIFT" -> if (currentState == KeyboardState.CAPS_LOCK) "⬆" else "⇧"
        "BKSP" -> "⌫"; "ENTER" -> "↵"; "SPACE" -> "␣"
        "SYM", "SYM1", "SYM2" -> "?123"; "ABC" -> "ABC"
        "TAB" -> "⇥"; "CTRL" -> "Ctrl"; "ESC" -> "Esc"
        "←" -> "◀"; "→" -> "▶"; "↑" -> "▲"; "↓" -> "▼"
        else -> key
    }

    private fun getKeyColor(key: String, isArrowRow: Boolean): Int {
        if (isArrowRow) return Color.parseColor("#252525")
        return when (key) {
            "SHIFT" -> when (currentState) {
                KeyboardState.CAPS_LOCK -> Color.parseColor("#3DDC84")
                KeyboardState.UPPERCASE -> Color.parseColor("#4A90D9")
                else -> Color.parseColor("#3A3A3A")
            }
            "BKSP", "ENTER", "SYM", "SYM1", "SYM2", "ABC" -> Color.parseColor("#3A3A3A")
            "SPACE" -> Color.parseColor("#2D2D2D")
            else -> Color.parseColor("#2D2D2D")
        }
    }

    private fun handleKeyPress(key: String) {
        when (key) {
            "SHIFT" -> toggleShift()
            "BKSP" -> listener?.onSpecialKey(SpecialKey.BACKSPACE)
            "ENTER" -> listener?.onSpecialKey(SpecialKey.ENTER)
            "SPACE" -> listener?.onSpecialKey(SpecialKey.SPACE)
            "TAB" -> listener?.onSpecialKey(SpecialKey.TAB)
            "CTRL" -> listener?.onSpecialKey(SpecialKey.CTRL)
            "ESC" -> listener?.onSpecialKey(SpecialKey.ESCAPE)
            "←" -> listener?.onSpecialKey(SpecialKey.ARROW_LEFT)
            "→" -> listener?.onSpecialKey(SpecialKey.ARROW_RIGHT)
            "↑" -> listener?.onSpecialKey(SpecialKey.ARROW_UP)
            "↓" -> listener?.onSpecialKey(SpecialKey.ARROW_DOWN)
            "SYM", "SYM1" -> { currentState = KeyboardState.SYMBOLS_1; buildKeyboard() }
            "SYM2" -> { currentState = KeyboardState.SYMBOLS_2; buildKeyboard() }
            "ABC" -> { currentState = KeyboardState.LOWERCASE; buildKeyboard() }
            else -> {
                listener?.onTextInput(key)
                if (currentState == KeyboardState.UPPERCASE) { currentState = KeyboardState.LOWERCASE; buildKeyboard() }
            }
        }
    }

    private fun toggleShift() {
        currentState = when (currentState) {
            KeyboardState.LOWERCASE -> KeyboardState.UPPERCASE
            KeyboardState.UPPERCASE -> KeyboardState.LOWERCASE
            KeyboardState.CAPS_LOCK -> KeyboardState.LOWERCASE
            else -> currentState
        }
        buildKeyboard()
    }

    private fun toggleCapsLock() {
        currentState = when (currentState) {
            KeyboardState.LOWERCASE, KeyboardState.UPPERCASE -> KeyboardState.CAPS_LOCK
            KeyboardState.CAPS_LOCK -> KeyboardState.LOWERCASE
            else -> currentState
        }
        if (vibrationEnabled) vibrate()
        buildKeyboard()
    }

    private fun vibrate() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else { @Suppress("DEPRECATION") v.vibrate(30) }
    }

    private fun dpToPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
    ).roundToInt()
}
