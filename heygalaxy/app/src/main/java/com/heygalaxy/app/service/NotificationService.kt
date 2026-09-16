package com.heygalaxy.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationService : NotificationListenerService() {

    companion object {
        var instance: NotificationService? = null
        private val recentMessages = mutableListOf<String>()
    }

    override fun onListenerConnected() {
        instance = this
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkg = sbn?.packageName ?: return
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title") ?: return
        val text = sbn.notification?.extras?.getCharSequence("android.text")?.toString() ?: return

        val entry = "$title: $text"
        synchronized(recentMessages) {
            recentMessages.add(0, entry)
            if (recentMessages.size > 20) recentMessages.removeAt(recentMessages.size - 1)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    fun getRecentMessages(): List<String> {
        return synchronized(recentMessages) { recentMessages.toList() }
    }

    override fun onListenerDisconnected() {
        instance = null
    }
}