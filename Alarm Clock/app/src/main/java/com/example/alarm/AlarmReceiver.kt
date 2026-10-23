package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER = "com.example.auroraalarm.ACTION_TRIGGER_ALARM"
        const val ACTION_DISMISS = "com.example.auroraalarm.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE = "com.example.auroraalarm.ACTION_SNOOZE_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_TRIGGER
        val alarmId = intent.getLongExtra(AlarmAudioService.EXTRA_ALARM_ID, 0L)

        when (action) {
            ACTION_DISMISS -> {
                val stopIntent = Intent(context, AlarmAudioService::class.java).apply {
                    this.action = AlarmAudioService.ACTION_STOP_ALARM
                }
                context.startService(stopIntent)
            }
            ACTION_SNOOZE -> {
                val stopIntent = Intent(context, AlarmAudioService::class.java).apply {
                    this.action = AlarmAudioService.ACTION_STOP_ALARM
                }
                context.startService(stopIntent)

                val scheduler = AlarmScheduler(context)
                scheduler.scheduleSnooze(alarmId, snoozeMinutes = 9)
            }
            else -> {
                // ACTION_TRIGGER
                val label = intent.getStringExtra(AlarmAudioService.EXTRA_ALARM_LABEL) ?: "Morning Sunrise"
                val soundName = intent.getStringExtra(AlarmAudioService.EXTRA_SOUND_NAME) ?: "Forest Dawn Birds"
                val customSoundUri = intent.getStringExtra(AlarmAudioService.EXTRA_CUSTOM_SOUND_URI)
                val sunriseDuration = intent.getIntExtra(AlarmAudioService.EXTRA_SUNRISE_DURATION, 15)
                val vibrate = intent.getBooleanExtra(AlarmAudioService.EXTRA_VIBRATE, true)
                val challengeType = intent.getStringExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE) ?: "NONE"
                val ambientTheme = intent.getStringExtra(AlarmAudioService.EXTRA_AMBIENT_THEME) ?: "SUNRISE_GOLD"

                // Start Foreground Service
                val serviceIntent = Intent(context, AlarmAudioService::class.java).apply {
                    this.action = AlarmAudioService.ACTION_START_ALARM
                    putExtra(AlarmAudioService.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmAudioService.EXTRA_ALARM_LABEL, label)
                    putExtra(AlarmAudioService.EXTRA_SOUND_NAME, soundName)
                    putExtra(AlarmAudioService.EXTRA_CUSTOM_SOUND_URI, customSoundUri)
                    putExtra(AlarmAudioService.EXTRA_SUNRISE_DURATION, sunriseDuration)
                    putExtra(AlarmAudioService.EXTRA_VIBRATE, vibrate)
                    putExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE, challengeType)
                    putExtra(AlarmAudioService.EXTRA_AMBIENT_THEME, ambientTheme)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }

                // Launch Alarm Ringing Activity
                val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    putExtra(AlarmAudioService.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmAudioService.EXTRA_ALARM_LABEL, label)
                    putExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE, challengeType)
                    putExtra(AlarmAudioService.EXTRA_AMBIENT_THEME, ambientTheme)
                }
                context.startActivity(activityIntent)
            }
        }
    }
}
