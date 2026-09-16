package com.heygalaxy.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "Unknown"

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                VoiceListenerService.instance?.speak("Incoming call from $number")
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {}
            TelephonyManager.EXTRA_STATE_IDLE -> {}
        }
    }
}