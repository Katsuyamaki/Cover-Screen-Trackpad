package com.example.coverscreentester

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
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

        // Check Shizuku Permission
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(0)
        }

        // --- BUTTON LISTENERS ---

        toggleButton.setOnClickListener {
            if (!isAccessibilityEnabled()) {
                Toast.makeText(this, "Please Enable Accessibility Service", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } else {
                // "Move Here" Logic
                sendMoveCommand()
                Toast.makeText(this, "Moving Trackpad to this screen...", Toast.LENGTH_SHORT).show()
            }
        }

        settingsButton.setOnClickListener { 
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        helpButton.setOnClickListener { showHelpDialog() }

        lockButton.setOnClickListener { toggleLock() }

        savePosButton.setOnClickListener { 
            sendCommandToService("SAVE_LAYOUT")
            Toast.makeText(this, "Position Saved", Toast.LENGTH_SHORT).show()
        }

        loadPosButton.setOnClickListener { 
            sendCommandToService("LOAD_LAYOUT")
            Toast.makeText(this, "Position Loaded", Toast.LENGTH_SHORT).show()
        }

        resetButton.setOnClickListener { sendCommandToService("RESET_POSITION") }
        
        rotateButton.setOnClickListener { sendCommandToService("ROTATE") }

        closeButton.setOnClickListener {
            // To stop an Accessibility Service programmatically is hard, 
            // usually we just disable our internal UI logic or kill the app.
            // For now, we just kill the app process which usually clears the overlay.
            finishAffinity()
            System.exit(0)
        }

        // Auto-move if already enabled
        if (isAccessibilityEnabled()) {
            sendMoveCommand()
        }

        updateStatusUI()
        updateLockUI()
    }

    override fun onResume() {
        super.onResume()
        updateStatusUI()
        updateLockUI()
        if (isAccessibilityEnabled()) sendMoveCommand()
    }

    // --- HELPER METHODS ---

    private fun sendMoveCommand() {
        val currentDisplayId = display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
        val intent = Intent(this, OverlayService::class.java)
        intent.putExtra("DISPLAY_ID", currentDisplayId)
        startService(intent)
    }

    private fun sendCommandToService(action: String) {
        if (isAccessibilityEnabled()) {
            val intent = Intent(this, OverlayService::class.java)
            intent.action = action
            startService(intent)
        } else {
            Toast.makeText(this, "Service not running", Toast.LENGTH_SHORT).show()
        }
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
            == SETUP ==
            1. Start Shizuku.
            2. Enable 'CoverScreen Trackpad' in Accessibility Settings.
            3. Open this app on the screen where you want the trackpad.

            == CONTROLS ==
            • **Left Click:** Tap anywhere.
            • **Right Click:** Press Volume Down.
            • **Drag:** Hold Volume Up + Swipe.
            • **Scroll:** Use the edge bars (Right/Bottom).
            
            == WINDOW ==
            • **Move:** Drag Top-Right handle (1s hold).
            • **Resize:** Drag Bottom-Right handle (1s hold).
            • **Menu:** Tap Bottom-Left handle.
        """.trimIndent()
        scrollView.addView(text)

        AlertDialog.Builder(this)
            .setTitle("Instructions")
            .setView(scrollView)
            .setPositiveButton("Got it", null)
            .show()
    }
}
