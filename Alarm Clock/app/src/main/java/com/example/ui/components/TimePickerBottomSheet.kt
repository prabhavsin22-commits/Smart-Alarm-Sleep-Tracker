package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AmbientAudioEngine
import com.example.data.CustomDeviceTone
import com.example.data.CustomToneManager
import com.example.data.SoundCatalog
import com.example.data.model.AlarmEntity
import com.example.data.model.AmbientTheme
import com.example.data.model.ChallengeType
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import java.util.Calendar

enum class EditorTab {
    TIME_REPEAT,
    LIGHT_AND_SOUND,
    CHALLENGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerBottomSheet(
    initialAlarm: AlarmEntity? = null,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 15) }
    var label by remember { mutableStateOf(initialAlarm?.label ?: "Daily Alarm") }
    var repeatDays by remember { mutableIntStateOf(initialAlarm?.repeatDays ?: 31) }
    var soundName by remember { mutableStateOf(initialAlarm?.soundName ?: "Forest Dawn Birds") }
    var customSoundUri by remember { mutableStateOf(initialAlarm?.customSoundUri) }
    var sunriseDuration by remember { mutableIntStateOf(initialAlarm?.sunriseDurationMinutes ?: 10) }
    var vibrate by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }
    var challengeType by remember { mutableStateOf(initialAlarm?.challengeType ?: ChallengeType.NONE) }
    var smartWake by remember { mutableStateOf(initialAlarm?.smartWakeCycle ?: false) }
    var ambientThemeName by remember { mutableStateOf(initialAlarm?.ambientThemeName ?: "SUNRISE_GOLD") }

    var customTones by remember { mutableStateOf(CustomToneManager.getSavedCustomTones(context)) }
    val previewEngine = remember { AmbientAudioEngine(context) }
    var previewingTone by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            previewEngine.stop()
        }
    }

    // Audio file picker launcher (.mp3, .wav, .m4a, etc.)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val imported = CustomToneManager.importAudioFile(context, uri)
            if (imported != null) {
                customTones = CustomToneManager.getSavedCustomTones(context)
                soundName = imported.displayName
                customSoundUri = imported.uriString
            }
        }
    }

    // System ringtone picker launcher
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedUri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (pickedUri != null) {
                val ringtone = RingtoneManager.getRingtone(context, pickedUri)
                val title = ringtone?.getTitle(context) ?: "Device Ringtone"
                val saved = CustomToneManager.registerSystemRingtone(context, pickedUri, title)
                customTones = CustomToneManager.getSavedCustomTones(context)
                soundName = saved.displayName
                customSoundUri = saved.uriString
            }
        }
    }

    var selectedTab by remember { mutableStateOf(EditorTab.TIME_REPEAT) }

    // Live countdown calculation
    val tempAlarm = AlarmEntity(hour = hour, minute = minute)
    val countdownText = tempAlarm.timeRemainingText()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131522)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialAlarm == null) "New Alarm" else "Edit Alarm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = countdownText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SunriseGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs for effortless configuration
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color(0xFF181C2E),
                contentColor = SunriseGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = SunriseGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == EditorTab.TIME_REPEAT,
                    onClick = { selectedTab = EditorTab.TIME_REPEAT },
                    text = { Text("Time & Recurrence", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == EditorTab.LIGHT_AND_SOUND,
                    onClick = { selectedTab = EditorTab.LIGHT_AND_SOUND },
                    text = { Text("Light & Sound", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == EditorTab.CHALLENGE,
                    onClick = { selectedTab = EditorTab.CHALLENGE },
                    text = { Text("Wake Task", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                EditorTab.TIME_REPEAT -> {
                    // Interactive Stepper with 1-minute single-digit precision
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E30))
                    ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Hour Stepper
                                    DigitControlColumn(
                                        displayValue = String.format("%02d", if (hour % 12 == 0) 12 else hour % 12),
                                        label = "HOUR",
                                        onStepUp = {
                                            hour = (hour + 1) % 24
                                        },
                                        onStepDown = {
                                            hour = if (hour - 1 < 0) 23 else hour - 1
                                        }
                                    )

                                    Text(
                                        text = ":",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SunriseGold,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    )

                                    // Minute Stepper (Supports every single minute 0-59!)
                                    DigitControlColumn(
                                        displayValue = String.format("%02d", minute),
                                        label = "EXACT MINUTE",
                                        onStepUp = {
                                            minute = (minute + 1) % 60
                                        },
                                        onStepDown = {
                                            minute = if (minute - 1 < 0) 59 else minute - 1
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // AM / PM Toggle
                                    val isPm = hour >= 12
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        PillChoice(text = "AM", isSelected = !isPm) {
                                            if (isPm) hour -= 12
                                        }
                                        PillChoice(text = "PM", isSelected = isPm) {
                                            if (!isPm) hour += 12
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick minute increment/decrement chips (+1, -1, +5, -5)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    QuickMinuteChip(text = "+1m") { minute = (minute + 1) % 60 }
                                    QuickMinuteChip(text = "-1m") { minute = if (minute - 1 < 0) 59 else minute - 1 }
                                    QuickMinuteChip(text = "+5m") { minute = (minute + 5) % 60 }
                                    QuickMinuteChip(text = "-5m") { minute = if (minute - 5 < 0) 55 else minute - 5 }
                                    QuickMinuteChip(text = "+15m") { minute = (minute + 15) % 60 }
                                }
                            }
                        }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick All-Day Presets
                    Text(
                        text = "Quick Presets (Any Time of Day)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA5ABC7)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickPresetChip(title = "Morning", time = "07:00 AM") {
                            hour = 7; minute = 0; label = "Morning Rise"; ambientThemeName = "SUNRISE_GOLD"
                        }
                        QuickPresetChip(title = "Noon", time = "12:30 PM") {
                            hour = 12; minute = 30; label = "Lunch / Break"; ambientThemeName = "CRISP_DAYLIGHT"
                        }
                        QuickPresetChip(title = "Afternoon", time = "03:15 PM") {
                            hour = 15; minute = 15; label = "Afternoon Focus"; ambientThemeName = "OCEAN_AURORA"
                        }
                        QuickPresetChip(title = "Evening", time = "08:00 PM") {
                            hour = 20; minute = 0; label = "Evening Wind Down"; ambientThemeName = "SUNSET_TWILIGHT"
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Alarm Label
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Alarm Label / Purpose") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunriseGold,
                            unfocusedBorderColor = Color(0xFF2A2E44),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Repeat Days
                    Text(
                        text = "Repeat Schedule",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                        dayNames.forEachIndexed { index, dayLetter ->
                            val isSelected = (repeatDays and (1 shl index)) != 0
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SunriseGold else Color(0xFF1E2135))
                                    .clickable { repeatDays = repeatDays xor (1 shl index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLetter,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF12131C) else Color(0xFFA5ABC7)
                                )
                            }
                        }
                    }
                }

                EditorTab.LIGHT_AND_SOUND -> {
                    // Ambient Light Profile Customizer
                    Text(
                        text = "Ambient Light Glow Profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Select screen illumination style suitable for morning, day, or night:",
                        fontSize = 11.sp,
                        color = Color(0xFFA5ABC7)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AmbientTheme.values().forEach { theme ->
                        val isSelected = ambientThemeName == theme.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { ambientThemeName = theme.name },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF22263C) else Color(0xFF161928)
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(SunriseGold, SunriseWarmOrange))
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = theme.icon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SunriseGold else Color.White
                                    )
                                    Text(
                                        text = theme.description,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA0A5C2)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = SunriseGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gradual Light Ramp Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Gradual Light Ramp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = if (sunriseDuration == 0) "Instant Light" else "$sunriseDuration min ramp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunriseGold
                        )
                    }
                    Slider(
                        value = sunriseDuration.toFloat(),
                        onValueChange = { sunriseDuration = it.toInt() },
                        valueRange = 0f..30f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = SunriseGold,
                            activeTrackColor = SunriseWarmOrange,
                            inactiveTrackColor = Color(0xFF262A42)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sound Selection (Device Custom Tones, Loud Alarm Tones & Ambient Nature)
                    Text(
                        text = "Alarm Tone & Soundscape",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Group 1: Device & Custom Tones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📱 Your Device Tones & Files",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunriseGold
                        )
                        Text(
                            text = if (customTones.isEmpty()) "Use your own MP3/Ringtone" else "${customTones.size} imported",
                            fontSize = 10.sp,
                            color = Color(0xFFA5ABC7)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons to pick audio file or system ringtone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("pick_audio_file_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SunriseBrightSun),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A3E1B))
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Audio File", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Device Tone")
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                }
                                ringtonePickerLauncher.launch(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("pick_device_ringtone_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3845))
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Device Ringtone", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Display saved custom tones if available
                    if (customTones.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        customTones.forEach { tone ->
                            val isSelected = (customSoundUri != null && customSoundUri == tone.uriString) || (customSoundUri == null && soundName == tone.displayName)
                            val isPlaying = previewingTone == tone.displayName

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        soundName = tone.displayName
                                        customSoundUri = tone.uriString
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF2E2614) else Color(0xFF161928)
                                ),
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(SunriseGold, SunriseWarmOrange))
                                ) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = tone.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) SunriseBrightSun else Color.White,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF383216), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = if (tone.isSystemRingtone) "SYSTEM" else "FILE",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SunriseGold
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (tone.isSystemRingtone) "Device built-in alarm / ringtone" else "Imported from device storage",
                                            fontSize = 10.sp,
                                            color = Color(0xFFA0A5C2)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) {
                                                    previewEngine.stop()
                                                    previewingTone = null
                                                } else {
                                                    previewEngine.startSoundscape(
                                                        soundName = tone.displayName,
                                                        customSoundUri = tone.uriString,
                                                        rampDurationSeconds = 4,
                                                        maxVolume = 0.85f,
                                                        scope = scope
                                                    )
                                                    previewingTone = tone.displayName
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = "Preview Tone",
                                                tint = if (isPlaying) Color(0xFFFF5252) else SunriseGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                CustomToneManager.removeCustomTone(context, tone.id)
                                                customTones = CustomToneManager.getSavedCustomTones(context)
                                                if (customSoundUri == tone.uriString) {
                                                    customSoundUri = null
                                                    soundName = "Forest Dawn Birds"
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFF6B728E),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = SunriseGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Group 2: Loud Alarm Tones
                    Text(
                        text = "🚨 Loud Tones (Heavy Sleepers & High Urgency)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    SoundCatalog.soundscapes.filter { it.isLoud }.forEach { s ->
                        val isSelected = (customSoundUri == null && soundName == s.name)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    soundName = s.name
                                    customSoundUri = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF381418) else Color(0xFF161928)
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(Color(0xFFFF5252), SunriseWarmOrange))
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = s.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFFFF8A80) else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF4A1010), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(text = "LOUD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80))
                                        }
                                    }
                                    Text(text = s.description, fontSize = 10.sp, color = Color(0xFFA0A5C2))
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Group 3: Ambient Nature Soundscapes
                    Text(
                        text = "🌿 Ambient & Nature Soundscapes (Gentle Rise)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertCyan,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    SoundCatalog.soundscapes.filter { !it.isLoud }.forEach { s ->
                        val isSelected = (customSoundUri == null && soundName == s.name)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    soundName = s.name
                                    customSoundUri = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF241C38) else Color(0xFF161928)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFD7BAFF) else Color.White
                                    )
                                    Text(text = s.description, fontSize = 10.sp, color = Color(0xFFA0A5C2))
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFD7BAFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                EditorTab.CHALLENGE -> {
                    Text(
                        text = "Wake-up Challenge (Anti-Snooze)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Optional task to prove alertness before the alarm stops:",
                        fontSize = 11.sp,
                        color = Color(0xFFA5ABC7)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        ChallengeType.NONE to Pair("None (Standard Alarm)", "Dismiss or snooze directly with bottom slider"),
                        ChallengeType.MATH_PUZZLE to Pair("🧮 Analytical Math Problem", "Solve mental arithmetic to wake up your prefrontal cortex"),
                        ChallengeType.SKY_CAPTURE to Pair("🌤️ Daylight / Room Light Scan", "Point camera up at window or natural light to get out of bed"),
                        ChallengeType.MEMORY_SEQUENCE to Pair("🧠 Memory Reflex Pattern", "Repeat a 5-step glowing tile sequence")
                    ).forEach { (type, info) ->
                        val isSelected = challengeType == type
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { challengeType = type },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF22263C) else Color(0xFF161928)
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(AlertCyan, SunriseGold))
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = info.first,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SunriseGold else Color.White
                                    )
                                    Text(
                                        text = info.second,
                                        fontSize = 11.sp,
                                        color = Color(0xFFA0A5C2)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = SunriseGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Vibration Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181C2E), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Haptic Vibration Pattern", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Gentle pulsating wake vibration", fontSize = 11.sp, color = Color(0xFFA0A5C2))
                        }
                        Switch(
                            checked = vibrate,
                            onCheckedChange = { vibrate = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SunriseGold, checkedTrackColor = SunriseWarmOrange)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        previewEngine.stop()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = Color(0xFFA5ABC7))
                }

                Button(
                    onClick = {
                        previewEngine.stop()
                        val alarm = (initialAlarm ?: AlarmEntity(
                            hour = hour,
                            minute = minute
                        )).copy(
                            hour = hour,
                            minute = minute,
                            label = label.ifBlank { "Alarm" },
                            repeatDays = repeatDays,
                            soundName = soundName,
                            customSoundUri = customSoundUri,
                            sunriseDurationMinutes = sunriseDuration,
                            vibrate = vibrate,
                            challengeType = challengeType,
                            smartWakeCycle = smartWake,
                            ambientThemeName = ambientThemeName,
                            isEnabled = true
                        )
                        onSave(alarm)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("save_alarm_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SunriseGold)
                ) {
                    Text(
                        text = "Save Alarm",
                        color = Color(0xFF1E1700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DigitControlColumn(
    displayValue: String,
    label: String,
    onStepUp: () -> Unit,
    onStepDown: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Step Up button
        IconButton(
            onClick = onStepUp,
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF22263C), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increment",
                tint = SunriseGold,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = displayValue,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Step Down button
        IconButton(
            onClick = onStepDown,
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFF22263C), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrement",
                tint = SunriseGold,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8E93B0)
        )
    }
}

@Composable
private fun QuickMinuteChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF23273E))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SunriseGold)
    }
}

@Composable
private fun QuickPresetChip(title: String, time: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1B1E30))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunriseGold)
            Text(text = time, fontSize = 9.sp, color = Color(0xFFA5ABC7))
        }
    }
}

@Composable
private fun PillChoice(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SunriseWarmOrange else Color(0xFF22263C))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF8F94B2)
        )
    }
}
