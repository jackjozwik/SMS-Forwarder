// MainActivity.kt
package com.example.smsforwarder

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.content.Context
import android.os.Build

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("SMSForwarder", Context.MODE_PRIVATE)

        // Initialize UI elements
        val targetEmailInput = findViewById<android.widget.EditText>(R.id.targetEmailInput)
        val senderEmailInput = findViewById<android.widget.EditText>(R.id.senderEmailInput)
        val senderPasswordInput = findViewById<android.widget.EditText>(R.id.senderPasswordInput)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)
        val toggleService = findViewById<android.widget.Switch>(R.id.toggleService)
        val statusText = findViewById<android.widget.TextView>(R.id.statusText)

        // Load saved values
        targetEmailInput.setText(prefs.getString("targetEmail", ""))
        senderEmailInput.setText(prefs.getString("senderEmail", ""))
        senderPasswordInput.setText(prefs.getString("senderPassword", ""))
        toggleService.isChecked = isServiceRunning()

        // Request permissions
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            123
        )

        // Save button
        saveButton.setOnClickListener {
            prefs.edit().apply {
                putString("targetEmail", targetEmailInput.text.toString())
                putString("senderEmail", senderEmailInput.text.toString())
                putString("senderPassword", senderPasswordInput.text.toString())
                apply()
            }
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            statusText.text = "Settings saved!"
        }

        // Toggle service
        toggleService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startSMSService()
                statusText.text = "Service is running"
            } else {
                stopSMSService()
                statusText.text = "Service is stopped"
            }
        }

        // Auto-start service if it was running before
        if (toggleService.isChecked) {
            startSMSService()
        }
    }

    private fun startSMSService() {
        val serviceIntent = Intent(this, SMSForwarderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun stopSMSService() {
        stopService(Intent(this, SMSForwarderService::class.java))
    }

    private fun isServiceRunning(): Boolean {
        // This is a simple check - you might want to implement a more robust solution
        return prefs.getBoolean("serviceRunning", false)
    }
}