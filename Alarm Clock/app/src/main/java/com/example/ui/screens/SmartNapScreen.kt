package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SoundCatalog
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import com.example.ui.viewmodel.AlarmViewModel

data class NapProfile(
    val title: String,
    val durationMinutes: Int,
    val tag: String,
    val benefit: String,
    val color: Color
)

@Composable
fun SmartNapScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier
) {
    val activeNap by viewModel.activeNap.collectAsState()
    var selectedDuration by remember { mutableIntStateOf(20) }
    var selectedSound by remember { mutableStateOf("Gentle Rain & Thunder") }
    val previewingSound by viewModel.previewingSound.collectAsState()

    val napProfiles = listOf(
        NapProfile(
            title = "Power Nap",
            durationMinutes = 20,
            tag = "Alertness Boost",
            benefit = "Stops before slow-wave sleep to eliminate grogginess & boost motor reaction",
            color = SunriseGold
        ),
        NapProfile(
            title = "NASA Nap",
            durationMinutes = 26,
            tag = "Military Standard",
            benefit = "Scientifically proven to boost alertness by 54% and performance by 34%",
            color = AlertCyan
        ),
        NapProfile(
            title = "Recharge Nap",
            durationMinutes = 45,
            tag = "Mental Reset",
            benefit = "Short-wave memory clearing and nervous system deceleration",
            color = SleepRemLavender
        ),
        NapProfile(
            title = "Full Cycle Nap",
            durationMinutes = 90,
            tag = "Deep Recovery",
            benefit = "Complete REM + slow-wave sleep cycle for creativity & emotional balance",
            color = SunriseWarmOrange
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("smart_nap_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF38230D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Nap",
                            tint = SunriseGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Smart Power Nap",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Targeted recovery without sleep inertia",
                            fontSize = 12.sp,
                            color = Color(0xFFA5ABC7)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // If a nap is actively running: show countdown progress card
            if (activeNap.isRunning) {
                item {
                    ActiveNapCard(
                        activeNap = activeNap,
                        onCancel = { viewModel.cancelNap() }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Power Nap Profiles Section
            item {
                Text(
                    text = "Recovery Profiles",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(napProfiles.size) { index ->
                val profile = napProfiles[index]
                val isSelected = selectedDuration == profile.durationMinutes && !activeNap.isRunning

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { selectedDuration = profile.durationMinutes }
                        .testTag("nap_profile_${profile.durationMinutes}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF22263C) else Color(0xFF141726)
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(profile.color, SunriseGold))
                    ) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(profile.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${profile.durationMinutes}m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = profile.color
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(profile.color.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = profile.tag,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = profile.color
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = profile.benefit,
                                fontSize = 11.sp,
                                color = Color(0xFFA0A5C2),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Custom Duration Slider
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141726))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Custom Nap Length",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "$selectedDuration min",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunriseGold
                            )
                        }

                        Slider(
                            value = selectedDuration.toFloat(),
                            onValueChange = { selectedDuration = it.toInt() },
                            valueRange = 10f..120f,
                            steps = 21,
                            colors = SliderDefaults.colors(
                                thumbColor = SunriseGold,
                                activeTrackColor = SunriseWarmOrange,
                                inactiveTrackColor = Color(0xFF262A42)
                            )
                        )
                    }
                }
            }

            // Pre-Nap Ambient Sound Selector
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "Sleep Aid Soundscape (Fadeout Timer)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SoundCatalog.soundscapes.take(3).forEach { sound ->
                        val isSelected = selectedSound == sound.name
                        val isPlaying = previewingSound == sound.name

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2E203C) else Color(0xFF161928))
                                .clickable {
                                    selectedSound = sound.name
                                    viewModel.previewSound(sound.name)
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                    contentDescription = "Sound",
                                    tint = if (isSelected) SunriseGold else Color(0xFF8E93B0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sound.name.replace(" & Thunder", "").replace(" Dawn", ""),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SunriseGold else Color(0xFFA5ABC7),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Start Nap Button (Easily reachable at bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xF50D0E19), Color(0xFF0D0E19))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = {
                    val label = when (selectedDuration) {
                        20 -> "20m Power Nap"
                        26 -> "26m NASA Nap"
                        45 -> "45m Recharge Nap"
                        90 -> "90m Full Cycle"
                        else -> "$selectedDuration min Nap"
                    }
                    viewModel.startNap(selectedDuration, label)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_nap_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunriseGold)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Start",
                    tint = Color(0xFF1D1600),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start $selectedDuration min Recovery Nap",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1600)
                )
            }
        }
    }
}

@Composable
private fun ActiveNapCard(
    activeNap: com.example.ui.viewmodel.ActiveNapState,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_nap_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2238))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ ${activeNap.label} in Progress",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunriseGold
                )

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "End", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Circular progress with remaining time
            val totalSeconds = activeNap.durationMinutes * 60
            val progress = (activeNap.remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
            val minutes = activeNap.remainingSeconds / 60
            val seconds = activeNap.remainingSeconds % 60

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f - progress },
                    modifier = Modifier.size(140.dp),
                    color = SunriseGold,
                    trackColor = Color(0xFF2E334E),
                    strokeWidth = 8.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Remaining",
                        fontSize = 11.sp,
                        color = Color(0xFFA5ABC7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Rest peacefully. AuraRise will awaken you gently with progressive sunrise glow.",
                fontSize = 11.sp,
                color = Color(0xFFA5ABC7),
                textAlign = TextAlign.Center
            )
        }
    }
}
