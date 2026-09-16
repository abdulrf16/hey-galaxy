package com.heygalaxy.app.handlers

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.heygalaxy.app.service.VoiceListenerService
import java.util.*
import java.util.regex.Pattern

class AlarmHandler(private val context: Context) {

    fun parseAndSetAlarm(command: String) {
        val time = extractTime(command)
        if (time != null) {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, time.first)
                putExtra(AlarmClock.EXTRA_MINUTES, time.second)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
                val h = if (time.first % 12 == 0) 12 else time.first % 12
                val m = if (time.second < 10) "0${time.second}" else "${time.second}"
                val ampm = if (time.first < 12) "AM" else "PM"
                VoiceListenerService.instance?.speak("Alarm set for $h:$m $ampm")
            } catch (e: Exception) {
                VoiceListenerService.instance?.speak("Couldn't set the alarm")
            }
        } else {
            VoiceListenerService.instance?.speak("I couldn't understand the time. Try saying set alarm for 7 AM")
        }
    }

    fun parseAndSetReminder(command: String) {
        val time = extractTime(command)
        val message = extractReminderMessage(command)
        if (time != null) {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, time.first)
                putExtra(AlarmClock.EXTRA_MINUTES, time.second)
                putExtra(AlarmClock.EXTRA_MESSAGE, message.ifEmpty { "Reminder" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
                VoiceListenerService.instance?.speak("Reminder set: $message")
            } catch (e: Exception) {
                VoiceListenerService.instance?.speak("Couldn't set the reminder")
            }
        } else {
            VoiceListenerService.instance?.speak("I couldn't understand the reminder time")
        }
    }

    private fun extractTime(command: String): Pair<Int, Int>? {
        val pattern12 = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(a\\.?m\\.?|p\\.?m\\.?)", Pattern.CASE_INSENSITIVE)
        val pattern24 = Pattern.compile("(\\d{1,2}):(\\d{2})")

        var matcher = pattern12.matcher(command)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: return null
            val m = matcher.group(2)?.toIntOrNull() ?: 0
            val isPM = matcher.group(3)?.lowercase()?.contains("p") == true
            val hour = when {
                isPM && h < 12 -> h + 12
                !isPM && h == 12 -> 0
                else -> h
            }
            return Pair(hour, m)
        }

        matcher = pattern24.matcher(command)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: return null
            val m = matcher.group(2)?.toIntOrNull() ?: 0
            return Pair(h, m)
        }
        return null
    }

    private fun extractReminderMessage(command: String): String {
        return command
            .replace(Regex("remind me to|reminder to|remind me|set reminder|at .*", RegexOption.IGNORE_CASE), "")
            .trim()
            .replaceFirstChar { it.uppercase() }
            .ifEmpty { "Reminder" }
    }
}