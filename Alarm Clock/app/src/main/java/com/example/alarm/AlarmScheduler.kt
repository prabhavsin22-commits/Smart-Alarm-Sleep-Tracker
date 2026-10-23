package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AlarmEntity
import com.example.widget.AuraRiseWidgetProvider
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelAlarm(alarm.id)
            return
        }

        val triggerTimeMillis = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER
            putExtra(AlarmAudioService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmAudioService.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmAudioService.EXTRA_SOUND_NAME, alarm.soundName)
            putExtra(AlarmAudioService.EXTRA_SUNRISE_DURATION, alarm.sunriseDurationMinutes)
            putExtra(AlarmAudioService.EXTRA_VIBRATE, alarm.vibrate)
            putExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE, alarm.challengeType.name)
            putExtra(AlarmAudioService.EXTRA_AMBIENT_THEME, alarm.ambientThemeName)
            putExtra(AlarmAudioService.EXTRA_CUSTOM_SOUND_URI, alarm.customSoundUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val showIntent = Intent(context, AlarmRingingActivity::class.java)
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    alarm.id.toInt(),
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, showPendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
            Log.d("AlarmScheduler", "Alarm scheduled for ${alarm.formattedTime()} at $triggerTimeMillis")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Exact alarm permission missing", e)
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }

        // Notify widget of updated alarm status
        AuraRiseWidgetProvider.updateWidgets(context)
    }

    fun scheduleNap(durationMinutes: Int, label: String = "Power Nap") {
        val triggerTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val napAlarmId = 999999L

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER
            putExtra(AlarmAudioService.EXTRA_ALARM_ID, napAlarmId)
            putExtra(AlarmAudioService.EXTRA_ALARM_LABEL, "⚡ $label ($durationMinutes min)")
            putExtra(AlarmAudioService.EXTRA_SOUND_NAME, "Golden Sunrise Synth")
            putExtra(AlarmAudioService.EXTRA_SUNRISE_DURATION, 5)
            putExtra(AlarmAudioService.EXTRA_VIBRATE, true)
            putExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE, "MATH_PUZZLE")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            napAlarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }

        AuraRiseWidgetProvider.updateWidgets(context)
    }

    fun scheduleSnooze(alarmId: Long, snoozeMinutes: Int = 9) {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER
            putExtra(AlarmAudioService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmAudioService.EXTRA_ALARM_LABEL, "Snooze Alarm")
            putExtra(AlarmAudioService.EXTRA_SOUND_NAME, "Forest Dawn Birds")
            putExtra(AlarmAudioService.EXTRA_SUNRISE_DURATION, 5)
            putExtra(AlarmAudioService.EXTRA_VIBRATE, true)
            putExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE, "MATH_PUZZLE")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarmId + 500).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        AuraRiseWidgetProvider.updateWidgets(context)
    }

    private fun calculateNextTriggerTime(hour: Int, minute: Int, repeatDays: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = System.currentTimeMillis()

        if (repeatDays == 0) {
            // One-time alarm
            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // Repeating alarm: check next 7 days
        for (dayOffset in 0..7) {
            val checkCal = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            if (checkCal.timeInMillis > now) {
                // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2 .. Saturday=7
                // Our bitmask: 0=Mon, 1=Tue, 2=Wed, 3=Thu, 4=Fri, 5=Sat, 6=Sun
                val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK)
                val bitIndex = when (dayOfWeek) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    Calendar.SUNDAY -> 6
                    else -> 0
                }

                if ((repeatDays and (1 shl bitIndex)) != 0) {
                    return checkCal.timeInMillis
                }
            }
        }

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.timeInMillis
    }
}
