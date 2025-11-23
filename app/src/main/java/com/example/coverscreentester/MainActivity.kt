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
