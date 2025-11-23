package com.example.coverscreentester

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

            == USAGE ==
            
            • KEYBOARD & FOCUS: 
            The trackpad blocks the keyboard by default. 
            To type: Hold the Top-Left corner (1s). Border turns Red/Yellow (Focus OFF). 
            Type your text, then tap the trackpad to return to Mouse Mode (Green/White Border).

            • VOLUME BUTTONS:
            - Vol UP + Swipe: Drag / Select text.
            - Vol DOWN: Right Click / Back.

            • CORNER HANDLES:
            - Top-Left: Hold 1s to toggle Focus/Voice.
            - Top-Right: Hold 1s to Move Window.
            - Bottom-Right: Hold 1s to Resize.
            - Bottom-Left: Open this Main Menu.

            • CONFIGURATION:
            Use the Settings menu to Lock Position, change Scroll locations, or adjust touch sensitivity areas.
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
        vibCheck.textSize = 18f
        
        val reverseCheck = CheckBox(this)
        reverseCheck.text = "Natural Scrolling (Reverse)"
        reverseCheck.isChecked = prefs.getBoolean("reverse_scroll", true)
        reverseCheck.textSize = 18f
        
        val lockCheck = CheckBox(this)
        lockCheck.text = "Lock Trackpad Position/Size"
        lockCheck.isChecked = prefs.getBoolean("lock_position", false)
        lockCheck.textSize = 18f
        
        val vPosCheck = CheckBox(this)
        vPosCheck.text = "Vertical Scroll on Left"
        vPosCheck.isChecked = prefs.getBoolean("v_pos_left", false)
        vPosCheck.textSize = 18f
        
        val hPosCheck = CheckBox(this)
        hPosCheck.text = "Horizontal Scroll on Top"
        hPosCheck.isChecked = prefs.getBoolean("h_pos_top", false)
        hPosCheck.textSize = 18f

        // 2. Visual Sliders
        val alphaLabel = TextView(this)
        alphaLabel.text = "Border Visibility (Alpha)"
        alphaLabel.setPadding(0, 40, 0, 20)
        val alphaSeek = SeekBar(this)
        alphaSeek.max = 255
        alphaSeek.progress = prefs.getInt("alpha", 200)

        // 3. Touch Area Sliders
        val handleTouchLabel = TextView(this)
        handleTouchLabel.text = "Corner Handle Touch Area"
        handleTouchLabel.setPadding(0, 40, 0, 20)
        val handleTouchSeek = SeekBar(this)
        handleTouchSeek.max = 150
        handleTouchSeek.progress = prefs.getInt("handle_touch_size", 60) // Default 60px

        val scrollTouchLabel = TextView(this)
        scrollTouchLabel.text = "Scroll Bar Touch Width"
        scrollTouchLabel.setPadding(0, 40, 0, 20)
        val scrollTouchSeek = SeekBar(this)
        scrollTouchSeek.max = 150
        scrollTouchSeek.progress = prefs.getInt("scroll_touch_size", 60) // Default 60px
        
        layout.addView(vibCheck)
        layout.addView(reverseCheck)
        layout.addView(lockCheck)
        layout.addView(vPosCheck)
        layout.addView(hPosCheck)
        layout.addView(alphaLabel)
        layout.addView(alphaSeek)
        layout.addView(handleTouchLabel)
        layout.addView(handleTouchSeek)
        layout.addView(scrollTouchLabel)
        layout.addView(scrollTouchSeek)

        AlertDialog.Builder(this)
            .setTitle("Configuration")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit()
                    .putBoolean("vibrate", vibCheck.isChecked)
                    .putBoolean("reverse_scroll", reverseCheck.isChecked)
                    .putBoolean("lock_position", lockCheck.isChecked)
                    .putBoolean("v_pos_left", vPosCheck.isChecked)
                    .putBoolean("h_pos_top", hPosCheck.isChecked)
                    .putInt("alpha", alphaSeek.progress)
                    .putInt("handle_touch_size", Math.max(40, handleTouchSeek.progress)) // Min 40
                    .putInt("scroll_touch_size", Math.max(30, scrollTouchSeek.progress)) // Min 30
                    .apply()
                
                sendCommandToService("RELOAD_PREFS")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendCommandToService(action: String) {
        val intent = Intent(this, OverlayService::class.java)
        intent.action = action
        if (isOverlayPermissionGranted()) {
             ContextCompat.startForegroundService(this, intent)
        }
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
            toggleButton.setOnClickListener { 
                 if (isOverlayPermissionGranted()) startOverlayService() else requestOverlayPermission()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkShizukuStatus()
    }

    private fun startOverlayService() {
        val displayId = display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("DISPLAY_ID", displayId)
        }
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
