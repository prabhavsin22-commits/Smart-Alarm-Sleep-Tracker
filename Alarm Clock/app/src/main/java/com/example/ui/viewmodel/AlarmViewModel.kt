package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.alarm.AmbientAudioEngine
import com.example.data.AuraRiseDatabase
import com.example.data.CustomDeviceTone
import com.example.data.CustomToneManager
import com.example.data.HealthSyncRepository
import com.example.data.HealthSyncStatus
import com.example.data.model.AlarmEntity
import com.example.data.model.SleepSessionEntity
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveNapState(
    val isRunning: Boolean = false,
    val durationMinutes: Int = 20,
    val startTimeMillis: Long = 0L,
    val remainingSeconds: Int = 0,
    val label: String = "Power Nap"
)

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AuraRiseDatabase.getDatabase(application)
    private val scheduler = AlarmScheduler(application)
    private val healthRepo = HealthSyncRepository(application)
    private val audioEngine = AmbientAudioEngine(application)

    val alarms: StateFlow<List<AlarmEntity>> = database.alarmDao().getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepSessions: StateFlow<List<SleepSessionEntity>> = database.sleepDao().getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestSession: StateFlow<SleepSessionEntity?> = database.sleepDao().getLatestSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val healthSyncStatus: StateFlow<HealthSyncStatus> = healthRepo.syncStatus

    private val _themeMode = MutableStateFlow(AppThemeMode.DEEP_INDIGO)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _activeNap = MutableStateFlow(ActiveNapState())
    val activeNap: StateFlow<ActiveNapState> = _activeNap.asStateFlow()

    private val _previewingSound = MutableStateFlow<String?>(null)
    val previewingSound: StateFlow<String?> = _previewingSound.asStateFlow()

    private val _customTones = MutableStateFlow<List<CustomDeviceTone>>(emptyList())
    val customTones: StateFlow<List<CustomDeviceTone>> = _customTones.asStateFlow()

    init {
        loadCustomTones()
    }

    fun loadCustomTones() {
        _customTones.value = CustomToneManager.getSavedCustomTones(getApplication())
    }

    fun importCustomAudio(uri: Uri): CustomDeviceTone? {
        val tone = CustomToneManager.importAudioFile(getApplication(), uri)
        if (tone != null) {
            loadCustomTones()
        }
        return tone
    }

    fun registerSystemRingtone(uri: Uri, title: String): CustomDeviceTone {
        val tone = CustomToneManager.registerSystemRingtone(getApplication(), uri, title)
        loadCustomTones()
        return tone
    }

    fun removeCustomTone(toneId: String) {
        CustomToneManager.removeCustomTone(getApplication(), toneId)
        loadCustomTones()
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun toggleHighContrast(enabled: Boolean) {
        _isHighContrast.value = enabled
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun saveAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            if (alarm.id == 0L) {
                val newId = database.alarmDao().insertAlarm(alarm)
                scheduler.scheduleAlarm(alarm.copy(id = newId))
            } else {
                database.alarmDao().updateAlarm(alarm)
                scheduler.scheduleAlarm(alarm)
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = isEnabled)
            database.alarmDao().updateAlarm(updated)
            if (isEnabled) {
                scheduler.scheduleAlarm(updated)
            } else {
                scheduler.cancelAlarm(alarm.id)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            scheduler.cancelAlarm(alarm.id)
            database.alarmDao().deleteAlarm(alarm)
        }
    }

    fun startNap(durationMinutes: Int, label: String = "Power Nap") {
        scheduler.scheduleNap(durationMinutes, label)
        _activeNap.value = ActiveNapState(
            isRunning = true,
            durationMinutes = durationMinutes,
            startTimeMillis = System.currentTimeMillis(),
            remainingSeconds = durationMinutes * 60,
            label = label
        )

        // Launch countdown ticker
        viewModelScope.launch {
            while (_activeNap.value.isRunning && _activeNap.value.remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                val remaining = _activeNap.value.remainingSeconds - 1
                if (remaining <= 0) {
                    _activeNap.value = _activeNap.value.copy(isRunning = false, remainingSeconds = 0)
                } else {
                    _activeNap.value = _activeNap.value.copy(remainingSeconds = remaining)
                }
            }
        }
    }

    fun cancelNap() {
        scheduler.cancelAlarm(999999L)
        _activeNap.value = ActiveNapState(isRunning = false)
    }

    fun syncHealthData() {
        viewModelScope.launch {
            val newSession = healthRepo.syncLatestNightData()
            database.sleepDao().insertSession(newSession)
        }
    }

    fun toggleHealthConnection(enable: Boolean) {
        healthRepo.toggleHealthConnection(enable)
    }

    fun previewSound(soundName: String, customSoundUri: String? = null) {
        if (_previewingSound.value == soundName) {
            stopPreviewSound()
        } else {
            _previewingSound.value = soundName
            audioEngine.startSoundscape(
                soundName = soundName,
                customSoundUri = customSoundUri,
                rampDurationSeconds = 4,
                maxVolume = 0.85f,
                scope = viewModelScope
            )
        }
    }

    fun stopPreviewSound() {
        _previewingSound.value = null
        audioEngine.stop()
    }

    override fun onCleared() {
        audioEngine.stop()
        super.onCleared()
    }
}
