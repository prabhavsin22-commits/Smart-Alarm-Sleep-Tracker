package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AuraRiseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            val scheduler = AlarmScheduler(context)
            val database = AuraRiseDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                val enabledAlarms = database.alarmDao().getEnabledAlarms().firstOrNull() ?: emptyList()
                for (alarm in enabledAlarms) {
                    scheduler.scheduleAlarm(alarm)
                }
            }
        }
    }
}
