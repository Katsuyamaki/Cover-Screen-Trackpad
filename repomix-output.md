This file is a merged representation of a subset of the codebase, containing files not matching ignore patterns, combined into a single document by Repomix.

# File Summary

## Purpose
This file contains a packed representation of a subset of the repository's contents that is considered the most important context.
It is designed to be easily consumable by AI systems for analysis, code review,
or other automated processes.

## File Format
The content is organized as follows:
1. This summary section
2. Repository information
3. Directory structure
4. Repository files (if enabled)
5. Multiple file entries, each consisting of:
  a. A header with the file path (## File: path/to/file)
  b. The full contents of the file in a code block

## Usage Guidelines
- This file should be treated as read-only. Any changes should be made to the
  original repository files, not this packed version.
- When processing this file, use the file path to distinguish
  between different files in the repository.
- Be aware that this file may contain sensitive information. Handle it with
  the same level of security as you would the original repository.

## Notes
- Some files may have been excluded based on .gitignore rules and Repomix's configuration
- Binary files are not included in this packed representation. Please refer to the Repository Structure section for a complete list of file paths, including binary files
- Files matching these patterns are excluded: **/.gradle/**, **/build/**, **/*.png, **/*.webp
- Files matching patterns in .gitignore are excluded
- Files matching default ignore patterns are excluded
- Files are sorted by Git change count (files with more changes are at the bottom)

# Directory Structure
```
app/
  src/
    androidTest/
      java/
        com/
          example/
            coverscreentester/
              ExampleInstrumentedTest.kt
    main/
      aidl/
        com/
          example/
            coverscreentester/
              IShellService.aidl
      java/
        com/
          example/
            coverscreentester/
              MainActivity.kt
              ManualAdjustActivity.kt
              OverlayService.kt
              ProfilesActivity.kt
              SettingsActivity.kt
              ShellUserService.kt
              ShizukuBinder.java
              TrackpadService.kt
      res/
        drawable/
          ic_cursor.xml
          ic_launcher_background.xml
          ic_launcher_foreground.xml
          ic_lock_closed.xml
          ic_lock_open.xml
          red_border.xml
        layout/
          activity_main.xml
          activity_manual_adjust.xml
          activity_profiles.xml
          activity_settings.xml
        mipmap-anydpi-v26/
          ic_launcher_round.xml
          ic_launcher.xml
        values/
          colors.xml
          strings.xml
          themes.xml
        values-night/
          themes.xml
        xml/
          accessibility_service_config.xml
          backup_rules.xml
          data_extraction_rules.xml
      AndroidManifest.xml
    test/
      java/
        com/
          example/
            coverscreentester/
              ExampleUnitTest.kt
  .gitignore
  build.gradle.kts
  proguard-rules.pro
gradle/
  wrapper/
    gradle-wrapper.jar
    gradle-wrapper.properties
  libs.versions.toml
.gitignore
build.gradle.kts
CoverScreenTrackpad.apk
gradle.properties
gradlew
gradlew.bat
README.md
settings.gradle.kts
```

# Files

## File: app/src/androidTest/java/com/example/coverscreentester/ExampleInstrumentedTest.kt
```kotlin
package com.example.coverscreentester

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.coverscreentester", appContext.packageName)
    }
}
```

## File: app/src/main/java/com/example/coverscreentester/ManualAdjustActivity.kt
```kotlin
package com.example.coverscreentester

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ManualAdjustActivity : Activity() {

    private var isMoveMode = true
    private val STEP_SIZE = 10

    private lateinit var textMode: TextView
    private lateinit var btnToggle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_adjust)

        textMode = findViewById(R.id.text_mode)
        btnToggle = findViewById(R.id.btn_toggle_mode)
        
        updateModeUI()

        btnToggle.setOnClickListener {
            isMoveMode = !isMoveMode
            updateModeUI()
        }

        findViewById<Button>(R.id.btn_up).setOnClickListener { sendAdjust(0, -STEP_SIZE) }
        findViewById<Button>(R.id.btn_down).setOnClickListener { sendAdjust(0, STEP_SIZE) }
        findViewById<Button>(R.id.btn_left).setOnClickListener { sendAdjust(-STEP_SIZE, 0) }
        findViewById<Button>(R.id.btn_right).setOnClickListener { sendAdjust(STEP_SIZE, 0) }
        
        // New Center Reset
        findViewById<Button>(R.id.btn_center).setOnClickListener {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = "RESET_POSITION"
            startService(intent)
        }

        // New Rotate
        findViewById<Button>(R.id.btn_rotate).setOnClickListener {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = "ROTATE"
            startService(intent)
        }
        
        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun updateModeUI() {
        if (isMoveMode) {
            textMode.text = "CURRENT MODE: POSITION"
            textMode.setTextColor(Color.GREEN)
            btnToggle.text = "Switch to Resize"
        } else {
            textMode.text = "CURRENT MODE: SIZE"
            textMode.setTextColor(Color.CYAN)
            btnToggle.text = "Switch to Position"
        }
    }

    private fun sendAdjust(xChange: Int, yChange: Int) {
        val intent = Intent(this, OverlayService::class.java)
        intent.action = "MANUAL_ADJUST"
        
        if (isMoveMode) {
            intent.putExtra("DX", xChange)
            intent.putExtra("DY", yChange)
        } else {
            // In resize mode:
            // UP/DOWN affects Height
            // LEFT/RIGHT affects Width
            intent.putExtra("DW", xChange)
            intent.putExtra("DH", yChange)
        }
        
        startService(intent)
    }
}
```

## File: app/src/main/java/com/example/coverscreentester/ProfilesActivity.kt
```kotlin
package com.example.coverscreentester

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class ProfilesActivity : Activity() {

    private lateinit var tvStats: TextView
    private lateinit var tvList: TextView
    private lateinit var btnSave: Button
    private lateinit var btnReset: Button
    private lateinit var btnClose: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)

        // FORCE WINDOW SIZE
        try {
            val dm = resources.displayMetrics
            val targetWidth = (450 * dm.density).toInt().coerceAtMost(dm.widthPixels)
            window.setLayout(targetWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            window.setGravity(Gravity.CENTER)
        } catch (e: Exception) {}

        tvStats = findViewById(R.id.tvCurrentStats)
        tvList = findViewById(R.id.tvProfileList)
        btnSave = findViewById(R.id.btnSaveCurrent)
        btnReset = findViewById(R.id.btnResetCurrent)
        btnClose = findViewById(R.id.btnClose)

        refreshUI()

        btnSave.setOnClickListener {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = "SAVE_LAYOUT"
            startService(intent)
            Toast.makeText(this, "Saved for this Aspect Ratio!", Toast.LENGTH_SHORT).show()
            tvList.postDelayed({ refreshUI() }, 500)
        }

        btnReset.setOnClickListener {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = "DELETE_PROFILE"
            startService(intent)
            Toast.makeText(this, "Profile Deleted. Resetting...", Toast.LENGTH_SHORT).show()
            tvList.postDelayed({ refreshUI() }, 500)
        }

        btnClose.setOnClickListener { finish() }
    }

    private fun refreshUI() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()
        val ratio = width / height
        val ratioKey = String.format("%.1f", ratio)
        
        tvStats.text = "Ratio: $ratioKey\nRes: ${metrics.widthPixels} x ${metrics.heightPixels}\nDensity: ${metrics.density}"

        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        val allEntries = prefs.all
        val sb = StringBuilder()
        
        val foundProfiles = HashSet<String>()
        
        for (key in allEntries.keys) {
            if (key.startsWith("profile_") && key.endsWith("_xp")) {
                val parts = key.split("_")
                if (parts.size >= 2) {
                    foundProfiles.add(parts[1])
                }
            }
        }
        
        if (foundProfiles.isEmpty()) {
            sb.append("No saved profiles.")
        } else {
            for (p in foundProfiles) {
                sb.append("• Ratio $p: Saved\n")
            }
        }
        
        tvList.text = sb.toString()
    }
}
```

## File: app/src/main/java/com/example/coverscreentester/TrackpadService.kt
```kotlin
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
```

## File: app/src/main/res/drawable/ic_cursor.xml
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:strokeColor="#FFFFFF"
        android:strokeWidth="1"
        android:pathData="M7,2L19,13L12,13L11,20L7,2Z" />
</vector>
```

## File: app/src/main/res/drawable/ic_launcher_background.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#3DDC84"
        android:pathData="M0,0h108v108h-108z" />
    <path
        android:fillColor="#00000000"
        android:pathData="M9,0L9,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,0L19,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M29,0L29,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M39,0L39,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M49,0L49,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M59,0L59,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M69,0L69,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M79,0L79,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M89,0L89,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M99,0L99,108"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,9L108,9"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,19L108,19"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,29L108,29"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,39L108,39"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,49L108,49"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,59L108,59"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,69L108,69"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,79L108,79"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,89L108,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M0,99L108,99"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,29L89,29"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,39L89,39"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,49L89,49"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,59L89,59"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,69L89,69"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M19,79L89,79"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M29,19L29,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M39,19L39,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M49,19L49,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M59,19L59,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M69,19L69,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
    <path
        android:fillColor="#00000000"
        android:pathData="M79,19L79,89"
        android:strokeWidth="0.8"
        android:strokeColor="#33FFFFFF" />
</vector>
```

## File: app/src/main/res/drawable/ic_launcher_foreground.xml
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M31,63.928c0,0 6.4,-11 12.1,-13.1c7.2,-2.6 26,-1.4 26,-1.4l38.1,38.1L107,108.928l-32,-1L31,63.928z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:endX="85.84757"
                android:endY="92.4963"
                android:startX="42.9492"
                android:startY="49.59793"
                android:type="linear">
                <item
                    android:color="#44000000"
                    android:offset="0.0" />
                <item
                    android:color="#00000000"
                    android:offset="1.0" />
            </gradient>
        </aapt:attr>
    </path>
    <path
        android:fillColor="#FFFFFF"
        android:fillType="nonZero"
        android:pathData="M65.3,45.828l3.8,-6.6c0.2,-0.4 0.1,-0.9 -0.3,-1.1c-0.4,-0.2 -0.9,-0.1 -1.1,0.3l-3.9,6.7c-6.3,-2.8 -13.4,-2.8 -19.7,0l-3.9,-6.7c-0.2,-0.4 -0.7,-0.5 -1.1,-0.3C38.8,38.328 38.7,38.828 38.9,39.228l3.8,6.6C36.2,49.428 31.7,56.028 31,63.928h46C76.3,56.028 71.8,49.428 65.3,45.828zM43.4,57.328c-0.8,0 -1.5,-0.5 -1.8,-1.2c-0.3,-0.7 -0.1,-1.5 0.4,-2.1c0.5,-0.5 1.4,-0.7 2.1,-0.4c0.7,0.3 1.2,1 1.2,1.8C45.3,56.528 44.5,57.328 43.4,57.328L43.4,57.328zM64.6,57.328c-0.8,0 -1.5,-0.5 -1.8,-1.2s-0.1,-1.5 0.4,-2.1c0.5,-0.5 1.4,-0.7 2.1,-0.4c0.7,0.3 1.2,1 1.2,1.8C66.5,56.528 65.6,57.328 64.6,57.328L64.6,57.328z"
        android:strokeWidth="1"
        android:strokeColor="#00000000" />
</vector>
```

## File: app/src/main/res/drawable/ic_lock_closed.xml
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FFFFFF">
  <path
      android:fillColor="#FFFFFFFF"
      android:pathData="M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10C20,8.9 19.1,8 18,8zM12,17c-1.1,0 -2,-0.9 -2,-2c0,-1.1 0.9,-2 2,-2s2,0.9 2,2C14,16.1 13.1,17 12,17zM9,8V6c0,-1.66 1.34,-3 3,-3s3,1.34 3,3v2H9z"/>
</vector>
```

## File: app/src/main/res/drawable/ic_lock_open.xml
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FFFFFF">
  <path
      android:fillColor="#FFFFFFFF"
      android:pathData="M12,17c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2s-2,0.9 -2,2S10.9,17 12,17zM18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6h1.9c0,-1.71 1.39,-3.1 3.1,-3.1c1.71,0 3.1,1.39 3.1,3.1v2H6c-1.1,0 -2,0.9 -2,2v10c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10C20,8.9 19.1,8 18,8z"/>
</vector>
```

## File: app/src/main/res/drawable/red_border.xml
```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <stroke
        android:width="4dp"
        android:color="#80FF0000" />
    <solid android:color="#00000000" />
</shape>
```

## File: app/src/main/res/layout/activity_manual_adjust.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:background="#121212"
    android:padding="16dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Manual Adjustment"
        android:textColor="#FFFFFF"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>

    <TextView
        android:id="@+id/text_mode"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="CURRENT MODE: MOVE"
        android:textColor="#3DDC84"
        android:textSize="16sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:layout_marginBottom="24dp">

        <Button
            android:id="@+id/btn_toggle_mode"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Switch Mode"
            android:backgroundTint="#444444"
            android:textColor="#FFFFFF"
            android:layout_marginEnd="8dp"/>

        <Button
            android:id="@+id/btn_rotate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Rotate 90°"
            android:backgroundTint="#333333"
            android:textColor="#FFFFFF"
            android:layout_marginStart="8dp"/>
    </LinearLayout>

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center">

        <Button
            android:id="@+id/btn_up"
            android:layout_width="80dp"
            android:layout_height="60dp"
            android:text="▲"
            android:textSize="20sp"
            android:backgroundTint="#333333"
            android:textColor="#FFFFFF"
            android:layout_marginBottom="4dp"/>

        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal">

            <Button
                android:id="@+id/btn_left"
                android:layout_width="80dp"
                android:layout_height="60dp"
                android:text="◄"
                android:textSize="20sp"
                android:backgroundTint="#333333"
                android:textColor="#FFFFFF"
                android:layout_marginEnd="4dp"/>

            <Button
                android:id="@+id/btn_center"
                android:layout_width="80dp"
                android:layout_height="60dp"
                android:text="⟲"
                android:textSize="24sp"
                android:backgroundTint="#555555"
                android:textColor="#FFFFFF"
                android:tooltipText="Reset to Center"/>

            <Button
                android:id="@+id/btn_right"
                android:layout_width="80dp"
                android:layout_height="60dp"
                android:text="►"
                android:textSize="20sp"
                android:backgroundTint="#333333"
                android:textColor="#FFFFFF"
                android:layout_marginStart="4dp"/>
        </LinearLayout>

        <Button
            android:id="@+id/btn_down"
            android:layout_width="80dp"
            android:layout_height="60dp"
            android:text="▼"
            android:textSize="20sp"
            android:backgroundTint="#333333"
            android:textColor="#FFFFFF"
            android:layout_marginTop="4dp"/>

    </LinearLayout>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Center Button resets position/size"
        android:textColor="#666666"
        android:textSize="12sp"
        android:layout_marginTop="16dp"/>

    <View
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

    <Button
        android:id="@+id/btn_back"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Back to Main Menu"
        android:backgroundTint="#3DDC84"
        android:textColor="#000000"
        android:textStyle="bold"/>

</LinearLayout>
```

## File: app/src/main/res/layout/activity_profiles.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#121212"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Layout Profiles"
            android:textColor="#FFFFFF"
            android:textSize="24sp"
            android:textStyle="bold"
            android:layout_marginBottom="16dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="The app automatically detects screen changes (Cover Screen vs AR Glasses) and loads the matching layout."
            android:textColor="#AAAAAA"
            android:layout_marginBottom="24dp"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:background="#222222"
            android:padding="16dp"
            android:layout_marginBottom="24dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="CURRENT SCREEN"
                android:textColor="#FF9800"
                android:textStyle="bold"
                android:layout_marginBottom="8dp"/>

            <TextView
                android:id="@+id/tvCurrentStats"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Ratio: 1.0"
                android:textColor="#FFFFFF"
                android:fontFamily="monospace"
                android:layout_marginBottom="16dp"/>

            <Button
                android:id="@+id/btnSaveCurrent"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Save Current Position"
                android:backgroundTint="#3DDC84"
                android:textColor="#000000"
                android:textStyle="bold"
                android:layout_marginBottom="8dp"/>
                
            <Button
                android:id="@+id/btnResetCurrent"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Delete Profile (Reset)"
                android:backgroundTint="#990000"
                android:textColor="#FFFFFF"/>
        </LinearLayout>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="ALL SAVED PROFILES"
            android:textColor="#AAAAAA"
            android:layout_marginBottom="8dp"/>
            
        <TextView
            android:id="@+id/tvProfileList"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="..."
            android:textColor="#FFFFFF"
            android:fontFamily="monospace"
            android:layout_marginBottom="24dp"/>

        <Button
            android:id="@+id/btnClose"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Back"
            android:backgroundTint="#444444"
            android:layout_marginBottom="40dp"/>

    </LinearLayout>
</ScrollView>
```

## File: app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

## File: app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

## File: app/src/main/res/values/colors.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
</resources>
```

## File: app/src/main/res/values/strings.xml
```xml
<resources>
    <string name="app_name">CoverScreenTrackpad</string>
    <string name="accessibility_service_description">Input Injector Service for Cover Screen Test</string>
</resources>
```

## File: app/src/main/res/values-night/themes.xml
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.CoverScreenTester" parent="Theme.Material3.DayNight.NoActionBar">
        </style>
</resources>
```

## File: app/src/main/res/xml/backup_rules.xml
```xml
<?xml version="1.0" encoding="utf-8"?><full-backup-content>
    </full-backup-content>
```

## File: app/src/main/res/xml/data_extraction_rules.xml
```xml
<?xml version="1.0" encoding="utf-8"?><data-extraction-rules>
    <cloud-backup>
        </cloud-backup>
    </data-extraction-rules>
```

## File: app/src/test/java/com/example/coverscreentester/ExampleUnitTest.kt
```kotlin
package com.example.coverscreentester

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

## File: app/.gitignore
```
/build
```

## File: app/proguard-rules.pro
```
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
```

## File: gradle/wrapper/gradle-wrapper.properties
```
#Fri Nov 21 07:45:00 EST 2025
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

## File: gradle/libs.versions.toml
```toml
[versions]
agp = "8.13.1"
kotlin = "1.9.23"
coreKtx = "1.13.1"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
appcompat = "1.6.1"
material = "1.11.0"
activity = "1.9.0"
constraintlayout = "2.1.4"
shizuku = "13.1.5" # <--- REVERTED TO LATEST VERSION

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
shizuku-api = { group = "dev.rikka.shizuku", name = "api", version.ref = "shizuku" }
shizuku-provider = { group = "dev.rikka.shizuku", name = "provider", version.ref = "shizuku" }
shizuku-aidl = { group = "dev.rikka.shizuku", name = "aidl", version.ref = "shizuku" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

## File: build.gradle.kts
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
```

## File: gradlew
```
#!/bin/sh
APP_HOME=${0%/*}
APP_NAME=$(basename $0)
APP_BASE_NAME=${APP_NAME%.*}
if [ "$APP_HOME" = "$APP_NAME" ]; then APP_HOME=.; fi
if [ -n "$JAVA_HOME" ] ; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE="java"
fi
if [ ! -x "$JAVA_EXE" ] ; then
    echo "Error: JAVA_HOME is not set or $JAVA_EXE does not exist." >&2
    exit 1
fi
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_EXE" -Dorg.gradle.appname="$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
```

## File: gradlew.bat
```
@if "%DEBUG%"=="" @echo off
setlocal
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%
set JAVA_EXE=java.exe
if defined JAVA_HOME set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" -Dorg.gradle.appname=%~n0 -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
```

## File: settings.gradle.kts
```
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "CoverScreenTrackpad"
include(":app")
```

## File: app/src/main/java/com/example/coverscreentester/ShizukuBinder.java
```java
package com.example.coverscreentester;

import android.content.ComponentName;
import android.content.ServiceConnection;
import rikka.shizuku.Shizuku;

public class ShizukuBinder {
    
    public static void bind(ComponentName component, ServiceConnection connection, boolean debug, int version) {
        Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(component)
                .processNameSuffix("shell")
                .daemon(false)
                .debuggable(debug)
                .version(version);
        
        Shizuku.bindUserService(args, connection);
    }

    public static void unbind(ComponentName component, ServiceConnection connection) {
        Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(component)
                .processNameSuffix("shell")
                .daemon(false);
        
        Shizuku.unbindUserService(args, connection, true); 
    }
}
```

## File: app/src/main/res/values/themes.xml
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.CoverScreenTester" parent="Theme.Material3.DayNight.NoActionBar">
    </style>

    <style name="Theme.CoverScreenTester" parent="Base.Theme.CoverScreenTester" />
</resources>
```

## File: .gitignore
```
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
```

## File: README.md
```markdown
# CoverScreen Trackpad 🖱️

**Turn your Samsung Flip cover screen into a fully functional mouse trackpad.**

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Shizuku](https://img.shields.io/badge/Shizuku-Required-blue?style=for-the-badge)

## 📖 About
**CoverScreen Trackpad** is a specialized utility designed for the Samsung Galaxy Z Flip series (and similar foldables). It creates a transparent, always-on-top trackpad overlay on your cover screen, allowing you to control apps that are difficult to use on the small display.

This app solves the "fat finger" problem on tiny screens by giving you a precise cursor, similar to using a laptop touchpad. It uses **Shizuku** to perform clicks and gestures without Accessibility Services, ensuring better battery life and privacy.

## ✨ Key Features
* **Precision Cursor:** Navigate tiny UI elements with a mouse pointer.
* **Smart Input:** Toggle between "Mouse Mode" and "Keyboard Mode" by holding the corner (prevents the trackpad from blocking your typing).
* **Scroll Bars:** Dedicated vertical and horizontal scroll zones on the edges.
* **Customizable:** Adjust transparency, scroll direction, handle sizes, and scrollbar placement (Left/Right, Top/Bottom).
* **No Accessibility Service:** Uses ADB/Shizuku for cleaner input injection.

## 🛠️ Requirements
1.  **Android 11+**
2.  **[Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api)** (Must be installed and running).

## 🚀 Setup Instructions (Critical)
1.  Install **Shizuku** from the Play Store and start it (via Wireless Debugging).
2.  Install the **CoverScreen Trackpad** APK (from Releases).
3.  **OPEN THE APP ON YOUR MAIN SCREEN FIRST!** 🚨
4.  Grant **"Draw Over Other Apps"** and **"Shizuku"** permissions when prompted.
5.  Once the status says **"Ready"**, you can close the phone and launch the app on your cover screen.

## 🎮 Controls
| Action | Gesture / Button |
| :--- | :--- |
| **Left Click** | Tap anywhere on trackpad |
| **Right Click (Back)** | Press **Volume Down** |
| **Drag / Scroll** | Hold **Volume Up** + Swipe |
| **Toggle Keyboard** | Hold **Top-Left Corner** (1s) |
| **Move Window** | Drag **Top-Right Handle** |
| **Resize Window** | Hold **Bottom-Right Handle** (1s) then drag |
| **Open Menu** | Tap **Bottom-Left Handle** |

## ⚙️ Configuration
Open the app menu (Bottom-Left handle) to configure:
* Haptic Feedback
* Scroll Direction (Natural vs Standard)
* Scrollbar Placement
* Visual Transparency
* Handle Size

## ⚠️ Disclaimer
This project is currently in **Alpha**. It is intended for testing and development purposes. Use at your own risk.
```

## File: app/src/main/res/layout/activity_settings.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#121212"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="20dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Configuration"
            android:textColor="#FFFFFF"
            android:textSize="24sp"
            android:textStyle="bold"
            android:layout_marginBottom="20dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--- Input Speed ---"
            android:textColor="#AAAAAA"
            android:layout_gravity="center"
            android:layout_marginBottom="10dp"/>

        <TextView
            android:id="@+id/tvCursorSpeed"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Cursor Speed: 2.5"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarCursorSpeed"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="25"
            android:layout_marginBottom="16dp"/>

        <TextView
            android:id="@+id/tvScrollSpeed"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scroll Speed: 3.0"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarScrollSpeed"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="30"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--- Behavior ---"
            android:textColor="#AAAAAA"
            android:layout_gravity="center"
            android:layout_marginBottom="10dp"/>

        <Switch
            android:id="@+id/switchVibrate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Haptic Feedback"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:minHeight="48dp"/>

        <Switch
            android:id="@+id/switchReverseScroll"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Natural Scrolling (Reverse)"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:minHeight="48dp"/>

        <Switch
            android:id="@+id/switchVPosLeft"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Vertical Scroll on Left"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:minHeight="48dp"/>

        <Switch
            android:id="@+id/switchHPosTop"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Horizontal Scroll on Top"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:minHeight="48dp"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--- Visuals ---"
            android:textColor="#AAAAAA"
            android:layout_gravity="center"
            android:layout_marginBottom="10dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Border Visibility (Alpha)"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarAlpha"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="255"
            android:progress="200"
            android:layout_marginBottom="16dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Handle Icon Size"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarHandleSize"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="60"
            android:layout_marginBottom="16dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scroll Bar Thickness (Visual)"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarScrollVisual"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="20"
            android:progress="4"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="--- Touch Areas ---"
            android:textColor="#AAAAAA"
            android:layout_gravity="center"
            android:layout_marginBottom="10dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Corner Handle Touch Area"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarHandleTouch"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="150"
            android:progress="60"
            android:layout_marginBottom="16dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scroll Bar Touch Width"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarScrollTouch"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="150"
            android:progress="60"
            android:layout_marginBottom="24dp"/>

        <Button
            android:id="@+id/btnSave"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Save &amp; Apply"
            android:backgroundTint="#3DDC84"
            android:textColor="#000000"
            android:textStyle="bold"
            android:layout_marginBottom="12dp"/>

        <Button
            android:id="@+id/btnBack"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Back to Main Menu"
            android:backgroundTint="#444444"
            android:textColor="#FFFFFF"/>

    </LinearLayout>
</ScrollView>
```

## File: app/src/main/res/xml/accessibility_service_config.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_service_description"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="false"
    android:canPerformGestures="false" 
    android:canRequestFilterKeyEvents="true" 
    android:accessibilityFlags="flagRequestTouchExplorationMode|flagRequestFilterKeyEvents" />
```

## File: app/build.gradle.kts
```
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.coverscreentester"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.coverscreentester"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = true // <--- CRITICAL: Must be enabled
    }

    // Fixes "Unresolved Reference" for AIDL classes
    sourceSets {
        getByName("main") {
            aidl.srcDirs(listOf("src/main/aidl"))
            java.srcDirs(layout.buildDirectory.dir("generated/source/aidl/debug"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Shizuku
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("dev.rikka.shizuku:aidl:13.1.5")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

## File: gradle.properties
```
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```

## File: app/src/main/java/com/example/coverscreentester/SettingsActivity.kt
```kotlin
package com.example.coverscreentester

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)

        // Views
        val seekBarCursor = findViewById<SeekBar>(R.id.seekBarCursorSpeed)
        val tvCursor = findViewById<TextView>(R.id.tvCursorSpeed)
        val seekBarScroll = findViewById<SeekBar>(R.id.seekBarScrollSpeed)
        val tvScroll = findViewById<TextView>(R.id.tvScrollSpeed)
        
        val swVibrate = findViewById<Switch>(R.id.switchVibrate)
        val swReverse = findViewById<Switch>(R.id.switchReverseScroll)
        val swVPos = findViewById<Switch>(R.id.switchVPosLeft)
        val swHPos = findViewById<Switch>(R.id.switchHPosTop)
        
        val seekAlpha = findViewById<SeekBar>(R.id.seekBarAlpha)
        val seekHandleSize = findViewById<SeekBar>(R.id.seekBarHandleSize)
        val seekScrollVisual = findViewById<SeekBar>(R.id.seekBarScrollVisual)
        
        val seekHandleTouch = findViewById<SeekBar>(R.id.seekBarHandleTouch)
        val seekScrollTouch = findViewById<SeekBar>(R.id.seekBarScrollTouch)
        
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // Load Values
        val cSpeed = prefs.getFloat("cursor_speed", 2.5f)
        seekBarCursor.progress = (cSpeed * 10).toInt()
        tvCursor.text = "Cursor Speed: "

        val sSpeed = prefs.getFloat("scroll_speed", 3.0f)
        seekBarScroll.progress = (sSpeed * 10).toInt()
        tvScroll.text = "Scroll Speed: "

        swVibrate.isChecked = prefs.getBoolean("vibrate", true)
        swReverse.isChecked = prefs.getBoolean("reverse_scroll", true)
        swVPos.isChecked = prefs.getBoolean("v_pos_left", false)
        swHPos.isChecked = prefs.getBoolean("h_pos_top", false)
        
        seekAlpha.progress = prefs.getInt("alpha", 200)
        seekHandleSize.progress = prefs.getInt("handle_size", 60)
        seekScrollVisual.progress = prefs.getInt("scroll_visual_size", 4)
        seekHandleTouch.progress = prefs.getInt("handle_touch_size", 60)
        seekScrollTouch.progress = prefs.getInt("scroll_touch_size", 60)

        // Listeners
        seekBarCursor.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) {
                tvCursor.text = "Cursor Speed: "
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })

        seekBarScroll.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) {
                tvScroll.text = "Scroll Speed: "
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        
        seekAlpha.setOnSeekBarChangeListener(createPreviewListener("alpha"))
        seekHandleSize.setOnSeekBarChangeListener(createPreviewListener("handle_size"))
        seekScrollVisual.setOnSeekBarChangeListener(createPreviewListener("scroll_visual"))
        seekHandleTouch.setOnSeekBarChangeListener(createPreviewListener("handle_touch"))
        seekScrollTouch.setOnSeekBarChangeListener(createPreviewListener("scroll_touch"))

        btnSave.setOnClickListener {
            val cVal = if (seekBarCursor.progress < 1) 0.1f else seekBarCursor.progress / 10f
            val sVal = if (seekBarScroll.progress < 1) 0.1f else seekBarScroll.progress / 10f
            
            prefs.edit()
                .putFloat("cursor_speed", cVal)
                .putFloat("scroll_speed", sVal)
                .putBoolean("vibrate", swVibrate.isChecked)
                .putBoolean("reverse_scroll", swReverse.isChecked)
                .putBoolean("v_pos_left", swVPos.isChecked)
                .putBoolean("h_pos_top", swHPos.isChecked)
                .putInt("alpha", seekAlpha.progress)
                .putInt("handle_size", seekHandleSize.progress)
                .putInt("scroll_visual_size", seekScrollVisual.progress)
                .putInt("handle_touch_size", seekHandleTouch.progress)
                .putInt("scroll_touch_size", seekScrollTouch.progress)
                .apply()

            val intent = Intent(this, OverlayService::class.java)
            intent.action = "RELOAD_PREFS"
            startService(intent)
            finish()
        }
        
        btnBack.setOnClickListener { finish() }
    }
    
    private fun createPreviewListener(target: String) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) {
            val intent = Intent(this@SettingsActivity, OverlayService::class.java)
            intent.action = "PREVIEW_UPDATE"
            intent.putExtra("TARGET", target)
            intent.putExtra("VALUE", v)
            startService(intent)
        }
        override fun onStartTrackingTouch(s: SeekBar) {}
        override fun onStopTrackingTouch(s: SeekBar) {}
    }
}
```

## File: app/src/main/aidl/com/example/coverscreentester/IShellService.aidl
```
package com.example.coverscreentester;

interface IShellService {
    void injectMouse(int action, float x, float y, int displayId, int source, int buttonState, long downTime);
    void injectScroll(float x, float y, float vDistance, float hDistance, int displayId);
    void execClick(float x, float y, int displayId);
    void execRightClick(float x, float y, int displayId);
    void injectKey(int keyCode, int action);
    
    // New Window Management Methods
    void setWindowingMode(int taskId, int mode);
    void resizeTask(int taskId, int left, int top, int right, int bottom);
    String runCommand(String cmd);
}
```

## File: app/src/main/java/com/example/coverscreentester/ShellUserService.kt
```kotlin
package com.example.coverscreentester

import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.KeyEvent
import android.view.MotionEvent
import com.example.coverscreentester.IShellService
import java.lang.reflect.Method

class ShellUserService : IShellService.Stub() {

    private val TAG = "ShellUserService"
    private lateinit var inputManager: Any
    private lateinit var injectInputEventMethod: Method
    private val INJECT_MODE_ASYNC = 0

    init {
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

    // --- WINDOW MANAGEMENT ---
    
    override fun setWindowingMode(taskId: Int, mode: Int) {
        try {
            // Mode 5 = Freeform, 1 = Fullscreen
            Runtime.getRuntime().exec("am task set-windowing-mode  ").waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set window mode", e)
        }
    }

    override fun resizeTask(taskId: Int, left: Int, top: Int, right: Int, bottom: Int) {
        try {
            Runtime.getRuntime().exec("am task resize     ")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resize task", e)
        }
    }

    override fun runCommand(cmd: String?): String {
        try {
            if (cmd != null) {
                Runtime.getRuntime().exec(cmd)
            }
        } catch (e: Exception) {}
        return "" 
    }

    // --- INPUT INJECTION (Existing) ---

    override fun injectKey(keyCode: Int, action: Int) {
        if (action == KeyEvent.ACTION_DOWN) {
            try {
                Runtime.getRuntime().exec("input keyevent ")
            } catch (e: Exception) {
                Log.e(TAG, "Key injection failed", e)
            }
        }
    }

    override fun execClick(x: Float, y: Float, displayId: Int) {
        val downTime = SystemClock.uptimeMillis()
        injectInternal(MotionEvent.ACTION_DOWN, x, y, displayId, downTime, downTime, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_PRIMARY)
        try { Thread.sleep(50) } catch (e: InterruptedException) {}
        injectInternal(MotionEvent.ACTION_UP, x, y, displayId, downTime, SystemClock.uptimeMillis(), InputDevice.SOURCE_MOUSE, 0)
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

    override fun injectScroll(x: Float, y: Float, vDistance: Float, hDistance: Float, displayId: Int) {
        if (!this::inputManager.isInitialized || !this::injectInputEventMethod.isInitialized) return
        
        val now = SystemClock.uptimeMillis()
        val props = MotionEvent.PointerProperties()
        props.id = 0
        props.toolType = MotionEvent.TOOL_TYPE_MOUSE

        val coords = MotionEvent.PointerCoords()
        coords.x = x
        coords.y = y
        coords.pressure = 1.0f
        coords.size = 1.0f
        
        coords.setAxisValue(MotionEvent.AXIS_VSCROLL, vDistance)
        coords.setAxisValue(MotionEvent.AXIS_HSCROLL, hDistance)

        var event: MotionEvent? = null
        try {
            event = MotionEvent.obtain(
                now, now,
                MotionEvent.ACTION_SCROLL,
                1, arrayOf(props), arrayOf(coords),
                0, 0, 1.0f, 1.0f, 0, 0, 
                InputDevice.SOURCE_MOUSE, 0
            )
            setDisplayId(event, displayId)
            injectInputEventMethod.invoke(inputManager, event, INJECT_MODE_ASYNC)
        } catch (e: Exception) {
            Log.e(TAG, "Scroll Injection failed", e)
        } finally {
            event?.recycle()
        }
    }

    private fun setDisplayId(event: MotionEvent, displayId: Int) {
        try {
            val method = MotionEvent::class.java.getMethod("setDisplayId", Int::class.javaPrimitiveType)
            method.invoke(event, displayId)
        } catch (e: Exception) {}
    }

    private fun injectInternal(action: Int, x: Float, y: Float, displayId: Int, downTime: Long, eventTime: Long, source: Int, buttonState: Int) {
        if (!this::inputManager.isInitialized || !this::injectInputEventMethod.isInitialized) return
        val props = MotionEvent.PointerProperties()
        props.id = 0
        props.toolType = if (source == InputDevice.SOURCE_MOUSE) MotionEvent.TOOL_TYPE_MOUSE else MotionEvent.TOOL_TYPE_FINGER
        val coords = MotionEvent.PointerCoords()
        coords.x = x
        coords.y = y
        coords.pressure = if (buttonState != 0 || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) 1.0f else 0.0f
        coords.size = 1.0f
        var event: MotionEvent? = null
        try {
            event = MotionEvent.obtain(downTime, eventTime, action, 1, arrayOf(props), arrayOf(coords), 0, buttonState, 1.0f, 1.0f, 0, 0, source, 0)
            setDisplayId(event, displayId)
            injectInputEventMethod.invoke(inputManager, event, INJECT_MODE_ASYNC)
        } catch (e: Exception) { Log.e(TAG, "Injection failed", e) } finally { event?.recycle() }
    }
}
```

## File: app/src/main/res/layout/activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#121212"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="24dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="CoverScreen Trackpad"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="#FFFFFF"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:id="@+id/text_status"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Checking Shizuku..."
            android:textColor="#AAAAAA"
            android:layout_marginBottom="16dp"/>

        <Button
            android:id="@+id/btn_toggle"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Start Trackpad"
            android:backgroundTint="#3DDC84"
            android:textColor="#000000"
            android:textStyle="bold"
            android:layout_marginBottom="8dp"/>

        <Button
            android:id="@+id/btn_lock"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Position: Unlocked"
            android:drawableStart="@drawable/ic_lock_open"
            android:drawablePadding="10dp"
            android:paddingStart="20dp"
            android:paddingEnd="20dp"
            android:backgroundTint="#3DDC84"
            android:textColor="#000000"
            android:textStyle="bold"
            android:layout_marginBottom="8dp"/>

        <Button
            android:id="@+id/btn_profiles"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Layout Profiles (Auto-Resize)"
            android:backgroundTint="#FF9800"
            android:textColor="#000000"
            android:textStyle="bold"
            android:layout_marginBottom="8dp"/>

        <Button
            android:id="@+id/btn_manual_adjust"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Manual Adjustment (Fine Tune)"
            android:backgroundTint="#00ACC1"
            android:textColor="#FFFFFF"
            android:textStyle="bold"
            android:layout_marginBottom="8dp"/>

        <Button
            android:id="@+id/btn_settings"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Configuration / Settings"
            android:backgroundTint="#444444"
            android:textColor="#FFFFFF"
            android:layout_marginBottom="8dp"/>

        <Button
            android:id="@+id/btn_help"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Help / Instructions"
            android:backgroundTint="#0066CC"
            android:textColor="#FFFFFF"
            android:layout_marginBottom="24dp"/>

        <Button
            android:id="@+id/btn_close"
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:text="Close App"
            android:backgroundTint="#990000"
            android:textColor="#FFFFFF"/>

    </LinearLayout>
</ScrollView>
```

## File: app/src/main/AndroidManifest.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <queries>
        <package android:name="moe.shizuku.privileged.api" />
        <package android:name="rikka.shizuku.ui" />
    </queries>

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CoverScreenTester"
        android:resizeableActivity="true"> 
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|density|smallestScreenSize"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <meta-data android:name="android.max_aspect" android:value="4.0" />
        </activity>

        <activity 
            android:name=".SettingsActivity"
            android:exported="false" 
            android:theme="@style/Theme.CoverScreenTester" />

        <activity 
            android:name=".ProfilesActivity"
            android:exported="false" 
            android:theme="@style/Theme.CoverScreenTester" />

        <activity 
            android:name=".ManualAdjustActivity"
            android:exported="false" 
            android:theme="@style/Theme.CoverScreenTester" />

        <service
            android:name=".OverlayService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
                <action android:name="PREVIEW_UPDATE" />
                <action android:name="RESET_POSITION" />
                <action android:name="ROTATE" />
                <action android:name="SAVE_LAYOUT" />
                <action android:name="LOAD_LAYOUT" />
                <action android:name="RELOAD_PREFS" />
                <action android:name="DELETE_PROFILE" />
                <action android:name="MANUAL_ADJUST" /> </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

        <provider
            android:name="rikka.shizuku.ShizukuProvider"
            android:authorities="${applicationId}.shizuku"
            android:enabled="true"
            android:exported="true"
            android:multiprocess="false" />

    </application>

</manifest>
```

## File: app/src/main/java/com/example/coverscreentester/MainActivity.kt
```kotlin
package com.example.coverscreentester

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button
    private lateinit var lockButton: Button
    private lateinit var profilesButton: Button
    private lateinit var settingsButton: Button
    private lateinit var helpButton: Button
    private lateinit var closeButton: Button
    private lateinit var btnManualAdjust: Button
    
    private var lastKnownDisplayId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.text_status)
        toggleButton = findViewById(R.id.btn_toggle)
        lockButton = findViewById(R.id.btn_lock)
        profilesButton = findViewById(R.id.btn_profiles)
        settingsButton = findViewById(R.id.btn_settings)
        helpButton = findViewById(R.id.btn_help)
        closeButton = findViewById(R.id.btn_close)
        btnManualAdjust = findViewById(R.id.btn_manual_adjust)

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(0)
        }

        toggleButton.setOnClickListener {
            if (!isAccessibilityEnabled()) {
                Toast.makeText(this, "Please Enable Accessibility Service", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                forceMoveCommand()
                Toast.makeText(this, "Moving Trackpad to this screen...", Toast.LENGTH_SHORT).show()
            }
        }

        settingsButton.setOnClickListener { 
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        profilesButton.setOnClickListener {
            startActivity(Intent(this, ProfilesActivity::class.java))
        }
        
        btnManualAdjust.setOnClickListener {
            startActivity(Intent(this, ManualAdjustActivity::class.java))
        }

        helpButton.setOnClickListener { showHelpDialog() }
        lockButton.setOnClickListener { toggleLock() }

        closeButton.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }

        if (savedInstanceState == null && isAccessibilityEnabled()) {
            checkAndMoveDisplay()
        }

        updateStatusUI()
        updateLockUI()
    }

    override fun onResume() {
        super.onResume()
        updateStatusUI()
        updateLockUI()
        if (isAccessibilityEnabled()) {
            checkAndMoveDisplay()
        }
    }

    private fun checkAndMoveDisplay() {
        val currentDisplayId = display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
        if (currentDisplayId != lastKnownDisplayId) {
            lastKnownDisplayId = currentDisplayId
            forceMoveCommand()
        }
    }

    private fun forceMoveCommand() {
        val currentDisplayId = display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("DISPLAY_ID", currentDisplayId)
        startService(intent)
    }

    private fun updateStatusUI() {
        if (Shizuku.getBinder() == null) {
            statusText.text = "Status: Shizuku Not Running"
            statusText.setTextColor(0xFFFF0000.toInt())
        } else if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Status: Shizuku Permission Needed"
            statusText.setTextColor(0xFFFFFF00.toInt())
        } else if (isAccessibilityEnabled()) {
            statusText.text = "Status: Active"
            statusText.setTextColor(0xFF00FF00.toInt())
            toggleButton.text = "Move Trackpad Here"
        } else {
            statusText.text = "Status: Accessibility Needed"
            statusText.setTextColor(0xFFFFFF00.toInt())
            toggleButton.text = "Enable Trackpad"
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val componentName = ComponentName(this, OverlayService::class.java)
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices?.contains(componentName.flattenToString()) == true
    }

    private fun toggleLock() {
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        val current = prefs.getBoolean("lock_position", false)
        prefs.edit().putBoolean("lock_position", !current).apply()
        updateLockUI()
        val intent = Intent(this, OverlayService::class.java)
        intent.action = "RELOAD_PREFS"
        startService(intent)
    }

    private fun updateLockUI() {
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        val isLocked = prefs.getBoolean("lock_position", false)
        
        if (isLocked) {
            lockButton.text = "Position: Locked"
            lockButton.backgroundTintList = ColorStateList.valueOf(0xFFFF0000.toInt())
            lockButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_closed, 0, 0, 0)
        } else {
            lockButton.text = "Position: Unlocked"
            lockButton.backgroundTintList = ColorStateList.valueOf(0xFF3DDC84.toInt())
            lockButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_open, 0, 0, 0)
        }
    }

    private fun showHelpDialog() {
        val text = TextView(this)
        text.setPadding(50, 40, 50, 40)
        text.textSize = 16f
        text.text = """
            == SETUP ==
            1. Start Shizuku.
            2. Enable Service.
            
            == CONTROLS ==
            • Use Manual Adjust for fine tuning.
            • Click Center Button in Manual Adjust to reset.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Instructions")
            .setView(text)
            .setPositiveButton("Got it", null)
            .show()
    }
}
```

## File: app/src/main/java/com/example/coverscreentester/OverlayService.kt
```kotlin
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
    private var currentAspectRatio = 1.33f
    private var isTouchDragging = false
    private var isLeftKeyHeld = false
    private var isRightKeyHeld = false
    private var isRightDragPending = false 
    private var isVScrolling = false
    private var isHScrolling = false
    private val scrollSensitivity = 3.0f 
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
    
    // KEYBOARD STATE
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

    // --- GLOBAL KEY INTERCEPTION ---
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
                Log.d("OverlayService", "Aspect Ratio Changed: $lastLoadedProfileKey -> $newKey. Reloading.")
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
                    "MANUAL_ADJUST" -> handleManualAdjust(intent) // NEW
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
    
    private fun handleManualAdjust(intent: Intent) {
        if (windowManager == null || trackpadLayout == null) return
        
        // Read deltas
        val dx = intent.getIntExtra("DX", 0)
        val dy = intent.getIntExtra("DY", 0)
        val dw = intent.getIntExtra("DW", 0)
        val dh = intent.getIntExtra("DH", 0)
        
        // Apply
        trackpadParams.x += dx
        trackpadParams.y += dy
        trackpadParams.width = max(200, trackpadParams.width + dw)
        trackpadParams.height = max(200, trackpadParams.height + dh)
        
        try {
            windowManager?.updateViewLayout(trackpadLayout, trackpadParams)
            saveLayout() // Auto-save on manual adjust so it sticks
        } catch (e: Exception) {}
    }
    
    private fun removeOverlays() {
        try {
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
            
            // 1. CURSOR
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

            // 2. TRACKPAD
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
            addHandle(displayContext, Gravity.TOP or Gravity.RIGHT, handleColor) { v, e -> moveWindow(e) }
            addHandle(displayContext, Gravity.BOTTOM or Gravity.RIGHT, handleColor) { v, e -> resizeWindow(e) }
            addHandle(displayContext, Gravity.BOTTOM or Gravity.LEFT, handleColor) { v, e -> openMenuHandle(e) }
            addHandle(displayContext, Gravity.TOP or Gravity.LEFT, handleColor) { v, e -> voiceWindow(e) }
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
            
        } catch (e: Exception) {
            Log.e("OverlayService", "Setup Windows Crash", e)
        }
    }

    // --- PROFILE MANAGEMENT ---
    
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
            // Default Fallback
            trackpadParams.width = 400
            trackpadParams.height = 300
            trackpadParams.x = (screenWidth / 2) - 200
            trackpadParams.y = (screenHeight / 2) - 150
        }
        
        try { windowManager?.updateViewLayout(trackpadLayout, trackpadParams); } catch (e: Exception) {} 
    }
    
    private fun deleteCurrentProfile() {
        val key = getProfileKey()
        getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE).edit()
            .remove("${key}_saved")
            .remove("${key}_xp")
            .remove("${key}_yp")
            .remove("${key}_wp")
            .remove("${key}_hp")
            .apply()
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

    // --- INTERACTION LOGIC ---

    private fun moveWindow(event: MotionEvent): Boolean { if (prefLocked) return true; when (event.action) { MotionEvent.ACTION_DOWN -> { handler.postDelayed(moveLongPressRunnable, 1000); initialWindowX = trackpadParams.x; initialWindowY = trackpadParams.y; initialTouchX = event.rawX; initialTouchY = event.rawY; return true }; MotionEvent.ACTION_MOVE -> { if (isMoving) { trackpadParams.x = initialWindowX + (event.rawX - initialTouchX).toInt(); trackpadParams.y = initialWindowY + (event.rawY - initialTouchY).toInt(); windowManager?.updateViewLayout(trackpadLayout, trackpadParams) } else if (abs(event.rawX - initialTouchX) > 20) handler.removeCallbacks(moveLongPressRunnable); return true }; MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { handler.removeCallbacks(moveLongPressRunnable); if (isMoving) stopMove(); return true } }; return false }
    private fun startMove() { isMoving = true; vibrate(); updateBorderColor(0xFF0000FF.toInt()) }
    private fun stopMove() { isMoving = false; updateBorderColor(0x55FFFFFF.toInt()) }
    
    private fun resizeWindow(event: MotionEvent): Boolean { 
        if (prefLocked) return true
        when (event.action) { 
            MotionEvent.ACTION_DOWN -> { 
                handler.postDelayed(resizeLongPressRunnable, 1000)
                initialWindowWidth = trackpadParams.width
                initialWindowHeight = trackpadParams.height
                initialTouchX = event.rawX; initialTouchY = event.rawY
                return true 
            }
            MotionEvent.ACTION_MOVE -> { 
                if (isResizing) { 
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    val newWidth = (initialWindowWidth + deltaX).toInt()
                    val newHeight = (initialWindowHeight + deltaY).toInt()
                    trackpadParams.width = max(300, newWidth)
                    trackpadParams.height = max(300, newHeight)
                    windowManager?.updateViewLayout(trackpadLayout, trackpadParams) 
                } else if (abs(event.rawX - initialTouchX) > 20) handler.removeCallbacks(resizeLongPressRunnable)
                return true 
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { 
                handler.removeCallbacks(resizeLongPressRunnable); if (isResizing) stopResize(); return true 
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
                lastTouchX = event.x; lastTouchY = event.y
                val inVZone = if (prefVPosLeft) event.x < scrollZoneThickness else event.x > (viewWidth - scrollZoneThickness)
                val inHZone = if (prefHPosTop) event.y < scrollZoneThickness else event.y > (viewHeight - scrollZoneThickness)
                if (inVZone) { isVScrolling = true; vibrate(); updateBorderColor(0xFF00FFFF.toInt()); virtualScrollX = cursorX; virtualScrollY = cursorY; dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) }
                else if (inHZone) { isHScrolling = true; vibrate(); updateBorderColor(0xFF00FFFF.toInt()); virtualScrollX = cursorX; virtualScrollY = cursorY; dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) }
                else handler.postDelayed(longPressRunnable, 400)
            }
            MotionEvent.ACTION_MOVE -> {
                val rawDx = (event.x - lastTouchX) * cursorSpeed
                val rawDy = (event.y - lastTouchY) * cursorSpeed
                if (isVScrolling) {
                    val dist = (event.y - lastTouchY) * scrollSpeed
                    if (abs(dist) > 0) { if (prefReverseScroll) virtualScrollY += dist else virtualScrollY -= dist; injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) }
                } else if (isHScrolling) {
                    val dist = (event.x - lastTouchX) * scrollSpeed
                    if (abs(dist) > 0) { if (prefReverseScroll) virtualScrollX += dist else virtualScrollX -= dist; injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY) }
                } else {
                    var finalDx = rawDx; var finalDy = rawDy
                    when (rotationAngle) { 90 -> { finalDx = -rawDy; finalDy = rawDx }; 180 -> { finalDx = -rawDx; finalDy = -rawDy }; 270 -> { finalDx = rawDy; finalDy = -rawDx } }
                    if (!isTouchDragging && (abs(rawDx) > 5 || abs(rawDy) > 5)) { handler.removeCallbacks(longPressRunnable); if (isRightDragPending) { isRightDragPending = false; handler.removeCallbacks(voiceRunnable); isRightKeyHeld = true; startKeyDrag(MotionEvent.BUTTON_SECONDARY) } }
                    moveCursor(finalDx, finalDy)
                    if (isTouchDragging) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime)
                    else if (isLeftKeyHeld) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_PRIMARY, dragDownTime)
                    else if (isRightKeyHeld) injectAction(MotionEvent.ACTION_MOVE, InputDevice.SOURCE_MOUSE, MotionEvent.BUTTON_SECONDARY, dragDownTime)
                }
                lastTouchX = event.x; lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { handler.removeCallbacks(longPressRunnable); if (isTouchDragging) stopTouchDrag(); if (isVScrolling || isHScrolling) { injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime, virtualScrollX, virtualScrollY); isVScrolling = false; isHScrolling = false; updateBorderColor(0x55FFFFFF.toInt()) } }
        }
    }
    private fun performRotation() { rotationAngle = (rotationAngle + 90) % 360; cursorView?.rotation = rotationAngle.toFloat(); vibrate(); updateBorderColor(0xFFFFFF00.toInt()); }
    private fun moveCursor(dx: Float, dy: Float) { cursorX = (cursorX + dx).coerceIn(0f, screenWidth.toFloat()); cursorY = (cursorY + dy).coerceIn(0f, screenHeight.toFloat()); cursorParams.x = cursorX.toInt(); cursorParams.y = cursorY.toInt(); try { windowManager?.updateViewLayout(cursorLayout, cursorParams) } catch (e: Exception) {} }
    private fun startKeyDrag(b: Int) { vibrate(); updateBorderColor(0xFF00FF00.toInt()); dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_MOUSE, b, dragDownTime) }
    private fun stopKeyDrag(b: Int) { updateBorderColor(0x55FFFFFF.toInt()); injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_MOUSE, b, dragDownTime) }
    private fun startTouchDrag() { isTouchDragging = true; vibrate(); updateBorderColor(0xFF00FF00.toInt()); dragDownTime = SystemClock.uptimeMillis(); injectAction(MotionEvent.ACTION_DOWN, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime) }
    private fun stopTouchDrag() { isTouchDragging = false; updateBorderColor(0x55FFFFFF.toInt()); injectAction(MotionEvent.ACTION_UP, InputDevice.SOURCE_TOUCHSCREEN, 0, dragDownTime) }
    private fun injectAction(a: Int, s: Int, b: Int, t: Long, x: Float = cursorX, y: Float = cursorY) { if (shellService == null) return; val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY; Thread { try { shellService?.injectMouse(a, x, y, dId, s, b, t) } catch (e: Exception) {} }.start() }
    private fun injectScroll(v: Float, h: Float) { if (shellService == null) return; val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY; Thread { try { shellService?.injectScroll(cursorX, cursorY, v, h, dId) } catch (e: Exception) {} }.start() }
    private fun performClick(r: Boolean) { if (shellService == null) { bindShizuku(); return }; val dId = cursorLayout?.display?.displayId ?: Display.DEFAULT_DISPLAY; Thread { try { if (r) shellService?.execRightClick(cursorX, cursorY, dId) else shellService?.execClick(cursorX, cursorY, dId) } catch (e: Exception) {} }.start() }
    private fun vibrate() { if (!prefVibrate) return; val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator; if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)) else v.vibrate(50) }
    private fun bindShizuku() { try { val c = ComponentName(packageName, ShellUserService::class.java.name); ShizukuBinder.bind(c, userServiceConnection, BuildConfig.DEBUG, BuildConfig.VERSION_CODE) } catch (e: Exception) {} }
    private fun createNotification() { val c = NotificationChannel("overlay_service", "Trackpad Active", NotificationManager.IMPORTANCE_LOW); (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c); val n = Notification.Builder(this, "overlay_service").setContentTitle("Trackpad Active").setSmallIcon(R.mipmap.ic_launcher).build(); if (Build.VERSION.SDK_INT >= 34) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(1, n) }
    override fun onDestroy() { super.onDestroy(); displayManager?.unregisterDisplayListener(this); if (trackpadLayout != null) windowManager?.removeView(trackpadLayout); if (cursorLayout != null) windowManager?.removeView(cursorLayout); if (isBound) ShizukuBinder.unbind(ComponentName(packageName, ShellUserService::class.java.name), userServiceConnection) }
    
    // --- Helpers for UI ---
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

    private fun openMenuHandle(event: MotionEvent): Boolean { if (event.action == MotionEvent.ACTION_DOWN) { vibrate(); val intent = Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }; startActivity(intent); return true }; return false }
    private fun voiceWindow(event: MotionEvent): Boolean { if(event.action == MotionEvent.ACTION_DOWN) { handler.postDelayed(voiceRunnable, 1000); return true } else if (event.action == MotionEvent.ACTION_UP) { handler.removeCallbacks(voiceRunnable); if(!isKeyboardMode) updateBorderColor(0x55FFFFFF.toInt()); return true }; return false }
    
    private fun loadPrefs() { val p = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE); cursorSpeed = p.getFloat("cursor_speed", 2.5f); scrollSpeed = p.getFloat("scroll_speed", 3.0f); prefVibrate = p.getBoolean("vibrate", true); prefReverseScroll = p.getBoolean("reverse_scroll", true); prefAlpha = p.getInt("alpha", 200); prefLocked = p.getBoolean("lock_position", false); prefVPosLeft = p.getBoolean("v_pos_left", false); prefHPosTop = p.getBoolean("h_pos_top", false); prefHandleTouchSize = p.getInt("handle_touch_size", 60); prefScrollTouchSize = p.getInt("scroll_touch_size", 60); prefHandleSize = p.getInt("handle_size", 60); prefScrollVisualSize = p.getInt("scroll_visual_size", 4); scrollZoneThickness = prefScrollTouchSize }
    private fun handlePreview(intent: Intent) { val t = intent.getStringExtra("TARGET"); val v = intent.getIntExtra("VALUE", 0); handler.removeCallbacks(clearHighlightsRunnable); when (t) { "alpha" -> { prefAlpha = v; highlightAlpha = true; updateBorderColor(currentBorderColor) }; "handle_touch" -> { prefHandleTouchSize = v; highlightHandles = true; updateLayoutSizes() }; "scroll_touch" -> { prefScrollTouchSize = v; scrollZoneThickness = v; highlightScrolls = true; updateLayoutSizes(); updateScrollPosition() }; "handle_size" -> { prefHandleSize = v; highlightHandles = true; updateHandleSize() }; "scroll_visual" -> { prefScrollVisualSize = v; highlightScrolls = true; updateLayoutSizes() } }; handler.postDelayed(clearHighlightsRunnable, 1500) }
    private fun addHandle(context: Context, gravity: Int, color: Int, onTouch: (View, MotionEvent) -> Boolean) { val c = FrameLayout(context); val cp = FrameLayout.LayoutParams(prefHandleTouchSize, prefHandleTouchSize); cp.gravity = gravity; val v = View(context); val bg = GradientDrawable(); bg.setColor(color); bg.cornerRadii = floatArrayOf(15f,15f, 15f,15f, 15f,15f, 15f,15f); v.background = bg; val vp = FrameLayout.LayoutParams(prefHandleSize, prefHandleSize); vp.gravity = Gravity.CENTER; c.addView(v, vp); handleVisuals.add(v); handleContainers.add(c); trackpadLayout?.addView(c, cp); c.setOnTouchListener { view, e -> onTouch(view, e) } }
    private fun updateHandleSize() { for (v in handleVisuals) { val p = v.layoutParams; p.width = prefHandleSize; p.height = prefHandleSize; v.layoutParams = p } }
    private fun updateLayoutSizes() { for (c in handleContainers) { val p = c.layoutParams; p.width = prefHandleTouchSize; p.height = prefHandleTouchSize; c.layoutParams = p }; updateScrollPosition() } 
    private fun addScrollBars(context: Context) { val m = prefHandleTouchSize + 10; vScrollContainer = FrameLayout(context); val vp = FrameLayout.LayoutParams(prefScrollTouchSize, FrameLayout.LayoutParams.MATCH_PARENT); vp.gravity = if (prefVPosLeft) Gravity.LEFT else Gravity.RIGHT; vp.setMargins(0, m, 0, m); trackpadLayout?.addView(vScrollContainer, vp); vScrollVisual = View(context); vScrollVisual!!.setBackgroundColor(0x30FFFFFF.toInt()); val vvp = FrameLayout.LayoutParams(prefScrollVisualSize, FrameLayout.LayoutParams.MATCH_PARENT); vvp.gravity = Gravity.CENTER_HORIZONTAL; vScrollContainer?.addView(vScrollVisual, vvp); hScrollContainer = FrameLayout(context); val hp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, prefScrollTouchSize); hp.gravity = if (prefHPosTop) Gravity.TOP else Gravity.BOTTOM; hp.setMargins(m, 0, m, 0); trackpadLayout?.addView(hScrollContainer, hp); hScrollVisual = View(context); hScrollVisual!!.setBackgroundColor(0x30FFFFFF.toInt()); val hvp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, prefScrollVisualSize); hvp.gravity = Gravity.CENTER_VERTICAL; hScrollContainer?.addView(hScrollVisual, hvp) }
    private fun updateScrollPosition() { val m = prefHandleTouchSize + 10; if (vScrollContainer != null) { val vp = vScrollContainer!!.layoutParams as FrameLayout.LayoutParams; vp.gravity = if (prefVPosLeft) Gravity.LEFT else Gravity.RIGHT; vp.setMargins(0, m, 0, m); vScrollContainer!!.layoutParams = vp }; if (hScrollContainer != null) { val hp = hScrollContainer!!.layoutParams as FrameLayout.LayoutParams; hp.gravity = if (prefHPosTop) Gravity.TOP else Gravity.BOTTOM; hp.setMargins(m, 0, m, 0); hScrollContainer!!.layoutParams = hp } }
    private fun updateBorderColor(strokeColor: Int) { currentBorderColor = strokeColor; val bg = trackpadLayout?.background as? GradientDrawable ?: return; bg.setColor(Color.TRANSPARENT); val colorWithAlpha = (strokeColor and 0x00FFFFFF) or (prefAlpha shl 24); bg.setStroke(4, if (highlightAlpha) 0xFF00FF00.toInt() else colorWithAlpha); trackpadLayout?.invalidate() }
}
```
