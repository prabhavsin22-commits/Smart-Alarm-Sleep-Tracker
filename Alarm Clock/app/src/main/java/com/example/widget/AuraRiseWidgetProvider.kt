package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.alarm.AlarmScheduler
import com.example.data.AuraRiseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AuraRiseWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_QUICK_NAP = "com.example.auroraalarm.ACTION_QUICK_NAP_WIDGET"

        fun updateWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AuraRiseWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, AuraRiseWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_NAP) {
            val scheduler = AlarmScheduler(context)
            scheduler.scheduleNap(20, "Widget Power Nap")
            updateWidgets(context)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AuraRiseDatabase.getDatabase(context)
            val enabledAlarms = database.alarmDao().getEnabledAlarms().firstOrNull() ?: emptyList()
            val latestSession = database.sleepDao().getLatestSession().firstOrNull()

            val nextAlarm = enabledAlarms.firstOrNull()
            val nextTimeText = nextAlarm?.formattedTime() ?: "No Alarm"
            val detailText = if (nextAlarm != null) {
                "${nextAlarm.soundName} (${nextAlarm.sunriseDurationMinutes}m sunrise)"
            } else {
                "Tap to set morning sunrise"
            }
            val scoreText = if (latestSession != null) "Sleep ${latestSession.sleepScore}%" else "Sleep 90%"

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_aurora_layout)

                views.setTextViewText(R.id.widget_next_alarm_time, nextTimeText)
                views.setTextViewText(R.id.widget_alarm_detail, detailText)
                views.setTextViewText(R.id.widget_sleep_score, scoreText)

                // Root click -> Open MainActivity
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

                // Quick Nap button click -> Trigger ACTION_QUICK_NAP
                val napIntent = Intent(context, AuraRiseWidgetProvider::class.java).apply {
                    action = ACTION_QUICK_NAP
                }
                val napPendingIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    napIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_quick_nap, napPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
