package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AlarmAudioService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var audioEngine: AmbientAudioEngine? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "sunrise_alarm_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_ALARM = "com.example.auroraalarm.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.auroraalarm.ACTION_STOP_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_SOUND_NAME = "extra_sound_name"
        const val EXTRA_SUNRISE_DURATION = "extra_sunrise_duration"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_CHALLENGE_TYPE = "extra_challenge_type"
        const val EXTRA_AMBIENT_THEME = "extra_ambient_theme"
        const val EXTRA_CUSTOM_SOUND_URI = "extra_custom_sound_uri"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        audioEngine = AmbientAudioEngine(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "AuraRise::AlarmWakeLock"
        )
        wakeLock?.acquire(15 * 60 * 1000L) // 15 mins max timeout

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_ALARM

        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, 0L) ?: 0L
        val label = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "Morning Sunrise"
        val soundName = intent?.getStringExtra(EXTRA_SOUND_NAME) ?: "Forest Dawn Birds"
        val customSoundUri = intent?.getStringExtra(EXTRA_CUSTOM_SOUND_URI)
        val sunriseDuration = intent?.getIntExtra(EXTRA_SUNRISE_DURATION, 15) ?: 15
        val shouldVibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true
        val challengeType = intent?.getStringExtra(EXTRA_CHALLENGE_TYPE) ?: "NONE"
        val ambientTheme = intent?.getStringExtra(EXTRA_AMBIENT_THEME) ?: "SUNRISE_GOLD"

        val notification = buildForegroundNotification(alarmId, label, challengeType, ambientTheme)
        startForeground(NOTIFICATION_ID, notification)

        // Start Ambient Soundscape / Custom Tone
        audioEngine?.startSoundscape(
            soundName = soundName,
            customSoundUri = customSoundUri,
            rampDurationSeconds = (sunriseDuration * 60).coerceIn(10, 180),
            maxVolume = 1.0f,
            scope = serviceScope
        )

        // Start Gentle Vibration Pattern
        if (shouldVibrate) {
            startVibration()
        }

        return START_STICKY
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 500, 1500, 500, 1500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (_: Exception) {}
    }

    private fun stopAlarm() {
        audioEngine?.stop()
        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    private fun buildForegroundNotification(alarmId: Long, label: String, challengeType: String, ambientTheme: String): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_CHALLENGE_TYPE, challengeType)
            putExtra(EXTRA_AMBIENT_THEME, ambientTheme)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.toInt() + 100,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId.toInt() + 200,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $label")
            .setContentText("Gradual Ambient Light & Sound Alert")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, "Snooze (9m)", snoozePendingIntent)
            .addAction(0, "Wake Up", fullScreenPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sunrise Ambient Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gradual ambient light and sound wake up notifications"
                setBypassDnd(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopAlarm()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
