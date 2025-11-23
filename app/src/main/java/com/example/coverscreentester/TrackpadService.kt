package com.example.coverscreentester

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.FrameLayout
import kotlin.math.abs

class TrackpadService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private lateinit var trackpadView: FrameLayout
    private var isTrackpadVisible = false

    // Settings
    private var cursorSpeed = 1.0f
    private var scrollFactor = 20.0f
    private var placeVerticalLeft = false
    private var placeHorizontalTop = false

    // State
    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false
    private var isScrolling = false
    
    // Scrollbar Drawing
    private val barThickness = 60f // Touch area thickness
    private val barVisualThickness = 15f // Visual line thickness
    
    private val paintTrack = Paint().apply {
        color = Color.parseColor("#33FFFFFF") 
        style = Paint.Style.FILL
    }
    private val paintThumb = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var lastScrollXPercent = 0.5f 
    private var lastScrollYPercent = 0.5f

    override fun onServiceConnected() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        loadSettings()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        cursorSpeed = prefs.getFloat("cursor_speed", 1.0f)
        scrollFactor = prefs.getFloat("scroll_speed", 20.0f)
        placeVerticalLeft = prefs.getBoolean("vertical_left", false)
        placeHorizontalTop = prefs.getBoolean("horizontal_top", false)
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "TOGGLE_TRACKPAD") {
            if (isTrackpadVisible) removeTrackpad() else showTrackpad()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showTrackpad() {
        loadSettings() 
        
        trackpadView = object : FrameLayout(this) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                drawScrollBars(canvas, width, height)
            }
        }
        
        trackpadView.setBackgroundColor(Color.parseColor("#99000000")) 

        trackpadView.setOnTouchListener { v, event ->
            handleTouch(event, v.width, v.height)
            v.invalidate() 
            true
        }

        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.LEFT

        windowManager.addView(trackpadView, params)
        isTrackpadVisible = true
    }

    private fun removeTrackpad() {
        if (isTrackpadVisible) {
            windowManager.removeView(trackpadView)
            isTrackpadVisible = false
        }
    }

    private fun drawScrollBars(canvas: Canvas, width: Int, height: Int) {
        val w = width.toFloat()
        val h = height.toFloat()
        
        // Vertical
        val vLeft = if (placeVerticalLeft) 0f else w - barVisualThickness
        val vRight = if (placeVerticalLeft) barVisualThickness else w

        canvas.drawRect(vLeft, 0f, vRight, h, paintTrack)

        val vThumbHeight = h * 0.2f
        val vThumbTop = (h * lastScrollYPercent) - (vThumbHeight / 2)
        canvas.drawRoundRect(RectF(vLeft, vThumbTop, vRight, vThumbTop + vThumbHeight), 5f, 5f, paintThumb)

        // Horizontal
        val hTop = if (placeHorizontalTop) 0f else h - barVisualThickness
        val hBottom = if (placeHorizontalTop) barVisualThickness else h

        canvas.drawRect(0f, hTop, w, hBottom, paintTrack)
        
        val hThumbWidth = w * 0.2f
        val hThumbLeft = (w * lastScrollXPercent) - (hThumbWidth / 2)
        canvas.drawRoundRect(RectF(hThumbLeft, hTop, hThumbLeft + hThumbWidth, hBottom), 5f, 5f, paintThumb)
    }

    private fun handleTouch(event: MotionEvent, width: Int, height: Int) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                
                val inVerticalZone = if (placeVerticalLeft) (event.x < barThickness) else (event.x > width - barThickness)
                val inHorizontalZone = if (placeHorizontalTop) (event.y < barThickness) else (event.y > height - barThickness)
                
                if (inVerticalZone || inHorizontalZone) {
                    isScrolling = true
                } else {
                    isDragging = true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - lastX
                val deltaY = event.y - lastY

                if (isScrolling) {
                    if (abs(deltaX) > 2 || abs(deltaY) > 2) {
                        // Visual update only for now
                        lastScrollXPercent = (event.x / width).coerceIn(0.1f, 0.9f)
                        lastScrollYPercent = (event.y / height).coerceIn(0.1f, 0.9f)
                    }
                } else if (isDragging) {
                    performMouseClick(deltaX * cursorSpeed, deltaY * cursorSpeed)
                }

                lastX = event.x
                lastY = event.y
            }
            
            MotionEvent.ACTION_UP -> {
                if (!isScrolling && !isDragging) {
                    dispatchGesture(createClick(lastX, lastY), null, null)
                }
                isDragging = false
                isScrolling = false
            }
        }
    }

    private fun performMouseClick(dx: Float, dy: Float) {
        if (abs(dx) > 1 || abs(dy) > 1) {
            val path = Path()
            path.moveTo(lastX, lastY)
            path.lineTo(lastX + dx, lastY + dy)
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 10))
                .build()
            dispatchGesture(gesture, null, null)
        }
    }

    private fun createClick(x: Float, y: Float): GestureDescription {
        val path = Path()
        path.moveTo(x, y)
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        return gesture
    }
}
