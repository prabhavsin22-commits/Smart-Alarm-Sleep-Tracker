package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.alarm.AlarmAudioService
import com.example.alarm.AlarmRingingActivity
import com.example.data.model.AlarmEntity
import com.example.data.model.AmbientTheme
import com.example.data.model.ChallengeType
import com.example.ui.components.TimePickerBottomSheet
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseDeepAmber
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import com.example.ui.viewmodel.AlarmViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: AlarmViewModel,
    alarms: List<AlarmEntity>,
    onNavigateToNap: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToNightstand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var isCreatingAlarm by remember { mutableStateOf(false) }

    var currentTimeString by remember { mutableStateOf("") }
    var currentHourOfDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }

    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        while (true) {
            val now = Calendar.getInstance()
            currentTimeString = formatter.format(now.time)
            currentHourOfDay = now.get(Calendar.HOUR_OF_DAY)
            kotlinx.coroutines.delay(1000)
        }
    }

    val nextAlarm = alarms.firstOrNull { it.isEnabled }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // Adaptive Time-of-Day Hero Status Header
            item {
                AdaptiveTimeHeroCard(
                    currentTime = currentTimeString,
                    hourOfDay = currentHourOfDay,
                    nextAlarm = nextAlarm,
                    onEditNext = { nextAlarm?.let { editingAlarm = it } }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Access Mode Pills (Power Recovery Nap & Bedside OLED Clock)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickPill(
                        icon = Icons.Default.Bolt,
                        title = "Power Nap",
                        subtitle = "Targeted Recovery",
                        color = SunriseGold,
                        onClick = onNavigateToNap,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_power_nap_pill")
                    )
                    QuickPill(
                        icon = Icons.Default.Nightlight,
                        title = "Nightstand",
                        subtitle = "Bedside OLED Clock",
                        color = SleepRemLavender,
                        onClick = onNavigateToNightstand,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_nightstand_pill")
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Quick Presets Row (Morning, Afternoon, Evening, Night)
            item {
                Text(
                    text = "Quick Presets (All-Day)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFA5ABC7)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickAddPresetPill(
                            icon = "☀️",
                            title = "Morning Wake",
                            time = "07:00 AM",
                            onClick = {
                                editingAlarm = AlarmEntity(
                                    hour = 7,
                                    minute = 0,
                                    label = "Morning Wake",
                                    ambientThemeName = "SUNRISE_GOLD",
                                    soundName = "Forest Dawn Birds"
                                )
                            }
                        )
                    }
                    item {
                        QuickAddPresetPill(
                            icon = "💡",
                            title = "Midday Focus",
                            time = "12:30 PM",
                            onClick = {
                                editingAlarm = AlarmEntity(
                                    hour = 12,
                                    minute = 30,
                                    label = "Lunch & Focus",
                                    ambientThemeName = "CRISP_DAYLIGHT",
                                    soundName = "Zen Garden Stream"
                                )
                            }
                        )
                    }
                    item {
                        QuickAddPresetPill(
                            icon = "🌊",
                            title = "Afternoon Break",
                            time = "03:15 PM",
                            onClick = {
                                editingAlarm = AlarmEntity(
                                    hour = 15,
                                    minute = 15,
                                    label = "Afternoon Break",
                                    ambientThemeName = "OCEAN_AURORA",
                                    soundName = "Ocean Horizon Waves"
                                )
                            }
                        )
                    }
                    item {
                        QuickAddPresetPill(
                            icon = "🌇",
                            title = "Evening Wind Down",
                            time = "08:00 PM",
                            onClick = {
                                editingAlarm = AlarmEntity(
                                    hour = 20,
                                    minute = 0,
                                    label = "Evening Rest",
                                    ambientThemeName = "SUNSET_TWILIGHT",
                                    soundName = "Tibetan Singing Bowl"
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section Header: Alarms
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Ambient Alarms",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${alarms.count { it.isEnabled }} active",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SunriseGold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (alarms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141726))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "No Alarms",
                                tint = SunriseGold,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Alarms Scheduled",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tap '+ Set Alarm' below to set an alarm for any time of day",
                                fontSize = 12.sp,
                                color = Color(0xFFA5ABC7)
                            )
                        }
                    }
                }
            } else {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                        onClick = { editingAlarm = alarm },
                        onDelete = { viewModel.deleteAlarm(alarm) },
                        onPreviewSound = { viewModel.previewSound(alarm.soundName) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Bottom Dock for primary alarm actions (Easily reachable at bottom of screen)
        SurfaceBottomActionBar(
            onAddAlarm = { isCreatingAlarm = true },
            onTestRinging = {
                // Instantly triggers ringing activity with real light & audio preview
                val testIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(AlarmAudioService.EXTRA_ALARM_ID, 99999L)
                    putExtra(AlarmAudioService.EXTRA_ALARM_LABEL, nextAlarm?.label ?: "Ambient Wake Preview")
                    putExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE, nextAlarm?.challengeType?.name ?: "NONE")
                    putExtra(AlarmAudioService.EXTRA_AMBIENT_THEME, nextAlarm?.ambientThemeName ?: "SUNRISE_GOLD")
                }
                context.startActivity(testIntent)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Time picker bottom sheet for creating or editing alarm
    if (isCreatingAlarm || editingAlarm != null) {
        TimePickerBottomSheet(
            initialAlarm = editingAlarm,
            onDismiss = {
                isCreatingAlarm = false
                editingAlarm = null
            },
            onSave = { alarmToSave ->
                viewModel.saveAlarm(alarmToSave)
                isCreatingAlarm = false
                editingAlarm = null
            }
        )
    }
}

@Composable
private fun AdaptiveTimeHeroCard(
    currentTime: String,
    hourOfDay: Int,
    nextAlarm: AlarmEntity?,
    onEditNext: () -> Unit
) {
    val (greeting, periodTag) = when (hourOfDay) {
        in 5..11 -> Pair("Good Morning", "Morning Routine")
        in 12..16 -> Pair("Good Afternoon", "Daytime Focus")
        in 17..21 -> Pair("Good Evening", "Evening Unwind")
        else -> Pair("Peaceful Night", "Rest & Recovery")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_sunrise_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141727))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.morning_sunrise_sky_1792759331915),
                contentDescription = "Skyline",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC141727), Color(0xFF141727))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = greeting,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .background(Color(0x8010121C), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SunriseGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = periodTag,
                            fontSize = 10.sp,
                            color = SunriseGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = currentTime.ifEmpty { "07:15 AM" },
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Next Alarm Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1D2138))
                        .clickable { onEditNext() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val themeIcon = nextAlarm?.getAmbientTheme()?.icon ?: "⏰"
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF2E243C), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = themeIcon, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nextAlarm != null) "Next: ${nextAlarm.formattedTime()}" else "No active alarm",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (nextAlarm != null) "${nextAlarm.label} • ${nextAlarm.timeRemainingText()}" else "Tap '+ Set Alarm' below to set an alarm",
                            fontSize = 11.sp,
                            color = Color(0xFFA0A5C2)
                        )
                    }

                    if (nextAlarm != null) {
                        Text(
                            text = "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SunriseGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddPresetPill(
    icon: String,
    title: String,
    time: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181C2E))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = time, fontSize = 10.sp, color = SunriseGold)
            }
        }
    }
}

@Composable
private fun QuickPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161928))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 10.sp, color = Color(0xFF8E93B0))
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPreviewSound: () -> Unit
) {
    val ambientTheme = alarm.getAmbientTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("alarm_item_${alarm.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16192A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = alarm.formattedTime(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) Color.White else Color(0xFF6E7490)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF22263C), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = alarm.timePeriodLabel(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AlertCyan
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${alarm.label} • ${alarm.daysSummary()}",
                            fontSize = 12.sp,
                            color = if (alarm.isEnabled) SunriseGold else Color(0xFF6E7490)
                        )
                        if (alarm.isEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${alarm.timeRemainingText()})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF80DEEA)
                            )
                        }
                    }
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SunriseGold,
                        checkedTrackColor = SunriseDeepAmber,
                        uncheckedTrackColor = Color(0xFF22263A)
                    ),
                    modifier = Modifier.testTag("alarm_toggle_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details tags: Ambient theme, Light ramp, Soundscape, Challenge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailTag(
                    text = "${ambientTheme.icon} ${ambientTheme.displayName}",
                    color = SunriseGold
                )
                if (alarm.sunriseDurationMinutes > 0) {
                    DetailTag(
                        text = "💡 ${alarm.sunriseDurationMinutes}m ramp",
                        color = SunriseWarmOrange
                    )
                }
                DetailTag(
                    text = "🎵 ${alarm.soundName.replace(" & Thunder", "")}",
                    color = AlertCyan
                )
                if (alarm.challengeType != ChallengeType.NONE) {
                    DetailTag(
                        text = when (alarm.challengeType) {
                            ChallengeType.MATH_PUZZLE -> "🧮 Math"
                            ChallengeType.SKY_CAPTURE -> "🌤️ Light Scan"
                            ChallengeType.MEMORY_SEQUENCE -> "🧠 Memory"
                            ChallengeType.NONE -> ""
                        },
                        color = SleepRemLavender
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alarm",
                        tint = Color(0xFF8E93B0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun SurfaceBottomActionBar(
    onAddAlarm: () -> Unit,
    onTestRinging: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0xF50D0E19), Color(0xFF0D0E19))
                )
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test Alarm Preview Button
            OutlinedButton(
                onClick = onTestRinging,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("test_sunrise_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCyan)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Test Alarm",
                    tint = AlertCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test Alarm",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Primary Add Alarm Button
            Button(
                onClick = onAddAlarm,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .testTag("add_alarm_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunriseGold)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm",
                    tint = Color(0xFF1D1600),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Set Alarm",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1600)
                )
            }
        }
    }
}
