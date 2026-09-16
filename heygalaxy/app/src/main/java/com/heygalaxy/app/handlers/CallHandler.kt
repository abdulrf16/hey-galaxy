package com.heygalaxy.app.handlers

import android.content.Context
import android.net.Uri
import android.telecom.TelecomManager
import com.heygalaxy.app.service.VoiceListenerService

class CallHandler(private val context: Context) {
    private val telecomManager = context.getSystemService(TelecomManager::class.java)

    fun answerCall() {
        try {
            telecomManager?.acceptRingingCall()
            VoiceListenerService.instance?.speak("Answering call")
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't answer the call")
        }
    }

    fun declineCall() {
        try {
            telecomManager?.endCall()
            VoiceListenerService.instance?.speak("Call declined")
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't decline the call")
        }
    }

    fun callContact(name: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${Uri.encode(name)}")
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            VoiceListenerService.instance?.speak("Calling $name")
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't find or call $name")
        }
    }
}