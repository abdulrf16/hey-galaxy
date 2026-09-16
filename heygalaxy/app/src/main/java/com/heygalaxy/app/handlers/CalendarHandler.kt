package com.heygalaxy.app.handlers

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.heygalaxy.app.service.VoiceListenerService
import java.util.*
import java.util.regex.Pattern

class CalendarHandler(private val context: Context) {

    fun parseAndCreateEvent(command: String) {
        val title = extractTitle(command)
        val timeMillis = extractTime(command)

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            if (timeMillis != null) {
                val endMillis = timeMillis + 60 * 60 * 1000
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            VoiceListenerService.instance?.speak("Opening calendar for: $title")
        } catch (e: Exception) {
            VoiceListenerService.instance?.speak("Couldn't open the calendar")
        }
    }

    private fun extractTitle(command: String): String {
        return command
            .replace(Regex("create event|add event|schedule event|schedule|add to calendar", RegexOption.IGNORE_CASE), "")
            .replace(Regex("tomorrow|on \\w+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("at \\d{1,2}(:\\d{2})?\\s*(am|pm)?", RegexOption.IGNORE_CASE), "")
            .trim()
            .replaceFirstChar { it.uppercase() }
            .ifEmpty { "New Event" }
    }

    private fun extractTime(command: String): Long? {
        val cal = Calendar.getInstance()
        if (command.contains("tomorrow", ignoreCase = true)) cal.add(Calendar.DAY_OF_YEAR, 1)

        val pattern = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(a\\.?m\\.?|p\\.?m\\.?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(command)
        if (matcher.find()) {
            val h = matcher.group(1)?.toIntOrNull() ?: return null
            val m = matcher.group(2)?.toIntOrNull() ?: 0
            val isPM = matcher.group(3)?.lowercase()?.contains("p") == true
            val hour = when {
                isPM && h < 12 -> h + 12
                !isPM && h == 12 -> 0
                else -> h
            }
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, m)
            cal.set(Calendar.SECOND, 0)
            return cal.timeInMillis
        }
        return null
    }
}