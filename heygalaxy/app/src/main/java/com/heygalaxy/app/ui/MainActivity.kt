package com.heygalaxy.app.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.heygalaxy.app.R
import com.heygalaxy.app.service.VoiceListenerService

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var lastCommandText: TextView
    private lateinit var micView: View
    private lateinit var startStopFab: FloatingActionButton
    private lateinit var permissionsFab: ExtendedFloatingActionButton
    private var serviceRunning = false

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getStringExtra(VoiceListenerService.EXTRA_STATUS_TEXT)
            val command = intent.getStringExtra(VoiceListenerService.EXTRA_LAST_COMMAND)
            runOnUiThread {
                statusText.text = status
                if (!command.isNullOrBlank()) {
                    lastCommandText.text = "Last: \"$command\""
                    lastCommandText.visibility = View.VISIBLE
                } else {
                    lastCommandText.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        lastCommandText = findViewById(R.id.lastCommandText)
        micView = findViewById(R.id.micView)
        startStopFab = findViewById(R.id.startStopFab)
        permissionsFab = findViewById(R.id.permissionsFab)

        startStopFab.setOnClickListener {
            if (serviceRunning) stopService() else startServiceIfPermitted()
        }
        permissionsFab.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }

        startServiceIfPermitted()
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startServiceIfPermitted() {
        if (hasRecordAudioPermission()) {
            startService()
        } else {
            statusText.text = "Microphone permission needed. Tap 'Check Permissions' below."
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
    }

    private fun startService() {
        try {
            val intent = Intent(this, VoiceListenerService::class.java)
            startForegroundService(intent)
            serviceRunning = true
            startStopFab.setImageResource(android.R.drawable.ic_media_pause)
            micView.animate().scaleX(1.3f).scaleY(1.3f).setDuration(500)
                .withEndAction {
                    micView.animate().scaleX(1f).scaleY(1f).setDuration(500)
                        .withEndAction { if (serviceRunning) pulseMic() }.start()
                }.start()
        } catch (e: Exception) {
            serviceRunning = false
            statusText.text = "Couldn't start the assistant. Check permissions and try again."
        }
    }

    private fun stopService() {
        val intent = Intent(this, VoiceListenerService::class.java)
        stopService(intent)
        serviceRunning = false
        startStopFab.setImageResource(android.R.drawable.ic_btn_speak_now)
        micView.clearAnimation()
        micView.scaleX = 1f
        micView.scaleY = 1f
    }

    private fun pulseMic() {
        if (!serviceRunning) return
        micView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(700)
            .withEndAction {
                micView.animate().scaleX(1f).scaleY(1f).setDuration(700)
                    .withEndAction { pulseMic() }.start()
            }.start()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            statusReceiver,
            IntentFilter(VoiceListenerService.ACTION_STATUS),
            RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(statusReceiver) } catch (e: Exception) {}
    }
}
