package com.heygalaxy.app.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.heygalaxy.app.service.GalaxyAccessibilityService
import com.heygalaxy.app.service.NotificationService
import com.heygalaxy.app.service.VoiceListenerService
import java.util.regex.Pattern

class WhatsAppHandler(private val context: Context) {

    fun openWhatsApp() {
        val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent != null) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            VoiceListenerService.instance?.speak("Opening WhatsApp")
        } else {
            VoiceListenerService.instance?.speak("WhatsApp is not installed")
        }
    }

    fun parseSendCommand(command: String) {
        val pattern = Pattern.compile(
            "(?:send\\s+(?:a\\s+)?(?:whatsapp\\s+)?(?:message\\s+)?to\\s+|whatsapp\\s+)(\\w+(?:\\s+\\w+)?)\\s+(.+)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(command)
        if (matcher.find()) {
            val name = matcher.group(1)?.trim() ?: ""
            val message = matcher.group(2)?.trim() ?: ""
            sendWhatsAppMessage(name, message)
        } else {
            VoiceListenerService.instance?.speak("I couldn't understand the message. Try: send WhatsApp to John, hello!")
        }
    }

    fun sendWhatsAppMessage(contactName: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            VoiceListenerService.instance?.speak("Opening WhatsApp to send message to $contactName")
            GalaxyAccessibilityService.instance?.queueMessage(contactName, message)
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't open WhatsApp")
        }
    }

    fun readLatestMessages() {
        val messages = NotificationService.instance?.getRecentMessages()
        if (messages.isNullOrEmpty()) {
            VoiceListenerService.instance?.speak("No recent messages found")
        } else {
            val text = messages.take(3).joinToString(". Next message. ")
            VoiceListenerService.instance?.speak(text)
        }
    }
}