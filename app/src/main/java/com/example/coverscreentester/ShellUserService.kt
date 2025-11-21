package com.example.coverscreentester

import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import com.example.coverscreentester.IShellService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method

class ShellUserService : IShellService.Stub() {

    private lateinit var TAG: String
    private lateinit var inputManager: Any
    private lateinit var injectInputEventMethod: Method
    private val INJECT_MODE_ASYNC = 0

    init {
        TAG = "ShellUserService"
        setupReflection()
    }

    private fun setupReflection() {
        try {
            val imClass = Class.forName("android.hardware.input.InputManager")
            val getInstance = imClass.getMethod("getInstance")
            inputManager = getInstance.invoke(null)!!

            injectInputEventMethod = imClass.getMethod(
                "injectInputEvent",
                InputEvent::class.java,
                Int::class.javaPrimitiveType
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup reflection", e)
        }
    }

    private fun setDisplayId(event: MotionEvent, displayId: Int) {
        try {
            val method = MotionEvent::class.java.getMethod("setDisplayId", Int::class.javaPrimitiveType)
            method.invoke(event, displayId)
        } catch (e: Exception) {
            Log.e(TAG, "Could not set displayId", e)
        }
    }

    override fun injectTouch(action: Int, x: Float, y: Float, displayId: Int) {
       // Not used
    }

    override fun execClick(x: Float, y: Float, displayId: Int) {
        val downTime = SystemClock.uptimeMillis()
        injectInternal(MotionEvent.ACTION_DOWN, x, y, displayId, downTime, downTime, InputDevice.SOURCE_TOUCHSCREEN, 0)
        try { Thread.sleep(50) } catch (e: InterruptedException) {}
        injectInternal(MotionEvent.ACTION_UP, x, y, displayId, downTime, SystemClock.uptimeMillis(), InputDevice.SOURCE_TOUCHSCREEN, 0)
    }

    override fun execRightClick(x: Float, y: Float, displayId: Int) {
        val downTime = SystemClock.uptimeMillis()
        injectInternal(MotionEvent.ACTION_DOWN, x, y, displayId, downTime, downTime, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_SECONDARY)
        try { Thread.sleep(50) } catch (e: InterruptedException) {}
        injectInternal(MotionEvent.ACTION_UP, x, y, displayId, downTime, SystemClock.uptimeMillis(), InputDevice.SOURCE_MOUSE, 0)
    }

    override fun injectMouse(action: Int, x: Float, y: Float, displayId: Int, source: Int, buttonState: Int, downTime: Long) {
        injectInternal(action, x, y, displayId, downTime, SystemClock.uptimeMillis(), source, buttonState)
    }

    // 🚨 RESTORED: Key Injection
    override fun execKey(keyCode: Int) {
        val now = SystemClock.uptimeMillis()
        injectInternalKey(KeyEvent.ACTION_DOWN, keyCode, now)
        try { Thread.sleep(50) } catch (e: InterruptedException) {}
        injectInternalKey(KeyEvent.ACTION_UP, keyCode, SystemClock.uptimeMillis())
    }

    private fun injectInternalKey(action: Int, keyCode: Int, eventTime: Long) {
        if (!this::inputManager.isInitialized || !this::injectInputEventMethod.isInitialized) return
        
        try {
            // Keys are injected to the default source (Keyboard)
            val event = KeyEvent(eventTime, eventTime, action, keyCode, 0)
            injectInputEventMethod.invoke(inputManager, event, INJECT_MODE_ASYNC)
        } catch (e: Exception) {
            Log.e(TAG, "Key injection failed", e)
        }
    }

    private fun injectInternal(action: Int, x: Float, y: Float, displayId: Int, downTime: Long, eventTime: Long, source: Int, buttonState: Int) {
        if (!this::inputManager.isInitialized || !this::injectInputEventMethod.isInitialized) return

        val props = PointerProperties()
        props.id = 0
        props.toolType = if (source == InputDevice.SOURCE_MOUSE) MotionEvent.TOOL_TYPE_MOUSE else MotionEvent.TOOL_TYPE_FINGER

        val coords = PointerCoords()
        coords.x = x
        coords.y = y
        coords.pressure = if (buttonState != 0 || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) 1.0f else 0.0f
        coords.size = 1.0f

        var event: MotionEvent? = null
        try {
            event = MotionEvent.obtain(
                downTime,
                eventTime,
                action,
                1, arrayOf(props), arrayOf(coords),
                0, buttonState, 1.0f, 1.0f, 0, 0, 
                source, 0
            )
            
            setDisplayId(event, displayId)
            injectInputEventMethod.invoke(inputManager, event, INJECT_MODE_ASYNC)
        } catch (e: Exception) {
            Log.e(TAG, "Injection failed", e)
        } finally {
            event?.recycle()
        }
    }

    override fun runCommand(cmd: String?): String { return "" }
}
