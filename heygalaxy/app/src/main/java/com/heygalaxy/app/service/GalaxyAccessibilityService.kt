package com.heygalaxy.app.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class GalaxyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: GalaxyAccessibilityService? = null
        private var pendingName: String? = null
        private var pendingMessage: String? = null
        private var step = State.IDLE
    }

    enum class State { IDLE, SEARCHING, TYPING, SENDING }

    override fun onServiceConnected() {
        instance = this
    }

    fun queueMessage(name: String, message: String) {
        pendingName = name
        pendingMessage = message
        step = State.SEARCHING
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != "com.whatsapp") return

        val root = rootInActiveWindow ?: return

        when (step) {
            State.SEARCHING -> {
                val searchBox = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/search_input").firstOrNull()
                if (searchBox != null) {
                    searchBox.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    val args = Bundle()
                    args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingName)
                    searchBox.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    step = State.TYPING
                }
            }
            State.TYPING -> {
                val contact = root.findAccessibilityNodeInfosByText(pendingName ?: "").firstOrNull()
                if (contact != null) {
                    contact.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    step = State.SENDING
                }
            }
            State.SENDING -> {
                val msgBox = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry").firstOrNull()
                if (msgBox != null && pendingMessage != null) {
                    msgBox.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    val args = Bundle()
                    args.putCharSequence(android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pendingMessage)
                    msgBox.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    val sendBtn = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send").firstOrNull()
                    sendBtn?.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    VoiceListenerService.instance?.speak("Message sent")
                    reset()
                }
            }
            State.IDLE -> {}
        }
    }

    private fun reset() {
        step = State.IDLE
        pendingName = null
        pendingMessage = null
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}