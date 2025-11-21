package com.example.coverscreentester

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Display
import android.view.GestureDetector
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.coverscreentester.BuildConfig
import kotlin.math.abs
import kotlin.math.max

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    
    private var trackpadLayout: FrameLayout? = null
    private lateinit var trackpadParams: WindowManager.LayoutParams
    private var cursorLayout: FrameLayout? = null
    private var cursorView: ImageView? = null
    private lateinit var cursorParams: WindowManager.LayoutParams
    private var shellService: IShellService? = null
    private var isBound = false

    private var cursorX = 300f
    private var cursorY = 300f
    private var screenWidth = 0
    private var screenHeight = 0
    private var rotationAngle = 0 
    
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val sensitivity = 2.5f
    
    private var initialWindowX = 0
    private var initialWindowY = 0
    private var initialWindowWidth = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var aspectRatio = 0f
    
    // --- STATE FLAGS ---
    private var isTouchDragging = false
    private var isLeftKeyHeld = false
    
    // Right Click / Voice Logic
    private var isRightKeyHeld = false
    private var isRightDragPending = false // New flag to delay right click
    
    private var dragDownTime: Long = 0L
    private var isFocusActive = true 
    
    private val handler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable { startTouchDrag() }
    
    private var isResizing = false
    private val resizeLongPressRunnable = Runnable { startResize() }

    private val rotationRunnable3s = Runnable { performRotation() }
    private val rotationRunnable5s = Runnable { resetRotation() }

    private val voiceRunnable = Runnable { performVoice() }

    override fun onBind(intent: Intent?): IBinder? = null

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            shellService = IShellService.Stub.asInterface(binder)
            isBound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            isBound = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        if (trackpadLayout != null) return START_NOT_STICKY

        bindShizuku()
        val displayId = intent?.getIntExtra("DISPLAY_ID", Display.DEFAULT_DISPLAY)
            ?: Display.DEFAULT_DISPLAY
        setupWindows(displayId)
        return START_NOT_STICKY
    }

    private fun setupWindows(displayId: Int) {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(displayId)
        val context = createDisplayContext(display)
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val metrics = android.util.DisplayMetrics()
        display.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        cursorX = screenWidth / 2f
        cursorY = screenHeight / 2f

        // 1. CURSOR WINDOW
        cursorLayout = FrameLayout(context)
        cursorView = ImageView(context)
        cursorView?.setImageResource(R.drawable.ic_cursor)
        cursorLayout?.addView(cursorView, FrameLayout.LayoutParams(50, 50))
        cursorParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or 
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cursorParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        cursorParams.gravity = Gravity.TOP or Gravity.LEFT
        cursorParams.x = cursorX.toInt()
        cursorParams.y = cursorY.toInt()
        windowManager?.addView(cursorLayout, cursorParams)

        // 2. TRACKPAD WINDOW
        trackpadLayout = object : FrameLayout(context) {
            override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN) {
                    if (event.repeatCount == 0) {
                        when (event.keyCode) {
                            KeyEvent.KEYCODE_VOLUME_UP -> {
                                isLeftKeyHeld = true
                                startKeyDrag(MotionEvent.BUTTON_PRIMARY)
                                return true
                            }
                            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                                // 🚨 DELAYED LOGIC: Don't start drag immediately.
                                // Wait to see if it's a click, drag, or voice hold.
                                isRightDragPending = true
                                handler.postDelayed(voiceRunnable, 1000)
                                return true
                            }
                        }
                    } else { return true }
                }
                if (event?.action == KeyEvent.ACTION_UP) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_VOLUME_UP -> {
                            isLeftKeyHeld = false
                            stopKeyDrag(MotionEvent.BUTTON_PRIMARY)
                            return true
                        }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            handler.removeCallbacks(voiceRunnable)
                            
                            if (isRightDragPending) {
                                // It was a short press (tap) -> Do Right Click
                                performClick(true)
                                isRightDragPending = false
                            } else {
                                // It was a drag or voice action -> Stop drag if active
                                isRightKeyHeld = false
                                stopKeyDrag(MotionEvent.BUTTON_SECONDARY)
                            }
                            return true
                        }
                    }
                }
                return super.dispatchKeyEvent(event)
            }
        }
        
        val bg = GradientDrawable()
        bg.setColor(0x40000000)
        bg.cornerRadius = 30f
        bg.setStroke(2, 0x55FFFFFF.toInt())
        trackpadLayout?.background = bg
        
        trackpadLayout?.isFocusable = true
        trackpadLayout?.isFocusableInTouchMode = true

        val handleColor = 0x15FFFFFF.toInt()
        
        addHandle(context, Gravity.TOP or Gravity.RIGHT, handleColor) { v, e -> moveWindow(e) }
        addHandle(context, Gravity.BOTTOM or Gravity.RIGHT, handleColor) { v, e -> resizeWindow(e) }
        addHandle(context, Gravity.BOTTOM or Gravity.LEFT, handleColor) { v, e -> rotateWindow(e) }
        addHandle(context, Gravity.TOP or Gravity.LEFT, handleColor) { v, e -> voiceWindow(e) }

        trackpadParams = WindowManager.LayoutParams(
            400, 300,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        trackpadParams.gravity = Gravity.TOP or Gravity.LEFT
        trackpadParams.x = (screenWidth / 2) - 200
        trackpadParams.y = (screenHeight / 2) + 200
        trackpadParams.title = "TrackpadInput"
        aspectRatio = 400f / 300f

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!isTouchDragging && !isLeftKeyHeld && !isRightKeyHeld) performClick(false)
                return true
            }
        })

        trackpadLayout?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!isFocusActive) regainFocus()
                trackpadLayout?.requestFocus()
            }

            gestureDetector.onTouchEvent(event)
            handleTrackpadTouch(event)
            true
        }
        windowManager?.addView(trackpadLayout, trackpadParams)
        trackpadLayout?.requestFocus()
    }

    private fun addHandle(context: Context, gravity: Int, color: Int, onTouch: (View, MotionEvent) -> Boolean) {
        val handle = View(context)
        val bg = GradientDrawable()
        bg.setColor(color)
        bg.cornerRadii = floatArrayOf(15f,15f, 15f,15f, 15f,15f, 15f,15f)
        handle.background = bg
        val params = FrameLayout.LayoutParams(60, 60)
        params.gravity = gravity
        trackpadLayout?.addView(handle, params)
        handle.setOnTouchListener { v, event -> onTouch(v, event) }
    }

    private fun voiceWindow(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.postDelayed(voiceRunnable, 1000)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(voiceRunnable)
                if (isFocusActive) updateBorderColor(0x55FFFFFF.toInt())
                return true
            }
        }
        return false
    }

    private fun performVoice() {
        vibrate()
        
        // 🚨 Logic: If we enter Voice Mode, we are definitely NOT Right Clicking
        isRightDragPending = false 
        
        dropFocus()
        
        Thread {
            try { 
                shellService?.runCommand("am start -a android.intent.action.VOICE_COMMAND")
            } catch (e: Exception) {
                Log.e("OverlayService", "Voice Failed", e)
            }
        }.start()
    }

    private fun dropFocus() {
        if (!isFocusActive) return
        isFocusActive = false
        trackpadParams.flags = trackpadParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
        updateBorderColor(0xFFFF0000.toInt())
        Log.d("OverlayService", "Focus Dropped")
    }

    private fun regainFocus() {
        if (isFocusActive) return
        isFocusActive = true
        trackpadParams.flags = trackpadParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
        updateBorderColor(0x55FFFFFF.toInt())
        Log.d("OverlayService", "Focus Regained")
    }

    private fun moveWindow(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialWindowX = trackpadParams.x; initialWindowY = trackpadParams.y
                initialTouchX = event.rawX; initialTouchY = event.rawY; return true
            }
            MotionEvent.ACTION_MOVE -> {
                trackpadParams.x = initialWindowX + (event.rawX - initialTouchX).toInt()
                trackpadParams.y = initialWindowY + (event.rawY - initialTouchY).toInt()
                windowManager?.updateViewLayout(trackpadLayout, trackpadParams); return true
            }
        }
        return false
    }
    
    private fun resizeWindow(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.postDelayed(resizeLongPressRunnable, 1000)
                initialWindowWidth = trackpadParams.width; initialTouchX = event.rawX; return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isResizing) {
                    val deltaX = event.rawX - initialTouchX
                    var newWidth = max(200, (initialWindowWidth + deltaX).toInt())
                    var newHeight = (newWidth / aspectRatio).toInt()
                    trackpadParams.width = newWidth; trackpadParams.height = newHeight
                    windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
                } else {
                     if (abs(event.rawX - initialTouchX) > 10) handler.removeCallbacks(resizeLongPressRunnable)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(resizeLongPressRunnable)
                if (isResizing) stopResize()
                return true
            }
        }
        return false
    }
    
    private fun rotateWindow(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { handler.postDelayed(rotationRunnable3s, 3000); return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(rotationRunnable3s); handler.removeCallbacks(rotationRunnable5s)
                updateBorderColor(0x55FFFFFF.toInt()); return true
            }
        }
        return false
    }

    private fun handleTrackpadTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                handler.postDelayed(longPressRunnable, 400)
            }
            MotionEvent.ACTION_MOVE -> {
                val rawDx = (event.x - lastTouchX) * sensitivity
                val rawDy = (event.y - lastTouchY) * sensitivity
                var finalDx = rawDx
                var finalDy = rawDy
                when (rotationAngle) {
                    90 -> { finalDx = -rawDy; finalDy = rawDx }
                    180 -> { finalDx = -rawDx; finalDy = -rawDy }
                    270 -> { finalDx = rawDy; finalDy = -rawDx }
                }

                if (!isTouchDragging && (abs(rawDx) > 5 || abs(rawDy) > 5)) {
                    handler.removeCallbacks(longPressRunnable)
                    
                    // 🚨 DETECT DRAG INTENT: If moving while pending, assume it's a drag, not a click
                    if (isRightDragPending) {
                        isRightDragPending = false
                        handler.removeCallbacks(voiceRunnable)
                        isRightKeyHeld = true
                        startKeyDrag(MotionEvent.BUTTON_SECONDARY)
                    }
                }

                moveCursor(finalDx, finalDy)
                lastTouchX = event.x
                lastTouchY = event.y

                if (isTouchDragging) {
                    injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime)
                } else if (isLeftKeyHeld) {
                    injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_PRIMARY, dragDownTime)
                } else if (isRightKeyHeld) {
                    injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_SECONDARY, dragDownTime)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                if (isTouchDragging) stopTouchDrag()
            }
        }
    }

    private fun performRotation() {
        rotationAngle = (rotationAngle + 90) % 360
        cursorView?.rotation = rotationAngle.toFloat()
        vibrate()
        updateBorderColor(0xFFFFFF00.toInt()) 
        handler.postDelayed(rotationRunnable5s, 2000)
    }
    
    private fun resetRotation() {
        rotationAngle = 0; cursorView?.rotation = 0f; vibrateLong()
        updateBorderColor(0xFFFF0000.toInt())
        handler.postDelayed({ updateBorderColor(0x55FFFFFF.toInt()) }, 500)
    }

    private fun updateBorderColor(strokeColor: Int) {
        val bg = trackpadLayout?.background as? GradientDrawable ?: return
        bg.setStroke(4, strokeColor); trackpadLayout?.invalidate()
    }

    private fun moveCursor(dx: Float, dy: Float) {
        cursorX = (cursorX + dx).coerceIn(0f, screenWidth.toFloat())
        cursorY = (cursorY + dy).coerceIn(0f, screenHeight.toFloat())
        cursorParams.x = cursorX.toInt(); cursorParams.y = cursorY.toInt()
        windowManager?.updateViewLayout(cursorLayout, cursorParams)
    }

    private fun startKeyDrag(buttonState: Int) {
        vibrate()
        updateBorderColor(0xFF00FF00.toInt())
        dragDownTime = SystemClock.uptimeMillis()
        injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_MOUSE, buttonState, dragDownTime)
    }

    private fun stopKeyDrag(buttonState: Int) {
        updateBorderColor(0x55FFFFFF.toInt())
        injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_MOUSE, buttonState, dragDownTime)
    }

    private fun startTouchDrag() {
        isTouchDragging = true; vibrate(); updateBorderColor(0xFF00FF00.toInt())
        dragDownTime = SystemClock.uptimeMillis()
        injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime)
    }

    private fun stopTouchDrag() {
        isTouchDragging = false; updateBorderColor(0x55FFFFFF.toInt())
        injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime)
    }

    private fun startResize() { isResizing = true; vibrate(); updateBorderColor(0xFF0000FF.toInt()) }
    private fun stopResize() { isResizing = false; updateBorderColor(0x55FFFFFF.toInt()) }

    private fun injectAction(action: Int, source: Int, buttonState: Int, downTime: Long) {
        if (shellService == null) return
        val displayId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY
        Thread {
            try {
                shellService?.injectMouse(action, cursorX, cursorY, displayId, source, buttonState, downTime)
            } catch (e: Exception) {}
        }.start()
    }
    
    private fun performClick(isRightClick: Boolean) {
        if (shellService == null) { bindShizuku(); return }
        val displayId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY
        Thread {
            try {
                if (isRightClick) shellService?.execRightClick(cursorX, cursorY, displayId)
                else shellService?.execClick(cursorX, cursorY, displayId)
            } catch (e: Exception) {}
        }.start()
    }
    
    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        else v.vibrate(50)
    }
    
    private fun vibrateLong() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        else v.vibrate(200)
    }

    private fun bindShizuku() {
        try {
            val component = ComponentName(packageName, ShellUserService::class.java.name)
            ShizukuBinder.bind(component, userServiceConnection, BuildConfig.DEBUG, BuildConfig.VERSION_CODE)
        } catch (e: Exception) {}
    }
    private fun createNotification() {
        val channel = NotificationChannel("overlay_service", "Trackpad Active", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val notification = Notification.Builder(this, "overlay_service").setContentTitle("Trackpad Active").setSmallIcon(R.mipmap.ic_launcher).build()
        startForeground(1, notification)
    }
    override fun onDestroy() {
        super.onDestroy()
        if (trackpadLayout != null) windowManager?.removeView(trackpadLayout)
        if (cursorLayout != null) windowManager?.removeView(cursorLayout)
        if (isBound) ShizukuBinder.unbind(ComponentName(packageName, ShellUserService::class.java.name), userServiceConnection)
    }
}
