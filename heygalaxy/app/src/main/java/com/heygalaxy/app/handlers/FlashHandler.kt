package com.heygalaxy.app.handlers

import android.content.Context
import android.hardware.camera2.CameraManager
import com.heygalaxy.app.service.VoiceListenerService

class FlashHandler(private val context: Context) {
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private val cameraId: String? = cameraManager?.cameraIdList?.firstOrNull()

    fun turnOn() {
        try {
            cameraId?.let { cameraManager?.setTorchMode(it, true) }
            VoiceListenerService.instance?.speak("Torch is on")
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't turn on torch")
        }
    }

    fun turnOff() {
        try {
            cameraId?.let { cameraManager?.setTorchMode(it, false) }
            VoiceListenerService.instance?.speak("Torch is off")
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't turn off torch")
        }
    }
}