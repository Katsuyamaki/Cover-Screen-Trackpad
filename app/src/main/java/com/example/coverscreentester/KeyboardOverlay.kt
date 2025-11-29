package com.example.coverscreentester

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import kotlin.math.max

class KeyboardOverlay(
    private val context: Context,
    private val windowManager: WindowManager,
    private val shellService: IShellService?,
    private val displayId: Int
) : KeyboardView.KeyboardListener {

    private var keyboardContainer: FrameLayout? = null
    private var keyboardView: KeyboardView? = null
    private var keyboardParams: WindowManager.LayoutParams? = null
    private var isVisible = false
    private var isLocked = false

    private var isMoving = false
    private var isResizing = false
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialWindowX = 0
    private var initialWindowY = 0
    private var initialWidth = 0
    private var initialHeight = 0

    private val handler = Handler(Looper.getMainLooper())
    private val TAG = "KeyboardOverlay"

    private var keyboardWidth = 500
    private var keyboardHeight = 280
    private var screenWidth = 720
    private var screenHeight = 748

    fun setScreenDimensions(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        keyboardWidth = (width * 0.9f).toInt().coerceIn(300, 600)
        keyboardHeight = (height * 0.38f).toInt().coerceIn(200, 350)
    }

    fun show() {
        if (isVisible) return

        try {
            createKeyboardWindow()
            isVisible = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show keyboard", e)
        }
    }

    fun hide() {
        if (!isVisible) return

        try {
            windowManager.removeView(keyboardContainer)
            keyboardContainer = null
            keyboardView = null
            isVisible = false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide keyboard", e)
        }
    }

    fun toggle() {
        if (isVisible) hide() else show()
    }

    fun isShowing(): Boolean = isVisible

    private fun createKeyboardWindow() {
        keyboardContainer = FrameLayout(context)
        
        val containerBg = GradientDrawable()
        containerBg.setColor(Color.parseColor("#1A1A1A"))
        containerBg.cornerRadius = 20f
        containerBg.setStroke(2, Color.parseColor("#3DDC84"))
        keyboardContainer?.background = containerBg

        keyboardView = KeyboardView(context)
        keyboardView?.setKeyboardListener(this)
        
        val prefs = context.getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        keyboardView?.setVibrationEnabled(prefs.getBoolean("vibrate", true))

        val kbParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        kbParams.setMargins(8, 30, 8, 8)
        keyboardContainer?.addView(keyboardView, kbParams)

        addDragHandle()
        addResizeHandle()
        addCloseButton()

        keyboardParams = WindowManager.LayoutParams(
            keyboardWidth,
            keyboardHeight,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        keyboardParams?.gravity = Gravity.TOP or Gravity.LEFT
        keyboardParams?.x = (screenWidth - keyboardWidth) / 2
        keyboardParams?.y = screenHeight - keyboardHeight - 20

        windowManager.addView(keyboardContainer, keyboardParams)
    }

    private fun addDragHandle() {
        val handle = FrameLayout(context)
        val handleParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            30
        )
        handleParams.gravity = Gravity.TOP
        
        val indicator = View(context)
        val indicatorBg = GradientDrawable()
        indicatorBg.setColor(Color.parseColor("#555555"))
        indicatorBg.cornerRadius = 3f
        indicator.background = indicatorBg
        
        val indicatorParams = FrameLayout.LayoutParams(60, 6)
        indicatorParams.gravity = Gravity.CENTER
        indicatorParams.topMargin = 10
        handle.addView(indicator, indicatorParams)

        handle.setOnTouchListener { _, event ->
            handleDrag(event)
            true
        }

        keyboardContainer?.addView(handle, handleParams)
    }

    private fun addResizeHandle() {
        val handle = FrameLayout(context)
        val handleParams = FrameLayout.LayoutParams(40, 40)
        handleParams.gravity = Gravity.BOTTOM or Gravity.RIGHT
        
        val indicator = View(context)
        val indicatorBg = GradientDrawable()
        indicatorBg.setColor(Color.parseColor("#3DDC84"))
        indicatorBg.cornerRadius = 4f
        indicator.background = indicatorBg
        indicator.alpha = 0.7f
        
        val indicatorParams = FrameLayout.LayoutParams(16, 16)
        indicatorParams.gravity = Gravity.BOTTOM or Gravity.RIGHT
        indicatorParams.setMargins(0, 0, 8, 8)
        handle.addView(indicator, indicatorParams)

        handle.setOnTouchListener { _, event ->
            handleResize(event)
            true
        }

        keyboardContainer?.addView(handle, handleParams)
    }

    private fun addCloseButton() {
        val button = FrameLayout(context)
        val buttonParams = FrameLayout.LayoutParams(30, 30)
        buttonParams.gravity = Gravity.TOP or Gravity.RIGHT
        buttonParams.setMargins(0, 2, 4, 0)
        
        val closeText = android.widget.TextView(context)
        closeText.text = "✕"
        closeText.setTextColor(Color.parseColor("#FF5555"))
        closeText.textSize = 14f
        closeText.gravity = Gravity.CENTER
        
        val closeParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        button.addView(closeText, closeParams)

        button.setOnClickListener {
            hide()
        }

        keyboardContainer?.addView(button, buttonParams)
    }

    private fun handleDrag(event: MotionEvent): Boolean {
        if (isLocked) return true
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMoving = true
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                initialWindowX = keyboardParams?.x ?: 0
                initialWindowY = keyboardParams?.y ?: 0
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMoving) {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    keyboardParams?.x = initialWindowX + dx
                    keyboardParams?.y = initialWindowY + dy
                    try {
                        windowManager.updateViewLayout(keyboardContainer, keyboardParams)
                    } catch (e: Exception) {}
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isMoving = false
                saveKeyboardPosition()
            }
        }
        return true
    }

    private fun handleResize(event: MotionEvent): Boolean {
        if (isLocked) return true
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isResizing = true
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                initialWidth = keyboardParams?.width ?: keyboardWidth
                initialHeight = keyboardParams?.height ?: keyboardHeight
            }
            MotionEvent.ACTION_MOVE -> {
                if (isResizing) {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    keyboardParams?.width = max(300, initialWidth + dx)
                    keyboardParams?.height = max(200, initialHeight + dy)
                    keyboardWidth = keyboardParams?.width ?: keyboardWidth
                    keyboardHeight = keyboardParams?.height ?: keyboardHeight
                    try {
                        windowManager.updateViewLayout(keyboardContainer, keyboardParams)
                    } catch (e: Exception) {}
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isResizing = false
                saveKeyboardSize()
            }
        }
        return true
    }

    private fun saveKeyboardSize() {
        val prefs = context.getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("keyboard_width", keyboardWidth)
            .putInt("keyboard_height", keyboardHeight)
            .apply()
    }

    private fun saveKeyboardPosition() {
        val prefs = context.getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("keyboard_x", keyboardParams?.x ?: 0)
            .putInt("keyboard_y", keyboardParams?.y ?: 0)
            .apply()
    }

    fun loadKeyboardSize() {
        val prefs = context.getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        keyboardWidth = prefs.getInt("keyboard_width", keyboardWidth)
        keyboardHeight = prefs.getInt("keyboard_height", keyboardHeight)
    }

    override fun onKeyPress(keyCode: Int, char: Char?) {
        injectKey(keyCode)
    }

    override fun onTextInput(text: String) {
        for (char in text) {
            injectCharacter(char)
        }
    }

    override fun onSpecialKey(key: KeyboardView.SpecialKey) {
        val keyCode = when (key) {
            KeyboardView.SpecialKey.BACKSPACE -> KeyEvent.KEYCODE_DEL
            KeyboardView.SpecialKey.ENTER -> KeyEvent.KEYCODE_ENTER
            KeyboardView.SpecialKey.SPACE -> KeyEvent.KEYCODE_SPACE
            KeyboardView.SpecialKey.TAB -> KeyEvent.KEYCODE_TAB
            KeyboardView.SpecialKey.ESCAPE -> KeyEvent.KEYCODE_ESCAPE
            KeyboardView.SpecialKey.ARROW_UP -> KeyEvent.KEYCODE_DPAD_UP
            KeyboardView.SpecialKey.ARROW_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
            KeyboardView.SpecialKey.ARROW_LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
            KeyboardView.SpecialKey.ARROW_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            KeyboardView.SpecialKey.HOME -> KeyEvent.KEYCODE_MOVE_HOME
            KeyboardView.SpecialKey.END -> KeyEvent.KEYCODE_MOVE_END
            KeyboardView.SpecialKey.DELETE -> KeyEvent.KEYCODE_FORWARD_DEL
            KeyboardView.SpecialKey.SHIFT -> KeyEvent.KEYCODE_SHIFT_LEFT
            KeyboardView.SpecialKey.CTRL -> KeyEvent.KEYCODE_CTRL_LEFT
            KeyboardView.SpecialKey.ALT -> KeyEvent.KEYCODE_ALT_LEFT
            else -> return
        }
        injectKey(keyCode)
    }

    private fun injectKey(keyCode: Int) {
        if (shellService == null) {
            Log.w(TAG, "Shell service not available")
            return
        }
        
        Thread {
            try {
                shellService.injectKey(keyCode, KeyEvent.ACTION_DOWN)
                Thread.sleep(20)
                shellService.injectKey(keyCode, KeyEvent.ACTION_UP)
            } catch (e: Exception) {
                Log.e(TAG, "Key injection failed", e)
            }
        }.start()
    }

    private fun injectCharacter(char: Char) {
        if (shellService == null) {
            Log.w(TAG, "Shell service not available")
            return
        }

        val keyCode = getKeyCodeForChar(char)
        val needsShift = char.isUpperCase() || isShiftRequired(char)

        Thread {
            try {
                if (needsShift) {
                    shellService.injectKey(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.ACTION_DOWN)
                    Thread.sleep(10)
                }
                
                shellService.injectKey(keyCode, KeyEvent.ACTION_DOWN)
                Thread.sleep(20)
                shellService.injectKey(keyCode, KeyEvent.ACTION_UP)
                
                if (needsShift) {
                    Thread.sleep(10)
                    shellService.injectKey(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.ACTION_UP)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Character injection failed", e)
            }
        }.start()
    }

    private fun getKeyCodeForChar(char: Char): Int {
        return when (char.lowercaseChar()) {
            'a' -> KeyEvent.KEYCODE_A
            'b' -> KeyEvent.KEYCODE_B
            'c' -> KeyEvent.KEYCODE_C
            'd' -> KeyEvent.KEYCODE_D
            'e' -> KeyEvent.KEYCODE_E
            'f' -> KeyEvent.KEYCODE_F
            'g' -> KeyEvent.KEYCODE_G
            'h' -> KeyEvent.KEYCODE_H
            'i' -> KeyEvent.KEYCODE_I
            'j' -> KeyEvent.KEYCODE_J
            'k' -> KeyEvent.KEYCODE_K
            'l' -> KeyEvent.KEYCODE_L
            'm' -> KeyEvent.KEYCODE_M
            'n' -> KeyEvent.KEYCODE_N
            'o' -> KeyEvent.KEYCODE_O
            'p' -> KeyEvent.KEYCODE_P
            'q' -> KeyEvent.KEYCODE_Q
            'r' -> KeyEvent.KEYCODE_R
            's' -> KeyEvent.KEYCODE_S
            't' -> KeyEvent.KEYCODE_T
            'u' -> KeyEvent.KEYCODE_U
            'v' -> KeyEvent.KEYCODE_V
            'w' -> KeyEvent.KEYCODE_W
            'x' -> KeyEvent.KEYCODE_X
            'y' -> KeyEvent.KEYCODE_Y
            'z' -> KeyEvent.KEYCODE_Z
            '0', ')' -> KeyEvent.KEYCODE_0
            '1', '!' -> KeyEvent.KEYCODE_1
            '2', '@' -> KeyEvent.KEYCODE_2
            '3', '#' -> KeyEvent.KEYCODE_3
            '4', '$' -> KeyEvent.KEYCODE_4
            '5', '%' -> KeyEvent.KEYCODE_5
            '6', '^' -> KeyEvent.KEYCODE_6
            '7', '&' -> KeyEvent.KEYCODE_7
            '8', '*' -> KeyEvent.KEYCODE_8
            '9', '(' -> KeyEvent.KEYCODE_9
            ' ' -> KeyEvent.KEYCODE_SPACE
            '.' -> KeyEvent.KEYCODE_PERIOD
            ',' -> KeyEvent.KEYCODE_COMMA
            ';', ':' -> KeyEvent.KEYCODE_SEMICOLON
            '\'', '"' -> KeyEvent.KEYCODE_APOSTROPHE
            '/', '?' -> KeyEvent.KEYCODE_SLASH
            '\\', '|' -> KeyEvent.KEYCODE_BACKSLASH
            '[', '{' -> KeyEvent.KEYCODE_LEFT_BRACKET
            ']', '}' -> KeyEvent.KEYCODE_RIGHT_BRACKET
            '-', '_' -> KeyEvent.KEYCODE_MINUS
            '=', '+' -> KeyEvent.KEYCODE_EQUALS
            '`', '~' -> KeyEvent.KEYCODE_GRAVE
            '<' -> KeyEvent.KEYCODE_COMMA
            '>' -> KeyEvent.KEYCODE_PERIOD
            else -> KeyEvent.KEYCODE_UNKNOWN
        }
    }

    private fun isShiftRequired(char: Char): Boolean {
        return char in listOf(
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
            '_', '+', '{', '}', '|', ':', '"', '<', '>', '?', '~'
        )
    }
}
