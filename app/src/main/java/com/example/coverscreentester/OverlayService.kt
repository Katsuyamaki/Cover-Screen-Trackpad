package com.example.coverscreentester

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs
import kotlin.math.max
import java.util.ArrayList
import com.example.coverscreentester.BuildConfig

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
    private var virtualScrollX = 0f
    private var virtualScrollY = 0f
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
    
    private var isTouchDragging = false
    private var isLeftKeyHeld = false
    private var isRightKeyHeld = false
    private var isRightDragPending = false 
    
    private var isVScrolling = false
    private var isHScrolling = false
    private val scrollSensitivity = 3.0f 
    private var scrollZoneThickness = 60 
    
    // --- PREFS ---
    private var prefVibrate = true
    private var prefReverseScroll = true
    private var prefAlpha = 200
    private var prefHandleSize = 60 
    private var prefVPosLeft = false
    private var prefHPosTop = false
    private var prefLocked = false
    
    private var prefHandleTouchSize = 60
    private var prefScrollTouchSize = 60
    private var prefScrollVisualSize = 4 // 🚨 NEW Visual Thickness

    private var dragDownTime: Long = 0L
    private var isFocusActive = true 
    private var currentBorderColor = 0xFFFFFFFF.toInt()
    
    // Highlight States for Preview
    private var highlightAlpha = false
    private var highlightHandles = false
    private var highlightScrolls = false
    
    // UI Refs
    private val handleContainers = ArrayList<FrameLayout>()
    private val handleVisuals = ArrayList<View>()
    private var vScrollContainer: FrameLayout? = null
    private var hScrollContainer: FrameLayout? = null
    private var vScrollVisual: View? = null // 🚨 NEW Ref
    private var hScrollVisual: View? = null // 🚨 NEW Ref
    
    private val handler = Handler(Looper.getMainLooper())
    private val longPressRunnable = Runnable { startTouchDrag() }
    private var isResizing = false
    private val resizeLongPressRunnable = Runnable { startResize() }
    private var isMoving = false
    private val moveLongPressRunnable = Runnable { startMove() }
    private val rotationRunnable5s = Runnable { resetRotation() }
    private val rotationRunnable3s = Runnable { performRotation() }
    private val voiceRunnable = Runnable { performVoice() }
    
    private val clearHighlightsRunnable = Runnable {
        highlightAlpha = false
        highlightHandles = false
        highlightScrolls = false
        updateBorderColor(currentBorderColor)
        updateLayoutSizes() 
    }

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
        try { createNotification() } catch (e: Exception) { stopSelf(); return START_NOT_STICKY }
        loadPrefs()
        
        if (intent?.action != null) {
            when (intent.action) {
                "RESET_POSITION" -> resetTrackpadPosition()
                "ROTATE" -> performRotation()
                "RELOAD_PREFS" -> {
                    loadPrefs()
                    updateBorderColor(currentBorderColor)
                    updateLayoutSizes()
                    updateScrollPosition()
                }
                "LOCK_TOGGLE" -> {
                    prefLocked = !prefLocked
                    val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("lock_position", prefLocked).apply()
                    vibrate()
                }
                "PREVIEW_UPDATE" -> handlePreview(intent)
            }
        }

        if (trackpadLayout != null) return START_NOT_STICKY

        bindShizuku()
        val displayId = intent?.getIntExtra("DISPLAY_ID", Display.DEFAULT_DISPLAY) ?: Display.DEFAULT_DISPLAY
        setupWindows(displayId)
        return START_NOT_STICKY
    }
    
    private fun loadPrefs() {
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        prefVibrate = prefs.getBoolean("vibrate", true)
        prefReverseScroll = prefs.getBoolean("reverse_scroll", true)
        prefAlpha = prefs.getInt("alpha", 200)
        prefLocked = prefs.getBoolean("lock_position", false)
        prefVPosLeft = prefs.getBoolean("v_pos_left", false)
        prefHPosTop = prefs.getBoolean("h_pos_top", false)
        prefHandleTouchSize = prefs.getInt("handle_touch_size", 60)
        prefScrollTouchSize = prefs.getInt("scroll_touch_size", 60)
        prefHandleSize = prefs.getInt("handle_size", 60)
        prefScrollVisualSize = prefs.getInt("scroll_visual_size", 4)
        
        scrollZoneThickness = prefScrollTouchSize
    }
    
    private fun handlePreview(intent: Intent) {
        val target = intent.getStringExtra("TARGET")
        val value = intent.getIntExtra("VALUE", 0)
        
        handler.removeCallbacks(clearHighlightsRunnable)
        
        when (target) {
            "alpha" -> {
                prefAlpha = value
                highlightAlpha = true
                updateBorderColor(currentBorderColor) 
            }
            "handle_touch" -> {
                prefHandleTouchSize = value
                highlightHandles = true
                updateLayoutSizes()
            }
            "scroll_touch" -> {
                prefScrollTouchSize = value
                scrollZoneThickness = value
                highlightScrolls = true
                updateLayoutSizes()
                updateScrollPosition() 
            }
            "handle_size" -> {
                prefHandleSize = value
                highlightHandles = true 
                updateHandleSize()
            }
            "scroll_visual" -> {
                prefScrollVisualSize = value
                highlightScrolls = true // Highlight container so user can see context
                updateLayoutSizes() // Updates visual thickness
            }
        }
        handler.postDelayed(clearHighlightsRunnable, 1500)
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

        // 1. CURSOR
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
        try { windowManager?.addView(cursorLayout, cursorParams) } catch (e: Exception) {}

        // 2. TRACKPAD
        trackpadLayout = object : FrameLayout(context) {
            override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN) {
                    if (event.repeatCount == 0) {
                        when (event.keyCode) {
                            KeyEvent.KEYCODE_VOLUME_UP -> { isLeftKeyHeld = true; startKeyDrag(MotionEvent.BUTTON_PRIMARY); return true }
                            KeyEvent.KEYCODE_VOLUME_DOWN -> { isRightDragPending = true; handler.postDelayed(voiceRunnable, 1000); return true }
                        }
                    } else { return true }
                }
                if (event?.action == KeyEvent.ACTION_UP) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_VOLUME_UP -> { isLeftKeyHeld = false; stopKeyDrag(MotionEvent.BUTTON_PRIMARY); return true }
                        KeyEvent.KEYCODE_VOLUME_DOWN -> {
                            handler.removeCallbacks(voiceRunnable)
                            if (isRightDragPending) { performClick(true); isRightDragPending = false }
                            else { isRightKeyHeld = false; stopKeyDrag(MotionEvent.BUTTON_SECONDARY) }
                            return true
                        }
                    }
                }
                return super.dispatchKeyEvent(event)
            }
        }
        
        val bg = GradientDrawable()
        bg.cornerRadius = 30f
        trackpadLayout?.background = bg
        updateBorderColor(0x55FFFFFF.toInt())
        trackpadLayout?.isFocusable = true
        trackpadLayout?.isFocusableInTouchMode = true

        handleContainers.clear()
        handleVisuals.clear()
        val handleColor = 0x15FFFFFF.toInt()
        
        addHandle(context, Gravity.TOP or Gravity.RIGHT, handleColor) { v, e -> moveWindow(e) }
        addHandle(context, Gravity.BOTTOM or Gravity.RIGHT, handleColor) { v, e -> resizeWindow(e) }
        addHandle(context, Gravity.BOTTOM or Gravity.LEFT, handleColor) { v, e -> openMenuHandle(e) }
        addHandle(context, Gravity.TOP or Gravity.LEFT, handleColor) { v, e -> voiceWindow(e) }

        addScrollBars(context)

        trackpadParams = WindowManager.LayoutParams(
            400, 300,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        trackpadParams.gravity = Gravity.TOP or Gravity.LEFT
        resetTrackpadPosition()
        trackpadParams.title = "TrackpadInput"
        aspectRatio = 400f / 300f

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (!isTouchDragging && !isLeftKeyHeld && !isRightKeyHeld && !isVScrolling && !isHScrolling) performClick(false)
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
        try { windowManager?.addView(trackpadLayout, trackpadParams); trackpadLayout?.requestFocus() } catch (e: Exception) {}
    }

    private fun addHandle(context: Context, gravity: Int, color: Int, onTouch: (View, MotionEvent) -> Boolean) {
        val container = FrameLayout(context)
        val containerParams = FrameLayout.LayoutParams(prefHandleTouchSize, prefHandleTouchSize)
        containerParams.gravity = gravity
        
        val visual = View(context)
        val bg = GradientDrawable()
        bg.setColor(color)
        bg.cornerRadii = floatArrayOf(15f,15f, 15f,15f, 15f,15f, 15f,15f)
        visual.background = bg
        val visualParams = FrameLayout.LayoutParams(prefHandleSize, prefHandleSize)
        visualParams.gravity = Gravity.CENTER
        
        container.addView(visual, visualParams)
        handleVisuals.add(visual)
        handleContainers.add(container)
        
        trackpadLayout?.addView(container, containerParams)
        container.setOnTouchListener { v, event -> onTouch(v, event) }
    }
    
    private fun updateHandleSize() {
        for (visual in handleVisuals) {
            val params = visual.layoutParams as FrameLayout.LayoutParams
            params.width = prefHandleSize
            params.height = prefHandleSize
            visual.layoutParams = params
        }
    }
    
    private fun updateLayoutSizes() {
        // 1. Update Handles
        for (container in handleContainers) {
            val params = container.layoutParams as FrameLayout.LayoutParams
            params.width = prefHandleTouchSize
            params.height = prefHandleTouchSize
            container.layoutParams = params
            if (highlightHandles) container.setBackgroundColor(0x4000FF00.toInt()) 
            else container.setBackgroundColor(Color.TRANSPARENT)
        }
        
        // 2. Update Scroll Bars Containers AND Visuals
        val margin = prefHandleTouchSize + 10
        
        if (vScrollContainer != null) {
            val params = vScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            params.width = prefScrollTouchSize
            params.setMargins(0, margin, 0, margin)
            vScrollContainer!!.layoutParams = params
            
            if (highlightScrolls) vScrollContainer!!.setBackgroundColor(0x4000FF00.toInt())
            else vScrollContainer!!.setBackgroundColor(Color.TRANSPARENT)
            
            // Update Inner Visual Width
            if (vScrollVisual != null) {
                val visParams = vScrollVisual!!.layoutParams as FrameLayout.LayoutParams
                visParams.width = prefScrollVisualSize
                vScrollVisual!!.layoutParams = visParams
            }
        }
        
        if (hScrollContainer != null) {
            val params = hScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            params.height = prefScrollTouchSize
            params.setMargins(margin, 0, margin, 0)
            hScrollContainer!!.layoutParams = params
            
            if (highlightScrolls) hScrollContainer!!.setBackgroundColor(0x4000FF00.toInt())
            else hScrollContainer!!.setBackgroundColor(Color.TRANSPARENT)
            
            // Update Inner Visual Height
            if (hScrollVisual != null) {
                val visParams = hScrollVisual!!.layoutParams as FrameLayout.LayoutParams
                visParams.height = prefScrollVisualSize
                hScrollVisual!!.layoutParams = visParams
            }
        }
    }

    private fun addScrollBars(context: Context) {
        val margin = prefHandleTouchSize + 10
        
        // Vertical
        vScrollContainer = FrameLayout(context)
        val vParams = FrameLayout.LayoutParams(prefScrollTouchSize, FrameLayout.LayoutParams.MATCH_PARENT)
        vParams.gravity = if (prefVPosLeft) Gravity.LEFT else Gravity.RIGHT
        vParams.setMargins(0, margin, 0, margin)
        trackpadLayout?.addView(vScrollContainer, vParams)
        
        vScrollVisual = View(context)
        vScrollVisual!!.setBackgroundColor(0x30FFFFFF.toInt())
        val vVisParams = FrameLayout.LayoutParams(prefScrollVisualSize, FrameLayout.LayoutParams.MATCH_PARENT)
        vVisParams.gravity = Gravity.CENTER_HORIZONTAL
        vScrollContainer?.addView(vScrollVisual, vVisParams)

        // Horizontal
        hScrollContainer = FrameLayout(context)
        val hParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, prefScrollTouchSize)
        hParams.gravity = if (prefHPosTop) Gravity.TOP else Gravity.BOTTOM
        hParams.setMargins(margin, 0, margin, 0)
        trackpadLayout?.addView(hScrollContainer, hParams)
        
        hScrollVisual = View(context)
        hScrollVisual!!.setBackgroundColor(0x30FFFFFF.toInt())
        val hVisParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, prefScrollVisualSize)
        hVisParams.gravity = Gravity.CENTER_VERTICAL
        hScrollContainer?.addView(hScrollVisual, hVisParams)
    }
    
    private fun updateScrollPosition() {
        val margin = prefHandleTouchSize + 10
        if (vScrollContainer != null) {
            val vParams = vScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            vParams.gravity = if (prefVPosLeft) Gravity.LEFT else Gravity.RIGHT
            vParams.setMargins(0, margin, 0, margin)
            vScrollContainer!!.layoutParams = vParams
        }
        if (hScrollContainer != null) {
            val hParams = hScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            hParams.gravity = if (prefHPosTop) Gravity.TOP else Gravity.BOTTOM
            hParams.setMargins(margin, 0, margin, 0)
            hScrollContainer!!.layoutParams = hParams
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

    private fun resetTrackpadPosition() {
        if (windowManager == null || trackpadLayout == null) return
        trackpadParams.x = (screenWidth / 2) - 200
        trackpadParams.y = (screenHeight / 2) + 200
        trackpadParams.width = 400
        trackpadParams.height = 300
        try { windowManager?.updateViewLayout(trackpadLayout, trackpadParams); vibrate() } catch (e: Exception) {}
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

    private fun voiceWindow(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { handler.postDelayed(voiceRunnable, 1000); return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { handler.removeCallbacks(voiceRunnable); if (isFocusActive) updateBorderColor(0x55FFFFFF.toInt()); return true }
        }
        return false
    }

    private fun performVoice() {
        vibrate(); isRightDragPending = false; dropFocus()
        Thread { try { shellService?.runCommand("am start -a android.intent.action.VOICE_COMMAND") } catch (e: Exception) { } }.start()
    }

    private fun dropFocus() {
        if (!isFocusActive) return
        isFocusActive = false
        trackpadParams.flags = trackpadParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
        updateBorderColor(0xFFFF0000.toInt())
    }

    private fun regainFocus() {
        if (isFocusActive) return
        isFocusActive = true
        trackpadParams.flags = trackpadParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
        updateBorderColor(0x55FFFFFF.toInt())
    }

    private fun moveWindow(event: MotionEvent): Boolean {
        // 🚨 LOCK FIX: Consume event if locked (return true) so it doesn't drag or fall through
        if (prefLocked) return true 
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { handler.postDelayed(moveLongPressRunnable, 1000); initialWindowX = trackpadParams.x; initialWindowY = trackpadParams.y; initialTouchX = event.rawX; initialTouchY = event.rawY; return true }
            MotionEvent.ACTION_MOVE -> {
                if (isMoving) { trackpadParams.x = initialWindowX + (event.rawX - initialTouchX).toInt(); trackpadParams.y = initialWindowY + (event.rawY - initialTouchY).toInt(); windowManager?.updateViewLayout(trackpadLayout, trackpadParams) } 
                else if (abs(event.rawX - initialTouchX) > 20) handler.removeCallbacks(moveLongPressRunnable)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { handler.removeCallbacks(moveLongPressRunnable); if (isMoving) stopMove(); return true }
        }
        return false
    }
    
    private fun startMove() { isMoving = true; vibrate(); updateBorderColor(0xFF0000FF.toInt()) }
    private fun stopMove() { isMoving = false; updateBorderColor(0x55FFFFFF.toInt()) }

    private fun resizeWindow(event: MotionEvent): Boolean {
        // 🚨 LOCK FIX
        if (prefLocked) return true 
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { handler.postDelayed(resizeLongPressRunnable, 1000); initialWindowWidth = trackpadParams.width; initialTouchX = event.rawX; return true }
            MotionEvent.ACTION_MOVE -> {
                if (isResizing) {
                    val deltaX = event.rawX - initialTouchX
                    var newWidth = max(200, (initialWindowWidth + deltaX).toInt())
                    var newHeight = (newWidth / aspectRatio).toInt()
                    trackpadParams.width = newWidth; trackpadParams.height = newHeight
                    windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
                } else if (abs(event.rawX - initialTouchX) > 20) handler.removeCallbacks(resizeLongPressRunnable)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { handler.removeCallbacks(resizeLongPressRunnable); if (isResizing) stopResize(); return true }
        }
        return false
    }
    
    private fun startResize() { isResizing = true; vibrate(); updateBorderColor(0xFF0000FF.toInt()) }
    private fun stopResize() { isResizing = false; updateBorderColor(0x55FFFFFF.toInt()) }
    
    private fun rotateWindow(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { handler.postDelayed(rotationRunnable3s, 3000); return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { handler.removeCallbacks(rotationRunnable3s); handler.removeCallbacks(rotationRunnable5s); updateBorderColor(0x55FFFFFF.toInt()); return true }
        }
        return false
    }

    private fun handleTrackpadTouch(event: MotionEvent) {
        val viewWidth = trackpadLayout?.width ?: 0
        val viewHeight = trackpadLayout?.height ?: 0
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x; lastTouchY = event.y
                val inVZone = if (prefVPosLeft) event.x < scrollZoneThickness else event.x > (viewWidth - scrollZoneThickness)
                val inHZone = if (prefHPosTop) event.y < scrollZoneThickness else event.y > (viewHeight - scrollZoneThickness)
                if (inVZone) { 
                    isVScrolling = true; vibrate(); updateBorderColor(0xFF00FFFF.toInt()); virtualScrollX = cursorX; virtualScrollY = cursorY; dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY)
                }
                else if (inHZone) { 
                    isHScrolling = true; vibrate(); updateBorderColor(0xFF00FFFF.toInt()); virtualScrollX = cursorX; virtualScrollY = cursorY; dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY)
                }
                else handler.postDelayed(longPressRunnable, 400)
            }
            MotionEvent.ACTION_MOVE -> {
                val rawDx = (event.x - lastTouchX) * sensitivity
                val rawDy = (event.y - lastTouchY) * sensitivity
                if (isVScrolling) {
                    val dist = (event.y - lastTouchY) * scrollSensitivity
                    if (abs(dist) > 0) { if (prefReverseScroll) virtualScrollY += dist else virtualScrollY -= dist; injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) }
                } else if (isHScrolling) {
                    val dist = (event.x - lastTouchX) * scrollSensitivity
                    if (abs(dist) > 0) { if (prefReverseScroll) virtualScrollX += dist else virtualScrollX -= dist; injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) }
                } else {
                    var finalDx = rawDx; var finalDy = rawDy
                    when (rotationAngle) { 90 -> { finalDx = -rawDy; finalDy = rawDx }; 180 -> { finalDx = -rawDx; finalDy = -rawDy }; 270 -> { finalDx = rawDy; finalDy = -rawDx } }
                    if (!isTouchDragging && (abs(rawDx) > 5 || abs(rawDy) > 5)) {
                         handler.removeCallbacks(longPressRunnable)
                         if (isRightDragPending) { isRightDragPending = false; handler.removeCallbacks(voiceRunnable); isRightKeyHeld = true; startKeyDrag(MotionEvent.BUTTON_SECONDARY) }
                    }
                    moveCursor(finalDx, finalDy)
                    if (isTouchDragging) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime)
                    else if (isLeftKeyHeld) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_PRIMARY, dragDownTime)
                    else if (isRightKeyHeld) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_SECONDARY, dragDownTime)
                }
                lastTouchX = event.x; lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { 
                handler.removeCallbacks(longPressRunnable)
                if (isTouchDragging) stopTouchDrag()
                if (isVScrolling || isHScrolling) { injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY); isVScrolling = false; isHScrolling = false; updateBorderColor(0x55FFFFFF.toInt()) }
            }
        }
    }

    private fun performRotation() { rotationAngle = (rotationAngle + 90) % 360; cursorView?.rotation = rotationAngle.toFloat(); vibrate(); updateBorderColor(0xFFFFFF00.toInt()); handler.postDelayed(rotationRunnable5s, 2000) }
    private fun resetRotation() { rotationAngle = 0; cursorView?.rotation = 0f; vibrateLong(); updateBorderColor(0xFFFF0000.toInt()); handler.postDelayed({ updateBorderColor(0x55FFFFFF.toInt()) }, 500) }
    private fun moveCursor(dx: Float, dy: Float) { cursorX = (cursorX + dx).coerceIn(0f, screenWidth.toFloat()); cursorY = (cursorY + dy).coerceIn(0f, screenHeight.toFloat()); cursorParams.x = cursorX.toInt(); cursorParams.y = cursorY.toInt(); try { windowManager?.updateViewLayout(cursorLayout, cursorParams) } catch (e: Exception) {} }
    private fun startKeyDrag(b: Int) { vibrate(); updateBorderColor(0xFF00FF00.toInt()); dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_MOUSE, b, dragDownTime) }
    private fun stopKeyDrag(b: Int) { updateBorderColor(0x55FFFFFF.toInt()); injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_MOUSE, b, dragDownTime) }
    private fun startTouchDrag() { isTouchDragging = true; vibrate(); updateBorderColor(0xFF00FF00.toInt()); dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime) }
    private fun stopTouchDrag() { isTouchDragging = false; updateBorderColor(0x55FFFFFF.toInt()); injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime) }
    private fun injectAction(a: Int, s: Int, b: Int, t: Long, x: Float = cursorX, y: Float = cursorY) { if (shellService == null) return; val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY; Thread { try { shellService?.injectMouse(a, x, y, dId, s, b, t) } catch (e: Exception) {} }.start() }
    private fun injectScroll(v: Float, h: Float) { if (shellService == null) return; val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY; Thread { try { shellService?.injectScroll(cursorX, cursorY, v, h, dId) } catch (e: Exception) {} }.start() }
    private fun performClick(r: Boolean) { if (shellService == null) { bindShizuku(); return }; val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY; Thread { try { if (r) shellService?.execRightClick(cursorX, cursorY, dId) else shellService?.execClick(cursorX, cursorY, dId) } catch (e: Exception) {} }.start() }
    private fun vibrate() { if (!prefVibrate) return; val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator; if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) else v.vibrate(50) }
    private fun vibrateLong() { if (!prefVibrate) return; val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator; if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)) else v.vibrate(200) }
    private fun bindShizuku() { try { val c = ComponentName(packageName, ShellUserService::class.java.name); ShizukuBinder.bind(c, userServiceConnection, BuildConfig.DEBUG, BuildConfig.VERSION_CODE) } catch (e: Exception) {} }
    private fun createNotification() { val c = NotificationChannel("overlay_service", "Trackpad Active", NotificationManager.IMPORTANCE_LOW); (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c); val n = Notification.Builder(this, "overlay_service").setContentTitle("Trackpad Active").setSmallIcon(R.mipmap.ic_launcher).build(); if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(1, n) }
    override fun onDestroy() { super.onDestroy(); if (trackpadLayout != null) windowManager?.removeView(trackpadLayout); if (cursorLayout != null) windowManager?.removeView(cursorLayout); if (isBound) ShizukuBinder.unbind(ComponentName(packageName, ShellUserService::class.java.name), userServiceConnection) }
}
