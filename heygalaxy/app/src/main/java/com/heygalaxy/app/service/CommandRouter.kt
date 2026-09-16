package com.heygalaxy.app.service

import android.content.Context
import com.heygalaxy.app.handlers.*
import java.util.Calendar

class CommandRouter(
    private val context: Context,
    private val callHandler: CallHandler,
    private val mediaHandler: MediaHandler,
    private val flashHandler: FlashHandler,
    private val alarmHandler: AlarmHandler,
    private val calendarHandler: CalendarHandler,
    private val whatsAppHandler: WhatsAppHandler
) {
    private val service get() = VoiceListenerService.instance

    fun route(command: String) {
        val cmd = command.lowercase().trim()

        when {
            // ── CALLS ─────────────────────────────────────────────
            cmd.contains("answer") && (cmd.contains("call") || cmd.contains("phone")) ->
                callHandler.answerCall()

            cmd.contains("decline") || cmd.contains("reject") || cmd.contains("ignore") ->
                callHandler.declineCall()

            cmd.matches(Regex(".*call (.+)")) -> {
                val name = cmd.replace(Regex(".*call "), "").trim()
                callHandler.callContact(name)
            }

            // ── MEDIA ─────────────────────────────────────────────
            cmd.contains("volume up") || cmd.contains("louder") || cmd.contains("increase volume") ->
                mediaHandler.volumeUp()

            cmd.contains("volume down") || cmd.contains("quieter") || cmd.contains("lower volume") ->
                mediaHandler.volumeDown()

            cmd.contains("mute") || cmd.contains("silence") ->
                mediaHandler.mute()

            cmd.contains("pause") || cmd.contains("stop music") || cmd.contains("stop song") ->
                mediaHandler.pause()

            cmd.contains("play") || cmd.contains("resume music") || cmd.contains("resume") ->
                mediaHandler.play()

            cmd.contains("next") && (cmd.contains("song") || cmd.contains("track") || cmd.contains("music")) ->
                mediaHandler.nextTrack()

            cmd.contains("previous") || cmd.contains("prev song") || cmd.contains("back song") ->
                mediaHandler.previousTrack()

            // ── FLASHLIGHT ────────────────────────────────────────
            (cmd.contains("turn on") || cmd.contains("switch on") || cmd.contains("enable")) &&
                    (cmd.contains("flash") || cmd.contains("torch") || cmd.contains("flashlight")) ->
                flashHandler.turnOn()

            (cmd.contains("turn off") || cmd.contains("switch off") || cmd.contains("disable")) &&
                    (cmd.contains("flash") || cmd.contains("torch") || cmd.contains("flashlight")) ->
                flashHandler.turnOff()

            // ── WHATSAPP ──────────────────────────────────────────
            cmd == "open whatsapp" || cmd == "open whats app" ->
                whatsAppHandler.openWhatsApp()

            cmd.contains("send") && cmd.contains("whatsapp") ->
                whatsAppHandler.parseSendCommand(cmd)

            cmd.contains("read") && (cmd.contains("message") || cmd.contains("messages")) ->
                whatsAppHandler.readLatestMessages()

            // ── ALARMS ────────────────────────────────────────────
            cmd.contains("set alarm") || cmd.contains("wake me") ->
                alarmHandler.parseAndSetAlarm(cmd)

            // ── REMINDERS ─────────────────────────────────────────
            cmd.contains("remind me") || cmd == "reminder" ->
                alarmHandler.parseAndSetReminder(cmd)

            // ── CALENDAR ──────────────────────────────────────────
            cmd.contains("create event") || cmd.contains("add event") ||
                    cmd.contains("schedule") || cmd.contains("add to calendar") ->
                calendarHandler.parseAndCreateEvent(cmd)

            // ── SYSTEM INFO ───────────────────────────────────────
            cmd.contains("what time") || cmd.contains("current time") || cmd == "time" ->
                speakTime()

            cmd.contains("battery") ->
                speakBattery()

            cmd.contains("hello") || cmd.contains("hi") ->
                service?.speak("Hello! How can I help you?")

            cmd.contains("stop") || cmd.contains("goodbye") || cmd.contains("bye") ->
                service?.speak("Goodbye!")

            else ->
                service?.speak("Sorry, I didn't understand that. Try saying 'Hey Galaxy' first.")
        }
    }

    private fun speakTime() {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val min = cal.get(Calendar.MINUTE)
        val ampm = if (hour < 12) "AM" else "PM"
        val h = if (hour % 12 == 0) 12 else hour % 12
        val m = if (min < 10) "0$min" else "$min"
        service?.speak("It's $h:$m $ampm")
    }

    private fun speakBattery() {
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, filter)
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        if (pct >= 0) service?.speak("Battery is at $pct percent")
        else service?.speak("Couldn't read battery level")
    }
}
