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
