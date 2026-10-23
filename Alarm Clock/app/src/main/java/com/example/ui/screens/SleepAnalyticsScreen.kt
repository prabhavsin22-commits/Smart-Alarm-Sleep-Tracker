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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HealthSyncStatus
import com.example.data.model.SleepSessionEntity
import com.example.ui.components.HeartRateSleepChart
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import com.example.ui.viewmodel.AlarmViewModel

@Composable
fun SleepAnalyticsScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier
) {
    val sleepSessions by viewModel.sleepSessions.collectAsState()
    val healthStatus by viewModel.healthSyncStatus.collectAsState()
    val latestSession = sleepSessions.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("sleep_analytics_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF281C38), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Analytics",
                            tint = SleepRemLavender,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sleep & Health Analytics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Circadian hygiene & nocturnal cardiac dip",
                            fontSize = 12.sp,
                            color = Color(0xFFA5ABC7)
                        )
                    }
                }

                // Sync button
                OutlinedButton(
                    onClick = { viewModel.syncHealthData() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCyan),
                    modifier = Modifier.testTag("sync_health_button")
                ) {
                    if (healthStatus.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = AlertCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Sync", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Health Apps Integration Card
        item {
            HealthIntegrationCard(
                status = healthStatus,
                onToggleConnection = { viewModel.toggleHealthConnection(it) }
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Circadian Hygiene KPI Row
        item {
            Text(
                text = "Circadian Hygiene Metrics",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Wake Consistency",
                    value = "93%",
                    subtitle = "±15 min variance",
                    color = SuccessGreen,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Sleep Debt",
                    value = "-18m",
                    subtitle = "Well aligned",
                    color = AlertCyan,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Alertness Speed",
                    value = "${latestSession?.wakeReactionTimeSeconds ?: 14}s",
                    subtitle = "Post-task reflex",
                    color = SunriseGold,
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Night Resting Heart Rate & Nocturnal Dip Section
        item {
            Text(
                text = "Night Resting Heart Rate (Dip Analysis)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Healthy restorative sleep exhibits a 10-20% nocturnal HR dip:",
                fontSize = 11.sp,
                color = Color(0xFFA0A5C2)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val hrPoints = latestSession?.heartRatePoints
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?: listOf(66, 62, 57, 52, 49, 48, 51, 56, 62)

            HeartRateSleepChart(
                heartRatePoints = hrPoints,
                avgHr = latestSession?.avgHeartRate ?: healthStatus.nightRestingHrAverage,
                minHr = latestSession?.minHeartRate ?: 48,
                maxHr = latestSession?.maxHeartRate ?: 68,
                hrDipPercent = latestSession?.hrDipPercent ?: healthStatus.nocturnalDipPercent,
                deepPercent = latestSession?.deepSleepPercent ?: 23,
                remPercent = latestSession?.remSleepPercent ?: 24,
                lightPercent = latestSession?.lightSleepPercent ?: 48,
                awakePercent = latestSession?.awakePercent ?: 5
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Sleep Hygiene & Productivity Recommendations Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161A2C))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Tips",
                            tint = SunriseGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sleep Hygiene & Productivity Coaching",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HygieneTipItem(
                        icon = "🌅",
                        title = "Circadian Anchor",
                        desc = "Your wake consistency is 93%. Keeping this within 30 mins over weekends stabilizes morning adenosine clearance."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HygieneTipItem(
                        icon = "❤️",
                        title = "Cardiovascular Recovery",
                        desc = "Your nocturnal heart rate dip of 15.6% indicates strong parasympathetic nervous system restoration."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HygieneTipItem(
                        icon = "☕",
                        title = "Adenosine Window",
                        desc = "Delay caffeine by 60-90 minutes post-waking to avoid an afternoon energy crash."
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // History Log of Previous Nights
        item {
            Text(
                text = "Recent Night Sessions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(sleepSessions, key = { it.id }) { session ->
            NightSessionCard(session = session)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun HealthIntegrationCard(
    status: HealthSyncStatus,
    onToggleConnection: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16192A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF381418), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Health",
                            tint = HeartRateRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = status.sourceName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (status.isConnected) "Active • Continuous Heart Rate Synced" else "Disconnected",
                            fontSize = 10.sp,
                            color = if (status.isConnected) SuccessGreen else Color(0xFF8E93B0)
                        )
                    }
                }

                Switch(
                    checked = status.isConnected,
                    onCheckedChange = onToggleConnection,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HeartRateRed,
                        checkedTrackColor = Color(0xFF42151A),
                        uncheckedTrackColor = Color(0xFF22263C)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1D2138), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Classification: ${status.dipClassification}",
                    fontSize = 11.sp,
                    color = SunriseBrightSun,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141728))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 10.sp, color = Color(0xFFA5ABC7))
            Text(text = subtitle, fontSize = 9.sp, color = color)
        }
    }
}

@Composable
private fun HygieneTipItem(icon: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SunriseGold)
            Text(text = desc, fontSize = 11.sp, color = Color(0xFFA0A5C2), lineHeight = 15.sp)
        }
    }
}

@Composable
private fun NightSessionCard(session: SleepSessionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141727))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = session.dateString,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${session.formattedDuration()} • ${session.cyclesCount} cycles",
                    fontSize = 11.sp,
                    color = Color(0xFFA0A5C2)
                )
                Text(
                    text = "Task: ${session.challengeCompleted}",
                    fontSize = 10.sp,
                    color = SunriseGold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF261938), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Score ${session.sleepScore}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleepRemLavender
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "HR Dip -${session.hrDipPercent}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
        }
    }
}
