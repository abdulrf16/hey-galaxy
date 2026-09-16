package com.heygalaxy.app.service

import android.app.*
import android.content.Intent
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.heygalaxy.app.handlers.*
import com.heygalaxy.app.ui.MainActivity
import java.util.*

class VoiceListenerService : LifecycleService(), TextToSpeech.OnInitListener {

    companion object {
        const val TAG = "VoiceListenerService"
        const val NOTIF_CHANNEL_ID = "hey_galaxy_channel"
        const val NOTIF_ID = 1001
        const val WAKE_WORD = "hey galaxy"
        var instance: VoiceListenerService? = null

        const val ACTION_STATUS = "com.heygalaxy.STATUS"
        const val EXTRA_STATUS_TEXT = "status_text"
        const val EXTRA_LAST_COMMAND = "last_command"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isListeningForCommand = false
    private var isRunning = true
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var callHandler: CallHandler
    private lateinit var mediaHandler: MediaHandler
    private lateinit var flashHandler: FlashHandler
    private lateinit var alarmHandler: AlarmHandler
    private lateinit var calendarHandler: CalendarHandler
    private lateinit var whatsAppHandler: WhatsAppHandler
    private lateinit var commandRouter: CommandRouter

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Listening for 'Hey Galaxy'..."))

        tts = TextToSpeech(this, this)

        callHandler = CallHandler(this)
        mediaHandler = MediaHandler(this)
        flashHandler = FlashHandler(this)
        alarmHandler = AlarmHandler(this)
        calendarHandler = CalendarHandler(this)
        whatsAppHandler = WhatsAppHandler(this)

        commandRouter = CommandRouter(
            this, callHandler, mediaHandler, flashHandler,
            alarmHandler, calendarHandler, whatsAppHandler
        )

        startWakeWordListening()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "galaxy_response")
    }

    private fun startWakeWordListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognizer not available on this device")
            updateNotification("Speech not available")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""

                if (text.contains(WAKE_WORD)) {
                    onWakeWordDetected(text)
                } else {
                    restartListeningDelayed(400)
                }
            }

            override fun onError(error: Int) {
                val delay = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> 300L
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 1500L
                    else -> 600L
                }
                restartListeningDelayed(delay)
            }

            override fun onReadyForSpeech(params: Bundle?) {
                broadcastUpdate("Listening for 'Hey Galaxy'...", "")
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun onWakeWordDetected(fullText: String) {
        updateNotification("Heard: $fullText")

        val afterWakeWord = fullText.substringAfter(WAKE_WORD).trim()

        if (afterWakeWord.length > 2) {
            broadcastUpdate("Command: $afterWakeWord", afterWakeWord)
            processCommand(afterWakeWord)
            restartListeningDelayed(1500)
        } else {
            broadcastUpdate("Listening for command...", "")
            speak("Yes?")
            listenForCommand()
        }
    }

    private fun listenForCommand() {
        isListeningForCommand = true
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                isListeningForCommand = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val command = matches?.firstOrNull()?.lowercase(Locale.getDefault()) ?: ""
                if (command.isNotBlank()) {
                    broadcastUpdate("Command: $command", command)
                    processCommand(command)
                }
                restartListeningDelayed(800)
            }

            override fun onError(error: Int) {
                isListeningForCommand = false
                speak("Sorry, I didn't catch that")
                restartListeningDelayed(600)
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun processCommand(command: String) {
        commandRouter.route(command)
    }

    private fun restartListeningDelayed(delayMs: Long) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isRunning) startWakeWordListening()
        }, delayMs)
    }

    private fun broadcastUpdate(status: String, command: String) {
        updateNotification(status)
        val intent = Intent(ACTION_STATUS).apply {
            putExtra(EXTRA_STATUS_TEXT, status)
            putExtra(EXTRA_LAST_COMMAND, command)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIF_CHANNEL_ID,
            "Hey Galaxy",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Always-on voice assistant"
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return Notification.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Hey Galaxy")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        try {
            manager.notify(NOTIF_ID, buildNotification(text))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update notification", e)
        }
    }

    override fun onDestroy() {
        isRunning = false
        instance = null
        handler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}