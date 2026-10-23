package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SunriseGold
import com.example.ui.viewmodel.AlarmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NightStandClockScreen(
    viewModel: AlarmViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRedShift by remember { mutableStateOf(true) }
    var brightnessLevel by remember { mutableFloatStateOf(0.15f) } // 0.05, 0.15, 0.40
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    val alarms by viewModel.alarms.collectAsState()
    val nextAlarm = alarms.firstOrNull { it.isEnabled }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

        while (true) {
            val now = Date()
            currentTimeString = "${timeFormat.format(now)} ${amPmFormat.format(now)}"
            currentDateString = dateFormat.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    // Colors: Red-shift or Midnight Amber
    val textColor = if (isRedShift) {
        Color(0xFFFF4D4D).copy(alpha = (brightnessLevel + 0.35f).coerceAtMost(1f))
    } else {
        SunriseGold.copy(alpha = (brightnessLevel + 0.35f).coerceAtMost(1f))
    }

    val subTextColor = if (isRedShift) {
        Color(0xFFB32626).copy(alpha = (brightnessLevel + 0.25f).coerceAtMost(1f))
    } else {
        Color(0xFFA0A5C2).copy(alpha = (brightnessLevel + 0.25f).coerceAtMost(1f))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black) // True OLED Black to preserve battery & darkness
            .testTag("nightstand_clock_screen")
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Exit Nightstand",
                    tint = subTextColor
                )
            }

            // Mode & Brightness Toggles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Red-shift toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isRedShift) Color(0xFF380808) else Color(0xFF1E2235))
                        .clickable { isRedShift = !isRedShift }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isRedShift) "🔴 Red-Shift" else "🌙 Soft Amber",
                        fontSize = 11.sp,
                        color = if (isRedShift) Color(0xFFFF8A80) else SunriseGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Dimmer cycle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E2235))
                        .clickable {
                            brightnessLevel = when (brightnessLevel) {
                                0.08f -> 0.20f
                                0.20f -> 0.50f
                                else -> 0.08f
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Dim ${(brightnessLevel * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Center Gigantic Clock
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTimeString.ifEmpty { "02:45 AM" },
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 78.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentDateString.ifEmpty { "Thursday, October 23" },
                fontSize = 16.sp,
                color = subTextColor,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Next Alarm Info Pill
            if (nextAlarm != null) {
                Row(
                    modifier = Modifier
                        .background(
                            if (isRedShift) Color(0x33B32626) else Color(0xFF1C2034),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Sunrise",
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alarm set for ${nextAlarm.formattedTime()} (${nextAlarm.sunriseDurationMinutes}m sunrise)",
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Info
        Text(
            text = "Melatonin-safe OLED bedside display • Tap anywhere to adjust dimming",
            fontSize = 10.sp,
            color = subTextColor.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )
    }
}
