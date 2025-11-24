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
- Files matching these patterns are excluded: **/.gradle/**, **/build/**, **/.idea/**, **/*.iml, **/local.properties, **/build_log.txt, **/*.png, **/*.webp, **/*.jar, **/*.aar, **/captures/**
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
              OverlayService.kt
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

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)

        // Views
        val seekBarSpeed = findViewById<SeekBar>(R.id.seekBarSpeed)
        val tvSpeedLabel = findViewById<TextView>(R.id.tvSpeedLabel)
        
        val seekBarScrollSpeed = findViewById<SeekBar>(R.id.seekBarScrollSpeed)
        val tvScrollSpeedLabel = findViewById<TextView>(R.id.tvScrollSpeedLabel)

        val switchVerticalLeft = findViewById<Switch>(R.id.switchVerticalLeft)
        val switchHorizontalTop = findViewById<Switch>(R.id.switchHorizontalTop)

        val btnSave = findViewById<Button>(R.id.btnSave)

        // Load Saved Values
        val currentSpeed = prefs.getFloat("cursor_speed", 1.0f)
        val progressSpeed = (currentSpeed * 10).toInt()
        seekBarSpeed.progress = progressSpeed
        tvSpeedLabel.text = "Cursor Speed: $currentSpeed"

        val currentScrollSpeed = prefs.getFloat("scroll_speed", 20.0f)
        seekBarScrollSpeed.progress = currentScrollSpeed.toInt()
        tvScrollSpeedLabel.text = "Scroll Speed: $currentScrollSpeed"

        val isVerticalLeft = prefs.getBoolean("vertical_left", false) 
        switchVerticalLeft.isChecked = isVerticalLeft

        val isHorizontalTop = prefs.getBoolean("horizontal_top", false)
        switchHorizontalTop.isChecked = isHorizontalTop

        // Listeners
        seekBarSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val speed = if (progress < 1) 0.1f else progress / 10f
                tvSpeedLabel.text = "Cursor Speed: $speed"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarScrollSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val sSpeed = if (progress < 1) 1f else progress.toFloat()
                tvScrollSpeedLabel.text = "Scroll Speed: $sSpeed"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSave.setOnClickListener {
            val speed = if (seekBarSpeed.progress < 1) 0.1f else seekBarSpeed.progress / 10f
            val sSpeed = if (seekBarScrollSpeed.progress < 1) 1f else seekBarScrollSpeed.progress.toFloat()
            
            val vLeft = switchVerticalLeft.isChecked
            val hTop = switchHorizontalTop.isChecked

            prefs.edit().apply {
                putFloat("cursor_speed", speed)
                putFloat("scroll_speed", sSpeed)
                putBoolean("vertical_left", vLeft)
                putBoolean("horizontal_top", hTop)
                apply()
            }

            // Restart Service to apply changes
            stopService(Intent(this, TrackpadService::class.java))
            startService(Intent(this, TrackpadService::class.java))
            finish()
        }
    }
}
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

    // FIXED: unbind now accepts ComponentName to reconstruct the required args
    public static void unbind(ComponentName component, ServiceConnection connection) {
        Shizuku.UserServiceArgs args = new Shizuku.UserServiceArgs(component)
                .processNameSuffix("shell")
                .daemon(false);
        
        Shizuku.unbindUserService(args, connection, true); 
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

## File: app/src/main/res/layout/activity_settings.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Trackpad Settings"
            android:textColor="#FFFFFF"
            android:textSize="24sp"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:id="@+id/tvSpeedLabel"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Cursor Speed: 1.0"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarSpeed"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="50"
            android:progress="10"
            android:layout_marginBottom="16dp"/>

        <TextView
            android:id="@+id/tvScrollSpeedLabel"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scroll Speed: 20.0"
            android:textColor="#FFFFFF"/>
        <SeekBar
            android:id="@+id/seekBarScrollSpeed"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:max="100"
            android:progress="20"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scrollbar Placement"
            android:textColor="#AAAAAA"
            android:textSize="14sp"
            android:layout_marginBottom="8dp"/>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="12dp">
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Vertical Bar on Left"
                android:textColor="#FFFFFF"
                android:textSize="16sp"/>
            <Switch
                android:id="@+id/switchVerticalLeft"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"/>
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="24dp">
            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Horizontal Bar on Top"
                android:textColor="#FFFFFF"
                android:textSize="16sp"/>
            <Switch
                android:id="@+id/switchHorizontalTop"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"/>
        </LinearLayout>

        <Button
            android:id="@+id/btnSave"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Save &amp; Apply"
            android:backgroundTint="#4CAF50"/>

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

## File: app/src/main/res/values/themes.xml
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.CoverScreenTester" parent="Theme.Material3.DayNight.NoActionBar">
        </style>

    <style name="Theme.CoverScreenTester" parent="Base.Theme.CoverScreenTester" />
</resources>
```

## File: app/src/main/res/values-night/themes.xml
```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.CoverScreenTester" parent="Theme.Material3.DayNight.NoActionBar">
        </style>
</resources>
```

## File: app/src/main/res/xml/accessibility_service_config.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_service_description"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="true"
    android:canPerformGestures="true" 
    android:accessibilityFlags="flagDefault|flagRequestTouchExplorationMode" />
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

## File: app/src/main/aidl/com/example/coverscreentester/IShellService.aidl
```
package com.example.coverscreentester;

interface IShellService {
    String runCommand(String cmd);
    void injectTouch(int action, float x, float y, int displayId);
    
    void execClick(float x, float y, int displayId);
    void execRightClick(float x, float y, int displayId);

    void injectMouse(int action, float x, float y, int displayId, int source, int buttonState, long downTime);

    void execKey(int keyCode);

    // 🚨 UPDATED: Supports Vertical AND Horizontal Scroll
    void injectScroll(float x, float y, float vDistance, float hDistance, int displayId);
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

    override fun injectTouch(action: Int, x: Float, y: Float, displayId: Int) { }

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

    override fun execKey(keyCode: Int) {
        val now = SystemClock.uptimeMillis()
        injectInternalKey(KeyEvent.ACTION_DOWN, keyCode, now)
        try { Thread.sleep(50) } catch (e: InterruptedException) {}
        injectInternalKey(KeyEvent.ACTION_UP, keyCode, SystemClock.uptimeMillis())
    }

    override fun injectScroll(x: Float, y: Float, vDistance: Float, hDistance: Float, displayId: Int) {
        if (!this::inputManager.isInitialized || !this::injectInputEventMethod.isInitialized) return
        
        val now = SystemClock.uptimeMillis()
        val props = PointerProperties()
        props.id = 0
        props.toolType = MotionEvent.TOOL_TYPE_MOUSE

        val coords = PointerCoords()
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

    private fun injectInternalKey(action: Int, keyCode: Int, eventTime: Long) {
        if (!this::inputManager.isInitialized || !this::injectInputEventMethod.isInitialized) return
        try {
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
            event = MotionEvent.obtain(downTime, eventTime, action, 1, arrayOf(props), arrayOf(coords), 0, buttonState, 1.0f, 1.0f, 0, 0, source, 0)
            setDisplayId(event, displayId)
            injectInputEventMethod.invoke(inputManager, event, INJECT_MODE_ASYNC)
        } catch (e: Exception) { Log.e(TAG, "Injection failed", e) } finally { event?.recycle() }
    }

    override fun runCommand(cmd: String?): String { return "" }
}
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
        android:theme="@style/Theme.CoverScreenTester">
        
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".OverlayService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" android:value="overlay_input" />
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
        applicationId = "com.example.katsuyamaki.coverscreentrackpad"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = true
    }

    sourceSets {
        getByName("main") {
            aidl.srcDirs(listOf("src/main/aidl"))
            // Use the preferred way to access the build directory
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
    // CRITICAL FIX: Ensure the provider library is included so the class exists at runtime
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider) 
    implementation(libs.shizuku.aidl)

    // Your App's libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Test libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
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

## File: gradle.properties
```
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2
```

## File: app/src/main/res/layout/activity_main.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp"
    android:background="#121212">

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

    <!-- NEW SAVE/LOAD ROW -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:weightSum="2"
        android:layout_marginBottom="8dp">

        <Button
            android:id="@+id/btn_save_pos"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Save Size/Pos"
            android:backgroundTint="#555555"
            android:textSize="12sp"
            android:layout_marginEnd="4dp"/>

        <Button
            android:id="@+id/btn_load_pos"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Load Size/Pos"
            android:backgroundTint="#555555"
            android:textSize="12sp"
            android:layout_marginStart="4dp"/>
    </LinearLayout>

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
        android:layout_marginBottom="8dp"/>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:weightSum="2"
        android:layout_marginBottom="8dp">

        <Button
            android:id="@+id/btn_reset"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Reset Pos"
            android:backgroundTint="#333333"
            android:textSize="12sp"
            android:layout_marginEnd="4dp"/>

        <Button
            android:id="@+id/btn_rotate"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Rotate"
            android:backgroundTint="#333333"
            android:textSize="12sp"
            android:layout_marginStart="4dp"/>
    </LinearLayout>

    <Button
        android:id="@+id/btn_close"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:text="Close App"
        android:backgroundTint="#990000"
        android:textColor="#FFFFFF"/>

</LinearLayout>
```

## File: app/src/main/java/com/example/coverscreentester/MainActivity.kt
```kotlin
package com.example.coverscreentester

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button
    private lateinit var lockButton: Button
    private lateinit var savePosButton: Button
    private lateinit var loadPosButton: Button
    private lateinit var settingsButton: Button
    private lateinit var helpButton: Button
    private lateinit var resetButton: Button
    private lateinit var rotateButton: Button
    private lateinit var closeButton: Button

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        checkShizukuStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.text_status)
        toggleButton = findViewById(R.id.btn_toggle)
        lockButton = findViewById(R.id.btn_lock)
        savePosButton = findViewById(R.id.btn_save_pos)
        loadPosButton = findViewById(R.id.btn_load_pos)
        settingsButton = findViewById(R.id.btn_settings)
        helpButton = findViewById(R.id.btn_help)
        resetButton = findViewById(R.id.btn_reset)
        rotateButton = findViewById(R.id.btn_rotate)
        closeButton = findViewById(R.id.btn_close)

        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

        toggleButton.setOnClickListener {
            if (isOverlayPermissionGranted()) startOverlayService() else requestOverlayPermission()
        }
        
        lockButton.setOnClickListener { toggleLock() }
        savePosButton.setOnClickListener { 
            sendCommandToService("SAVE_LAYOUT")
            Toast.makeText(this, "Position Saved", Toast.LENGTH_SHORT).show()
        }
        loadPosButton.setOnClickListener { 
            sendCommandToService("LOAD_LAYOUT") 
            Toast.makeText(this, "Position Loaded", Toast.LENGTH_SHORT).show()
        }
        settingsButton.setOnClickListener { showSettingsDialog() }
        helpButton.setOnClickListener { showHelpDialog() }
        
        resetButton.setOnClickListener { sendCommandToService("RESET_POSITION") }
        rotateButton.setOnClickListener { sendCommandToService("ROTATE") }
        
        closeButton.setOnClickListener {
            val intent = Intent(this, OverlayService::class.java)
            stopService(intent)
            finishAffinity()
            System.exit(0)
        }

        checkShizukuStatus()
        updateLockUI()
    }
    
    private fun toggleLock() {
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        val current = prefs.getBoolean("lock_position", false)
        val newState = !current
        
        prefs.edit().putBoolean("lock_position", newState).apply()
        updateLockUI()
        sendCommandToService("RELOAD_PREFS")
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
        val scrollView = ScrollView(this)
        val text = TextView(this)
        text.setPadding(50, 40, 50, 40)
        text.textSize = 16f
        text.text = """
            == SETUP (CRITICAL) ==
            1. Install 'Shizuku' from Play Store & Start it.
            2. Open this app on your phone's **MAIN SCREEN**.
            3. Grant 'Overlay' & 'Shizuku' permissions **on the main screen** first. This is required.
            4. Only after permissions are green, open this app on the Cover Screen.

            == KEYBOARD & FOCUS (IMPORTANT) ==
            By default, the trackpad "steals" focus to work. This **BLOCKS** the on-screen keyboard from popping up.
            
            To Type / Use Keyboard:
            • Hold **Volume Down** (1s) 
            • OR Hold **Top-Left Corner** (1s)
            
            The border will turn Red (Focus OFF). You can now type. Tap the trackpad anywhere to return to Mouse Mode.

            == CONTROLS ==
            • **Left Click:** Tap anywhere.
            • **Right Click:** Press Volume Down (Short Press).
            • **Drag/Scroll:** Hold Volume Up + Swipe.
            
            == HANDLES & LAYOUT ==
            • **Top-Right:** Hold 1s to Move Window.
            • **Bottom-Right:** Hold 1s to Resize.
            • **Save/Load:** Use main menu buttons to save your perfect layout.
            • **Lock:** Use the Lock button to prevent accidental moves.
        """.trimIndent()
        scrollView.addView(text)

        AlertDialog.Builder(this)
            .setTitle("Instructions")
            .setView(scrollView)
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showSettingsDialog() {
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 40)

        // 1. Toggles
        val vibCheck = CheckBox(this)
        vibCheck.text = "Haptic Feedback"
        vibCheck.isChecked = prefs.getBoolean("vibrate", true)
        
        val reverseCheck = CheckBox(this)
        reverseCheck.text = "Natural Scrolling (Reverse)"
        reverseCheck.isChecked = prefs.getBoolean("reverse_scroll", true)
        
        val vPosCheck = CheckBox(this)
        vPosCheck.text = "Vertical Scroll on Left"
        vPosCheck.isChecked = prefs.getBoolean("v_pos_left", false)
        
        val hPosCheck = CheckBox(this)
        hPosCheck.text = "Horizontal Scroll on Top"
        hPosCheck.isChecked = prefs.getBoolean("h_pos_top", false)

        // 2. Visual Sliders
        val alphaLabel = TextView(this)
        alphaLabel.text = "Border Visibility (Alpha)"
        val alphaSeek = SeekBar(this)
        alphaSeek.max = 255
        alphaSeek.progress = prefs.getInt("alpha", 200)
        alphaSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) { sendPreview("alpha", v) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })

        // 3. Touch Area Sliders
        val handleTouchLabel = TextView(this)
        handleTouchLabel.text = "Corner Handle Touch Area"
        val handleTouchSeek = SeekBar(this)
        handleTouchSeek.max = 150
        handleTouchSeek.progress = prefs.getInt("handle_touch_size", 60)
        handleTouchSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) { sendPreview("handle_touch", Math.max(40, v)) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })

        val scrollTouchLabel = TextView(this)
        scrollTouchLabel.text = "Scroll Bar Touch Width"
        val scrollTouchSeek = SeekBar(this)
        scrollTouchSeek.max = 150
        scrollTouchSeek.progress = prefs.getInt("scroll_touch_size", 60)
        scrollTouchSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) { sendPreview("scroll_touch", Math.max(30, v)) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        
        // 4. Visual Size Sliders
        val handleSizeLabel = TextView(this)
        handleSizeLabel.text = "Handle Icon Size (Visual)"
        val handleSizeSeek = SeekBar(this)
        handleSizeSeek.max = 60
        handleSizeSeek.progress = prefs.getInt("handle_size", 60)
        handleSizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) { sendPreview("handle_size", v) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })

        val scrollVisualLabel = TextView(this)
        scrollVisualLabel.text = "Scroll Bar Thickness (Visual)"
        val scrollVisualSeek = SeekBar(this)
        scrollVisualSeek.max = 20
        scrollVisualSeek.progress = prefs.getInt("scroll_visual_size", 4)
        scrollVisualSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, v: Int, f: Boolean) { sendPreview("scroll_visual", Math.max(1, v)) }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        
        layout.addView(vibCheck)
        layout.addView(reverseCheck)
        layout.addView(vPosCheck)
        layout.addView(hPosCheck)
        layout.addView(alphaLabel)
        layout.addView(alphaSeek)
        
        val div1 = TextView(this); div1.text = "--- Touch Areas ---"; div1.gravity = Gravity.CENTER; layout.addView(div1)
        layout.addView(handleTouchLabel)
        layout.addView(handleTouchSeek)
        layout.addView(scrollTouchLabel)
        layout.addView(scrollTouchSeek)
        
        val div2 = TextView(this); div2.text = "--- Visual Sizes ---"; div2.gravity = Gravity.CENTER; layout.addView(div2)
        layout.addView(handleSizeLabel)
        layout.addView(handleSizeSeek)
        layout.addView(scrollVisualLabel)
        layout.addView(scrollVisualSeek)

        AlertDialog.Builder(this)
            .setTitle("Configuration")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit()
                    .putBoolean("vibrate", vibCheck.isChecked)
                    .putBoolean("reverse_scroll", reverseCheck.isChecked)
                    .putBoolean("v_pos_left", vPosCheck.isChecked)
                    .putBoolean("h_pos_top", hPosCheck.isChecked)
                    .putInt("alpha", alphaSeek.progress)
                    .putInt("handle_touch_size", Math.max(40, handleTouchSeek.progress))
                    .putInt("scroll_touch_size", Math.max(30, scrollTouchSeek.progress))
                    .putInt("handle_size", handleSizeSeek.progress)
                    .putInt("scroll_visual_size", Math.max(1, scrollVisualSeek.progress))
                    .apply()
                sendCommandToService("RELOAD_PREFS")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun sendPreview(target: String, value: Int) {
        if (isOverlayPermissionGranted()) {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = "PREVIEW_UPDATE"
            intent.putExtra("TARGET", target)
            intent.putExtra("VALUE", value)
            ContextCompat.startForegroundService(this, intent)
        }
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(this, OverlayService::class.java)
        intent.action = action
        if (isOverlayPermissionGranted()) ContextCompat.startForegroundService(this, intent)
    }

    private fun checkShizukuStatus() {
        if (Shizuku.getBinder() == null) {
            statusText.text = "Status: Shizuku Not Running"
            statusText.setTextColor(0xFFFF0000.toInt()) 
            toggleButton.isEnabled = false
        } else if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Status: Permission Needed"
            statusText.setTextColor(0xFFFFFF00.toInt())
            toggleButton.text = "Grant Permission"
            toggleButton.isEnabled = true
            toggleButton.setOnClickListener { Shizuku.requestPermission(0) }
        } else {
            statusText.text = "Status: Ready"
            statusText.setTextColor(0xFF00FF00.toInt())
            toggleButton.text = "Start Trackpad"
            toggleButton.isEnabled = true
            toggleButton.setOnClickListener { if (isOverlayPermissionGranted()) startOverlayService() else requestOverlayPermission() }
        }
    }

    override fun onResume() {
        super.onResume()
        checkShizukuStatus()
        updateLockUI()
    }

    private fun startOverlayService() {
        val displayId = display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
        val intent = Intent(this, OverlayService::class.java).apply { putExtra("DISPLAY_ID", displayId) }
        ContextCompat.startForegroundService(this, intent)
        moveTaskToBack(true)
    }

    private fun isOverlayPermissionGranted(): Boolean = Settings.canDrawOverlays(this)
    private fun requestOverlayPermission() {
        Toast.makeText(this, "Grant Overlay Permission", Toast.LENGTH_LONG).show()
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}
```

## File: app/src/main/java/com/example/coverscreentester/OverlayService.kt
```kotlin
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
    private var prefScrollVisualSize = 4

    private var dragDownTime: Long = 0L
    private var isFocusActive = true 
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
                "SAVE_LAYOUT" -> saveLayout()
                "LOAD_LAYOUT" -> loadLayout()
                "RELOAD_PREFS" -> {
                    loadPrefs()
                    updateBorderColor(currentBorderColor)
                    updateLayoutSizes()
                    updateScrollPosition()
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
    
    private fun saveLayout() {
        if (trackpadLayout == null) return
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("layout_x", trackpadParams.x)
            .putInt("layout_y", trackpadParams.y)
            .putInt("layout_w", trackpadParams.width)
            .putInt("layout_h", trackpadParams.height)
            .putBoolean("layout_saved", true)
            .apply()
        vibrate()
    }
    
    private fun loadLayout() {
        if (trackpadLayout == null || windowManager == null) return
        val prefs = getSharedPreferences("TrackpadPrefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("layout_saved", false)) return
        
        trackpadParams.x = prefs.getInt("layout_x", (screenWidth / 2) - 200)
        trackpadParams.y = prefs.getInt("layout_y", (screenHeight / 2) + 200)
        trackpadParams.width = prefs.getInt("layout_w", 400)
        trackpadParams.height = prefs.getInt("layout_h", 300)
        
        try { windowManager?.updateViewLayout(trackpadLayout, trackpadParams); vibrate() } catch (e: Exception) {}
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
                highlightScrolls = true
                updateLayoutSizes()
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
        try { 
            windowManager?.addView(trackpadLayout, trackpadParams); trackpadLayout?.requestFocus()
            
            // 🚨 AUTO-LOAD: Attempt to restore position if saved
            loadLayout()
            
        } catch (e: Exception) {}
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
        for (container in handleContainers) {
            val params = container.layoutParams as FrameLayout.LayoutParams
            params.width = prefHandleTouchSize
            params.height = prefHandleTouchSize
            container.layoutParams = params
            if (highlightHandles) container.setBackgroundColor(0x4000FF00.toInt()) 
            else container.setBackgroundColor(Color.TRANSPARENT)
        }
        
        val margin = prefHandleTouchSize + 10
        
        if (vScrollContainer != null) {
            val params = vScrollContainer!!.layoutParams as FrameLayout.LayoutParams
            params.width = prefScrollTouchSize
            params.setMargins(0, margin, 0, margin)
            vScrollContainer!!.layoutParams = params
            
            if (highlightScrolls) vScrollContainer!!.setBackgroundColor(0x4000FF00.toInt())
            else vScrollContainer!!.setBackgroundColor(Color.TRANSPARENT)
            
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
            
            if (hScrollVisual != null) {
                val visParams = hScrollVisual!!.layoutParams as FrameLayout.LayoutParams
                visParams.height = prefScrollVisualSize
                hScrollVisual!!.layoutParams = visParams
            }
        }
    }

    private fun addScrollBars(context: Context) {
        val margin = prefHandleTouchSize + 10
        
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
```
