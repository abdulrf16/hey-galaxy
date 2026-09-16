package com.heygalaxy.app.handlers

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent
import com.heygalaxy.app.service.VoiceListenerService

class MediaHandler(private val context: Context) {
    private val audioManager = context.getSystemService(AudioManager::class.java)

    fun volumeUp() {
        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        VoiceListenerService.instance?.speak("Volume up. Level $vol")
    }

    fun volumeDown() {
        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
        val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        VoiceListenerService.instance?.speak("Volume down. Level $vol")
    }

    fun mute() {
        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        VoiceListenerService.instance?.speak("Muted")
    }

    fun play() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
        VoiceListenerService.instance?.speak("Playing")
    }

    fun pause() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
        VoiceListenerService.instance?.speak("Paused")
    }

    fun nextTrack() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        VoiceListenerService.instance?.speak("Next track")
    }

    fun previousTrack() {
        sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        VoiceListenerService.instance?.speak("Previous track")
    }

    private fun sendMediaKey(keyCode: Int) {
        try {
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (e: Exception) {
            // Silently fail — hardware key events may not always work
        }
    }
}