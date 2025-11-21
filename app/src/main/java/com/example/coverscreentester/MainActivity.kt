package com.example.coverscreentester

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button

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

        // Register Listeners
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

        toggleButton.setOnClickListener {
            if (isOverlayPermissionGranted()) {
                startOverlayService()
            } else {
                requestOverlayPermission()
            }
        }

        // Initial Check
        checkShizukuStatus()
    }

    override fun onResume() {
        super.onResume()
        checkShizukuStatus()
    }

    private fun checkShizukuStatus() {
        if (Shizuku.getBinder() == null) {
            statusText.text = "Status: Shizuku Not Running"
            statusText.setTextColor(0xFFFF0000.toInt()) // Red
            toggleButton.isEnabled = false
        } else if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Status: Permission Needed"
            statusText.setTextColor(0xFFFFFF00.toInt()) // Yellow
            toggleButton.text = "Grant Permission"
            toggleButton.isEnabled = true
            toggleButton.setOnClickListener { Shizuku.requestPermission(0) }
        } else {
            statusText.text = "Status: Ready"
            statusText.setTextColor(0xFF00FF00.toInt()) // Green
            toggleButton.text = "Start Trackpad"
            toggleButton.isEnabled = true
            // Reset listener to start service
            toggleButton.setOnClickListener { 
                 if (isOverlayPermissionGranted()) startOverlayService() else requestOverlayPermission()
            }
        }
    }

    private fun startOverlayService() {
        val displayId = display?.displayId ?: android.view.Display.DEFAULT_DISPLAY
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("DISPLAY_ID", displayId)
        }
        ContextCompat.startForegroundService(this, intent)
        // Move app to background so you can use the trackpad immediately
        moveTaskToBack(true)
    }

    private fun isOverlayPermissionGranted(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestOverlayPermission() {
        Toast.makeText(this, "Please grant Overlay permission", Toast.LENGTH_LONG).show()
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }
}
