package com.example.coverscreentester

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ServiceInfo
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
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs
import kotlin.math.max
import java.util.ArrayList
import com.example.coverscreentester.BuildConfig

class OverlayService : AccessibilityService(), DisplayManager.DisplayListener {

    private var windowManager: WindowManager? = null
    private var displayManager: DisplayManager? = null
    private var trackpadLayout: FrameLayout? = null
    private lateinit var trackpadParams: WindowManager.LayoutParams
    private var cursorLayout: FrameLayout? = null
    private var cursorView: ImageView? = null
    private lateinit var cursorParams: WindowManager.LayoutParams
    private var shellService: IShellService? = null
    private var isBound = false
    
    // KEYBOARD OVERLAY
    private var keyboardOverlay: KeyboardOverlay? = null
    private var isKeyboardVisible = false
    
    private var currentDisplayId = -1 
    private var lastLoadedProfileKey = ""

    // State Variables
    private var cursorX = 300f
    private var cursorY = 300f
    private var virtualScrollX = 0f
    private var virtualScrollY = 0f
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 1.0f
    private var rotationAngle = 0 
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    private var initialWindowX = 0
    private var initialWindowY = 0
    private var initialWindowWidth = 0
    private var initialWindowHeight = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isTouchDragging = false
    private var isLeftKeyHeld = false
    private var isRightKeyHeld = false
    private var isRightDragPending = false 
    private var isVScrolling = false
    private var isHScrolling = false
    private var scrollZoneThickness = 60 
    private var prefVibrate = true
    private var prefReverseScroll = true
    private var prefAlpha = 200
    private var prefHandleSize = 60 
    private var prefVPosLeft = false
    private var prefHPosTop = false
    private var prefLocked = false
    private var prefHandleTouchSize = 60
    private var prefScrollTouchSize = 60
    private var prefScrollVisualSize = 4
    private var cursorSpeed = 2.5f
    private var scrollSpeed = 3.0f 
    private var dragDownTime: Long = 0L
    
    private var isKeyboardMode = false 
    private var savedWindowX = 0
    private var savedWindowY = 0
    
    private var currentBorderColor = 0xFFFFFFFF.toInt()
    private var highlightAlpha = false
    private var highlightHandles = false
    private var highlightScrolls = false
    private val handleContainers = ArrayList<FrameLayout>()
    private val handleVisuals = ArrayList<View>()
    private var vScrollContainer: FrameLayout? = null
    private var hScrollContainer: FrameLayout? = null
    private var vScrollVisual: View? = null
    private var hScrollVisual: View? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable { startTouchDrag() }
    private var isResizing = false
    private val resizeLongPressRunnable = Runnable { startResize() }
    private var isMoving = false
    private val moveLongPressRunnable = Runnable { startMove() }
    private val voiceRunnable = Runnable { toggleKeyboardMode() }
    
    private val clearHighlightsRunnable = Runnable {
        highlightAlpha = false
        highlightHandles = false
        highlightScrolls = false
        updateBorderColor(currentBorderColor)
        updateLayoutSizes() 
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (!isLeftKeyHeld) {
                    isLeftKeyHeld = true
                    startKeyDrag(MotionEvent.BUTTON_PRIMARY)
                }
            } else if (action == KeyEvent.ACTION_UP) {
                isLeftKeyHeld = false
                stopKeyDrag(MotionEvent.BUTTON_PRIMARY)
            }
            return true 
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (action == KeyEvent.ACTION_DOWN) {
                if (!isRightDragPending) {
                    isRightDragPending = true
                    handler.postDelayed(voiceRunnable, 1000)
                }
            } else if (action == KeyEvent.ACTION_UP) {
                handler.removeCallbacks(voiceRunnable)
                if (isRightDragPending) {
                    performClick(true) 
                    isRightDragPending = false
                }
            }
            return true 
        }
        return super.onKeyEvent(event)
    }

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            shellService = IShellService.Stub.asInterface(binder)
            isBound = true
            initKeyboardOverlay()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            shellService = null
            isBound = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displayManager?.registerDisplayListener(this, handler)
        } catch (e: Exception) {
            Log.e("OverlayService", "Failed to init DisplayManager", e)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            createNotification() 
            bindShizuku()
            loadPrefs()
            
            if (displayManager == null) {
                displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                displayManager?.registerDisplayListener(this, handler)
            }
            
            setupWindows(Display.DEFAULT_DISPLAY) 
        } catch (e: Exception) {
            Log.e("OverlayService", "Crash in onServiceConnected", e)
        }
    }

    override fun onDisplayAdded(displayId: Int) {}
    override fun onDisplayRemoved(displayId: Int) {}
    override fun onDisplayChanged(displayId: Int) {
        if (displayId == currentDisplayId) {
            updateScreenMetrics(displayId)
            val newKey = getProfileKey()
            if (newKey != lastLoadedProfileKey) {
                loadLayout() 
            }
        }
    }

    private fun updateScreenMetrics(displayId: Int) {
        try {
            if (displayManager == null) displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager?.getDisplay(displayId) ?: return
            val metrics = android.util.DisplayMetrics()
            display.getRealMetrics(metrics)
            if (metrics.widthPixels > 0 && metrics.heightPixels > 0) {
                screenWidth = metrics.widthPixels
                screenHeight = metrics.heightPixels
                screenDensity = metrics.density
                cursorX = (screenWidth / 2f).coerceAtMost(screenWidth.toFloat())
                cursorY = (screenHeight / 2f).coerceAtMost(screenHeight.toFloat())
                keyboardOverlay?.setScreenDimensions(screenWidth, screenHeight)
            }
        } catch (e: Exception) {
            Log.e("OverlayService", "Metric Update Failed", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent?.action != null) {
                when (intent.action) {
                    "RESET_POSITION" -> resetTrackpadPosition()
                    "ROTATE" -> performRotation()
                    "SAVE_LAYOUT" -> saveLayout()
                    "LOAD_LAYOUT" -> loadLayout()
                    "DELETE_PROFILE" -> deleteCurrentProfile()
                    "MANUAL_ADJUST" -> handleManualAdjust(intent)
                    "TOGGLE_KEYBOARD" -> toggleKeyboard()
                    "RELOAD_PREFS" -> {
                        loadPrefs()
                        updateBorderColor(currentBorderColor)
                        updateLayoutSizes()
                        updateScrollPosition()
                    }
                    "PREVIEW_UPDATE" -> handlePreview(intent)
                }
            }
            
            if (intent?.hasExtra("DISPLAY_ID") == true) {
                val targetDisplayId = intent.getIntExtra("DISPLAY_ID", Display.DEFAULT_DISPLAY)
                if (targetDisplayId != currentDisplayId || trackpadLayout == null) {
                    removeOverlays()
                    setupWindows(targetDisplayId)
                }
            }
        } catch (e: Exception) {
            Log.e("OverlayService", "Crash in onStartCommand", e)
        }
        return START_STICKY
    }
    
    private fun initKeyboardOverlay() {
        if (windowManager == null || shellService == null) return
        keyboardOverlay = KeyboardOverlay(this, windowManager!!, shellService, currentDisplayId)
        keyboardOverlay?.setScreenDimensions(screenWidth, screenHeight)
        keyboardOverlay?.loadKeyboardSize()
    }
    
    private fun toggleKeyboard() {
        if (keyboardOverlay == null && shellService != null) {
            initKeyboardOverlay()
        }
        keyboardOverlay?.toggle()
        isKeyboardVisible = keyboardOverlay?.isShowing() ?: false
        if (isKeyboardVisible) {
            updateBorderColor(0xFFFF9800.toInt())
        } else {
            updateBorderColor(0x55FFFFFF.toInt())
        }
        vibrate()
    }
    
    private fun handleManualAdjust(intent: Intent) {
        if (windowManager == null || trackpadLayout == null) return
        val dx = intent.getIntExtra("DX", 0)
        val dy = intent.getIntExtra("DY", 0)
        val dw = intent.getIntExtra("DW", 0)
        val dh = intent.getIntExtra("DH", 0)
        trackpadParams.x += dx
        trackpadParams.y += dy
        trackpadParams.width = max(200, trackpadParams.width + dw)
        trackpadParams.height = max(200, trackpadParams.height + dh)
        try {
            windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
            saveLayout()
        } catch (e: Exception) {}
    }
    
    private fun removeOverlays() {
        try {
            keyboardOverlay?.hide()
            if (trackpadLayout != null) {
                windowManager?.removeView(trackpadLayout)
                trackpadLayout = null
            }
            if (cursorLayout != null) {
                windowManager?.removeView(cursorLayout)
                cursorLayout = null
            }
        } catch (e: Exception) {}
    }

    private fun setupWindows(displayId: Int) {
        if (trackpadLayout != null && displayId == currentDisplayId) return

        try {
            updateScreenMetrics(displayId)
            if (screenWidth == 0) updateScreenMetrics(displayId)

            val displayContext = createDisplayContext(displayManager!!.getDisplay(displayId))
            windowManager = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            currentDisplayId = displayId
            
            cursorLayout = FrameLayout(displayContext)
            cursorView = ImageView(displayContext)
            cursorView?.setImageResource(R.drawable.ic_cursor)
            cursorLayout?.addView(cursorView, FrameLayout.LayoutParams(50, 50))
            
            cursorParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, 
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

            trackpadLayout = object : FrameLayout(displayContext) {}
            val bg = GradientDrawable()
            bg.cornerRadius = 30f
            trackpadLayout?.background = bg
            updateBorderColor(0x55FFFFFF.toInt())
            trackpadLayout?.isFocusable = true
            trackpadLayout?.isFocusableInTouchMode = true

            handleContainers.clear()
            handleVisuals.clear()
            val handleColor = 0x15FFFFFF.toInt()
            addHandle(displayContext, Gravity.TOP or Gravity.RIGHT, handleColor) { _, e -> moveWindow(e) }
            addHandle(displayContext, Gravity.BOTTOM or Gravity.RIGHT, handleColor) { _, e -> resizeWindow(e) }
            addHandle(displayContext, Gravity.BOTTOM or Gravity.LEFT, handleColor) { _, e -> openMenuHandle(e) }
            addHandle(displayContext, Gravity.TOP or Gravity.LEFT, handleColor) { _, e -> keyboardHandle(e) }
            addScrollBars(displayContext)

            trackpadParams = WindowManager.LayoutParams(
                400, 300,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            trackpadParams.gravity = Gravity.TOP or Gravity.LEFT
            trackpadParams.title = "TrackpadInput"
            
            resetTrackpadPosition()
            
            val gestureDetector = GestureDetector(displayContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    if (!isTouchDragging && !isLeftKeyHeld && !isRightKeyHeld && !isVScrolling && !isHScrolling) performClick(false)
                    return true
                }
            })

            trackpadLayout?.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                handleTrackpadTouch(event)
                true
            }
            
            windowManager?.addView(trackpadLayout, trackpadParams)
            loadLayout()
            
            if (shellService != null) {
                initKeyboardOverlay()
            }
            
        } catch (e: Exception) {
            Log.e("OverlayService", "Setup Windows Crash", e)
        }
    }

    private fun getProfileKey(): String {
        if (screenHeight == 0) return "profile_1.0"
        val ratio = screenWidth.toFloat() / screenHeight.toFloat()
        return "profile_" + String.format("%.1f", ratio)
    }

    private fun saveLayout() { 
        if (trackpadLayout == null || screenWidth == 0 || screenHeight == 0) return
        val key = getProfileKey()
        lastLoadedProfileKey = key 
        val xPct = trackpadParams.x.toFloat() / screenWidth.toFloat()
        val yPct = trackpadParams.y.toFloat() / screenHeight.toFloat()
        val wPct = trackpadParams.width.toFloat() / screenWidth.toFloat()
        val hPct = trackpadParams.height.toFloat() / screenHeight.toFloat()
        val p = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        p.edit()
            .putFloat("${key}_xp", xPct)
            .putFloat("${key}_yp", yPct)
            .putFloat("${key}_wp", wPct)
            .putFloat("${key}_hp", hPct)
            .putBoolean("${key}_saved", true)
            .apply()
        vibrate() 
    }

    private fun loadLayout() { 
        if (trackpadLayout == null || windowManager == null) return
        val key = getProfileKey()
        lastLoadedProfileKey = key
        val p = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        if (p.getBoolean("${key}_saved", false)) {
            val xPct = p.getFloat("${key}_xp", 0.1f)
            val yPct = p.getFloat("${key}_yp", 0.1f)
            val wPct = p.getFloat("${key}_wp", 0.5f)
            val hPct = p.getFloat("${key}_hp", 0.4f)
            val calcW = (wPct * screenWidth).toInt()
            val calcH = (hPct * screenHeight).toInt()
            trackpadParams.width = calcW.coerceAtLeast(300)
            trackpadParams.height = calcH.coerceAtLeast(300)
            trackpadParams.x = (xPct * screenWidth).toInt()
            trackpadParams.y = (yPct * screenHeight).toInt()
        } else {
            trackpadParams.width = 400
            trackpadParams.height = 300
            trackpadParams.x = (screenWidth / 2) - 200
            trackpadParams.y = (screenHeight / 2) - 150
        }
        try { windowManager?.updateViewLayout(trackpadLayout, trackpadParams) } catch (e: Exception) {} 
    }
    
    private fun deleteCurrentProfile() {
        val key = getProfileKey()
        getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE).edit()
            .remove("${key}_saved").remove("${key}_xp").remove("${key}_yp")
            .remove("${key}_wp").remove("${key}_hp").apply()
        resetTrackpadPosition()
    }

    private fun resetTrackpadPosition() { 
        if (windowManager == null || trackpadLayout == null) return
        trackpadParams.width = 400
        trackpadParams.height = 300
        val centerX = (screenWidth / 2) - 200
        val centerY = (screenHeight / 2) - 150
        trackpadParams.x = if (centerX > 0) centerX else 100
        trackpadParams.y = if (centerY > 0) centerY else 100
        try { windowManager?.updateViewLayout(trackpadLayout, trackpadParams); vibrate() } catch (e: Exception) {} 
    }

    private fun moveWindow(event: MotionEvent): Boolean { 
        if (prefLocked) return true
        when (event.action) { 
            MotionEvent.ACTION_DOWN -> { 
                handler.postDelayed(moveLongPressRunnable, 1000)
                initialWindowX = trackpadParams.x
                initialWindowY = trackpadParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true 
            }
            MotionEvent.ACTION_MOVE -> { 
                if (isMoving) { 
                    trackpadParams.x = initialWindowX + (event.rawX - initialTouchX).toInt()
                    trackpadParams.y = initialWindowY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(trackpadLayout, trackpadParams) 
                } else if (abs(event.rawX - initialTouchX) > 20) {
                    handler.removeCallbacks(moveLongPressRunnable)
                }
                return true 
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { 
                handler.removeCallbacks(moveLongPressRunnable)
                if (isMoving) stopMove()
                return true 
            } 
        }
        return false 
    }
    
    private fun startMove() { isMoving = true; vibrate(); updateBorderColor(0xFF0000FF.toInt()) }
    private fun stopMove() { isMoving = false; updateBorderColor(0x55FFFFFF.toInt()) }
    
    private fun resizeWindow(event: MotionEvent): Boolean { 
        if (prefLocked) return true
        when (event.action) { 
            MotionEvent.ACTION_DOWN -> { 
                handler.postDelayed(resizeLongPressRunnable, 1000)
                initialWindowWidth = trackpadParams.width
                initialWindowHeight = trackpadParams.height
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true 
            }
            MotionEvent.ACTION_MOVE -> { 
                if (isResizing) { 
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    trackpadParams.width = max(300, (initialWindowWidth + deltaX).toInt())
                    trackpadParams.height = max(300, (initialWindowHeight + deltaY).toInt())
                    windowManager?.updateViewLayout(trackpadLayout, trackpadParams) 
                } else if (abs(event.rawX - initialTouchX) > 20) {
                    handler.removeCallbacks(resizeLongPressRunnable)
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
    
    private fun startResize() { isResizing = true; vibrate(); updateBorderColor(0xFF0000FF.toInt()) }
    private fun stopResize() { isResizing = false; updateBorderColor(0x55FFFFFF.toInt()) }
    
    private fun handleTrackpadTouch(event: MotionEvent) {
        val viewWidth = trackpadLayout?.width ?: 0
        val viewHeight = trackpadLayout?.height ?: 0
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                val inVZone = if (prefVPosLeft) event.x < scrollZoneThickness else event.x > (viewWidth - scrollZoneThickness)
                val inHZone = if (prefHPosTop) event.y < scrollZoneThickness else event.y > (viewHeight - scrollZoneThickness)
                if (inVZone) { 
                    isVScrolling = true
                    vibrate()
                    updateBorderColor(0xFF00FFFF.toInt())
                    virtualScrollX = cursorX
                    virtualScrollY = cursorY
                    dragDownTime = SystemClock.uptimeMillis()
                    injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) 
                } else if (inHZone) { 
                    isHScrolling = true
                    vibrate()
                    updateBorderColor(0xFF00FFFF.toInt())
                    virtualScrollX = cursorX
                    virtualScrollY = cursorY
                    dragDownTime = SystemClock.uptimeMillis()
                    injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) 
                } else {
                    handler.postDelayed(longPressRunnable, 400)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val rawDx = (event.x - lastTouchX) * cursorSpeed
                val rawDy = (event.y - lastTouchY) * cursorSpeed
                if (isVScrolling) {
                    val dist = (event.y - lastTouchY) * scrollSpeed
                    if (abs(dist) > 0) { 
                        if (prefReverseScroll) virtualScrollY += dist else virtualScrollY -= dist
                        injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) 
                    }
                } else if (isHScrolling) {
                    val dist = (event.x - lastTouchX) * scrollSpeed
                    if (abs(dist) > 0) { 
                        if (prefReverseScroll) virtualScrollX += dist else virtualScrollX -= dist
                        injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) 
                    }
                } else {
                    var finalDx = rawDx
                    var finalDy = rawDy
                    when (rotationAngle) { 
                        90 -> { finalDx = -rawDy; finalDy = rawDx }
                        180 -> { finalDx = -rawDx; finalDy = -rawDy }
                        270 -> { finalDx = rawDy; finalDy = -rawDx } 
                    }
                    if (!isTouchDragging && (abs(rawDx) > 5 || abs(rawDy) > 5)) { 
                        handler.removeCallbacks(longPressRunnable)
                        if (isRightDragPending) { 
                            isRightDragPending = false
                            handler.removeCallbacks(voiceRunnable)
                            isRightKeyHeld = true
                            startKeyDrag(MotionEvent.BUTTON_SECONDARY) 
                        } 
                    }
                    moveCursor(finalDx, finalDy)
                    if (isTouchDragging) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime)
                    else if (isLeftKeyHeld) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_PRIMARY, dragDownTime)
                    else if (isRightKeyHeld) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_SECONDARY, dragDownTime)
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { 
                handler.removeCallbacks(longPressRunnable)
                if (isTouchDragging) stopTouchDrag()
                if (isVScrolling || isHScrolling) { 
                    injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY)
                    isVScrolling = false
                    isHScrolling = false
                    updateBorderColor(0x55FFFFFF.toInt()) 
                } 
            }
        }
    }
    
    private fun performRotation() { 
        rotationAngle = (rotationAngle + 90) % 360
        cursorView?.rotation = rotationAngle.toFloat()
        vibrate()
        updateBorderColor(0xFFFFFF00.toInt()) 
    }
    
    private fun moveCursor(dx: Float, dy: Float) { 
        cursorX = (cursorX + dx).coerceIn(0f, screenWidth.toFloat())
        cursorY = (cursorY + dy).coerceIn(0f, screenHeight.toFloat())
        cursorParams.x = cursorX.toInt()
        cursorParams.y = cursorY.toInt()
        try { windowManager?.updateViewLayout(cursorLayout, cursorParams) } catch (e: Exception) {} 
    }
    
    private fun startKeyDrag(b: Int) { 
        vibrate()
        updateBorderColor(0xFF00FF00.toInt())
        dragDownTime = SystemClock.uptimeMillis()
        injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_MOUSE, b, dragDownTime) 
    }
    
    private fun stopKeyDrag(b: Int) { 
        updateBorderColor(0x55FFFFFF.toInt())
        injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_MOUSE, b, dragDownTime) 
    }
    
    private fun startTouchDrag() { 
        isTouchDragging = true
        vibrate()
        updateBorderColor(0xFF00FF00.toInt())
        dragDownTime = SystemClock.uptimeMillis()
        injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime) 
    }
    
    private fun stopTouchDrag() { 
        isTouchDragging = false
        updateBorderColor(0x55FFFFFF.toInt())
        injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime) 
    }
    
    private fun injectAction(a: Int, s: Int, b: Int, t: Long, x: Float = cursorX, y: Float = cursorY) { 
        if (shellService == null) return
        val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY
        Thread { try { shellService?.injectMouse(a, x, y, dId, s, b, t) } catch (e: Exception) {} }.start() 
    }
    
    private fun performClick(r: Boolean) { 
        if (shellService == null) { bindShizuku(); return }
        val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY
        Thread { try { if (r) shellService?.execRightClick(cursorX, cursorY, dId) else shellService?.execClick(cursorX, cursorY, dId) } catch (e: Exception) {} }.start() 
    }
    
    private fun vibrate() { 
        if (!prefVibrate) return
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) else v.vibrate(50) 
    }
    
    private fun bindShizuku() { 
        try { 
            val c = ComponentName(packageName, ShellUserService::class.java.name)
            ShizukuBinder.bind(c, userServiceConnection, BuildConfig.DEBUG, BuildConfig.VERSION_CODE) 
        } catch (e: Exception) {} 
    }
    
    private fun createNotification() { 
        val c = NotificationChannel("overlay_service", "Trackpad Active", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c)
        val n = Notification.Builder(this, "overlay_service").setContentTitle("Trackpad Active").setSmallIcon(R.mipmap.ic_launcher).build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(1, n) 
    }
    
    override fun onDestroy() { 
        super.onDestroy()
        displayManager?.unregisterDisplayListener(this)
        keyboardOverlay?.hide()
        if (trackpadLayout != null) windowManager?.removeView(trackpadLayout)
        if (cursorLayout != null) windowManager?.removeView(cursorLayout)
        if (isBound) ShizukuBinder.unbind(ComponentName(packageName, ShellUserService::class.java.name), userServiceConnection) 
    }
    
    private fun toggleKeyboardMode() {
        vibrate()
        isRightDragPending = false
        if (!isKeyboardMode) {
            isKeyboardMode = true
            savedWindowX = trackpadParams.x
            savedWindowY = trackpadParams.y
            trackpadParams.x = screenWidth - trackpadParams.width
            trackpadParams.y = 0
            windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
            updateBorderColor(0xFFFF0000.toInt())
        } else {
            isKeyboardMode = false
            trackpadParams.x = savedWindowX
            trackpadParams.y = savedWindowY
            windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
            updateBorderColor(0x55FFFFFF.toInt())
        }
    }

    private fun openMenuHandle(event: MotionEvent): Boolean { 
        if (event.action == MotionEvent.ACTION_DOWN) { 
            vibrate()
            val intent = Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            startActivity(intent)
            return true 
        }
        return false 
    }
    
    private fun keyboardHandle(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            toggleKeyboard()
            return true
        }
        return false
    }
    
    private fun loadPrefs() { 
        val p = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        cursorSpeed = p.getFloat("cursor_speed", 2.5f)
        scrollSpeed = p.getFloat("scroll_speed", 3.0f)
        prefVibrate = p.getBoolean("vibrate", true)
        prefReverseScroll = p.getBoolean("reverse_scroll", true)
        prefAlpha = p.getInt("alpha", 200)
        prefLocked = p.getBoolean("lock_position", false)
        prefVPosLeft = p.getBoolean("v_pos_left", false)
        prefHPosTop = p.getBoolean("h_pos_top", false)
        prefHandleTouchSize = p.getInt("handle_touch_size", 60)
        prefScrollTouchSize = p.getInt("scroll_touch_size", 60)
        prefHandleSize = p.getInt("handle_size", 60)
        prefScrollVisualSize = p.getInt("scroll_visual_size", 4)
        scrollZoneThickness = prefScrollTouchSize 
    }
    
    private fun handlePreview(intent: Intent) { 
        val t = intent.getStringExtra("TARGET")
        val v = intent.getIntExtra("VALUE", 0)
        handler.removeCallbacks(clearHighlightsRunnable)
        when (t) { 
            "alpha" -> { prefAlpha = v; highlightAlpha = true; updateBorderColor(currentBorderColor) }
            "handle_touch" -> { prefHandleTouchSize = v; highlightHandles = true; updateLayoutSizes() }
            "scroll_touch" -> { prefScrollTouchSize = v; scrollZoneThickness = v; highlightScrolls = true; updateLayoutSizes(); updateScrollPosition() }
            "handle_size" -> { prefHandleSize = v; highlightHandles = true; updateHandleSize() }
            "scroll_visual" -> { prefScrollVisualSize = v; highlightScrolls = true; updateLayoutSizes() } 
        }
        handler.postDelayed(clearHighlightsRunnable, 1500) 
    }
    
    private fun addHandle(context: Context, gravity: Int, color: Int, onTouch: (View, MotionEvent) -> Boolean) { 
        val c = FrameLayout(context)
        val cp = FrameLayout.LayoutParams(prefHandleTouchSize, prefHandleTouchSize)
        cp.gravity = gravity
        val v = View(context)
        val bg = GradientDrawable()
        bg.setColor(color)
        bg.cornerRadii = floatArrayOf(15f,15f, 15f,15f, 15f,15f, 15f,15f)
        v.background = bg
        val vp = FrameLayout.LayoutParams(prefHandleSize, prefHandleSize)
        vp.gravity = Gravity.CENTER
        c.addView(v, vp)
        handleVisuals.add(v)
        handleContainers.add(c)
        trackpadLayout?.addView(c, cp)
        c.setOnTouchListener { view, e -> onTouch(view, e) } 
    }
    
    private fun updateHandleSize() { 
        for (v in handleVisuals) { 
            val p = v.layoutParams
            p.width = prefHandleSize
            p.height = prefHandleSize
            v.layoutParams = p 
        } 
    }
    
    private fun updateLayoutSizes() { 
        for (c in handleContainers) { 
            val p = c.layoutParams
            p.width = prefHandleTouchSize
            p.height = prefHandleTouchSize
            c.layoutParams = p 
        }
        updateScrollPosition() 
    } 
    
    private fun addScrollBars(context: Context) { 
        val m = prefHandleTouchSize + 10
        vScrollContainer = FrameLayout(context)
        val vp = FrameLayout.LayoutParams(prefScrollTouchSize, FrameLayout.LayoutParams.MATCH_PARENT)
        vp.gravity = if (prefVPosLeft) Gravity.LEFT else Gravity.RIGHT
        vp.setMargins(0, m, 0, m)
        trackpadLayout?.addView(vScrollContainer, vp)
        vScrollVisual = View(context)
        vScrollVisual!!.setBackgroundColor(0x30FFFFFF.toInt())
        val vvp = FrameLayout.LayoutParams(prefScrollVisualSize, FrameLayout.LayoutParams.MATCH_PARENT)
        vvp.gravity = Gravity.CENTER_HORIZONTAL
        vScrollContainer?.addView(vScrollVisual, vvp)
        hScrollContainer = FrameLayout(context)
        val hp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, prefScrollTouchSize)
        hp.gravity = if (prefHPosTop) Gravity.TOP else Gravity.BOTTOM
        hp.setMargins(m, 0, m, 0)
        trackpadLayout?.addView(hScrollContainer, hp)
        hScrollVisual = View(context)
        hScrollVisual!!.setBackgroundColor(0x30FFFFFF.toInt())
        val hvp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, prefScrollVisualSize)
        hvp.gravity = Gravity.CENTER_VERTICAL
        hScrollContainer?.addView(hScrollVisual, hvp) 
    }
    
    private fun updateScrollPosition() { 
        val m = prefHandleTouchSize + 10
        if (vScrollContainer != null) { 
            val vp = vScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            vp.gravity = if (prefVPosLeft) Gravity.LEFT else Gravity.RIGHT
            vp.setMargins(0, m, 0, m)
            vScrollContainer!!.layoutParams = vp 
        }
        if (hScrollContainer != null) { 
            val hp = hScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            hp.gravity = if (prefHPosTop) Gravity.TOP else Gravity.BOTTOM
            hp.setMargins(m, 0, m, 0)
            hScrollContainer!!.layoutParams = hp 
        } 
    }
    
    private fun updateBorderColor(strokeColor: Int) { 
        currentBorderColor = strokeColor
        val bg = trackpadLayout?.background as? GradientDrawable ?: return
        bg.setColor(Color.TRANSPARENT)
        val colorWithAlpha = (strokeColor and 0x00FFFFFF) or (prefAlpha shl 24)
        bg.setStroke(4, if (highlightAlpha) 0xFF00FF00.toInt() else colorWithAlpha)
        trackpadLayout?.invalidate() 
    }
}
