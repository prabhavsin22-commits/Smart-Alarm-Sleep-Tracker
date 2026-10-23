package com.example.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuraRiseDatabase
import com.example.data.MorningQuotes
import com.example.data.model.ChallengeType
import com.example.data.model.SleepSessionEntity
import com.example.ui.components.BottomControlDock
import com.example.ui.components.HeartRateSleepChart
import com.example.ui.components.SunriseCanvasView
import com.example.ui.screens.MathPuzzleChallengeView
import com.example.ui.screens.MemorySequenceChallengeView
import com.example.ui.screens.SkyCaptureChallengeView
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show over keyguard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alarmId = intent.getLongExtra(AlarmAudioService.EXTRA_ALARM_ID, 0L)
        val alarmLabel = intent.getStringExtra(AlarmAudioService.EXTRA_ALARM_LABEL) ?: "Alarm"
        val challengeTypeStr = intent.getStringExtra(AlarmAudioService.EXTRA_CHALLENGE_TYPE) ?: "NONE"
        val ambientThemeStr = intent.getStringExtra(AlarmAudioService.EXTRA_AMBIENT_THEME) ?: "SUNRISE_GOLD"

        val challengeType = try {
            ChallengeType.valueOf(challengeTypeStr)
        } catch (_: Exception) {
            ChallengeType.NONE
        }

        val ambientTheme = try {
            com.example.data.model.AmbientTheme.valueOf(ambientThemeStr)
        } catch (_: Exception) {
            com.example.data.model.AmbientTheme.SUNRISE_GOLD
        }

        setContent {
            MyApplicationTheme {
                AlarmRingingScreen(
                    alarmId = alarmId,
                    label = alarmLabel,
                    challengeType = challengeType,
                    ambientTheme = ambientTheme,
                    onDismissComplete = {
                        stopAlarmService()
                        finish()
                    },
                    onSnooze = {
                        val scheduler = AlarmScheduler(this)
                        scheduler.scheduleSnooze(alarmId, 9)
                        stopAlarmService()
                        finish()
                    }
                )
            }
        }
    }

    private fun stopAlarmService() {
        val stopIntent = Intent(this, AlarmAudioService::class.java).apply {
            action = AlarmAudioService.ACTION_STOP_ALARM
        }
        startService(stopIntent)
    }
}

enum class RingingStep {
    AMBIENT_SUNRISE_AWAKENING,
    ALERTNESS_CHALLENGE,
    MORNING_ANALYTICS_AND_QUOTE
}

@Composable
fun AlarmRingingScreen(
    alarmId: Long,
    label: String,
    challengeType: ChallengeType,
    ambientTheme: com.example.data.model.AmbientTheme = com.example.data.model.AmbientTheme.SUNRISE_GOLD,
    onDismissComplete: () -> Unit,
    onSnooze: () -> Unit
) {
    var step by remember { mutableStateOf(RingingStep.AMBIENT_SUNRISE_AWAKENING) }
    var sunriseProgress by remember { mutableFloatStateOf(0.15f) }
    var currentTimeString by remember { mutableStateOf("") }
    var solvedReactionTime by remember { mutableStateOf(14) }
    var solvedSummary by remember { mutableStateOf("Challenge Complete") }
    val quote = remember { MorningQuotes.getRandomQuote() }
    val scope = rememberCoroutineScope()

    // Clock and ambient ramp animation
    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        while (true) {
            currentTimeString = formatter.format(Date())
            if (sunriseProgress < 1.0f) {
                sunriseProgress = (sunriseProgress + 0.02f).coerceAtMost(1.0f)
            }
            delay(1000)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("alarm_ringing_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ambient Light Canvas Background (Supports Sunrise, Ocean, Twilight, Daylight, Neon)
            SunriseCanvasView(
                progress = sunriseProgress,
                ambientTheme = ambientTheme,
                modifier = Modifier.fillMaxSize()
            )

            when (step) {
                RingingStep.AMBIENT_SUNRISE_AWAKENING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(54.dp))

                        // Ambient light indicator pill
                        Row(
                            modifier = Modifier
                                .background(Color(0x55000000), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = ambientTheme.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${ambientTheme.displayName} Ambient Light • ${(sunriseProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SunriseBrightSun
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Large Clock Display
                        Text(
                            text = currentTimeString.ifEmpty { "06:30 AM" },
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SunriseGold
                        )

                        Text(
                            text = "Gentle Nature Soundscape • Optimal Light Sleep Window",
                            fontSize = 12.sp,
                            color = Color(0xFFD6DBEF),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Ergonomic Bottom Control Dock: Easy to hit when groggy!
                        BottomControlDock(
                            onSnooze = onSnooze,
                            onDismissOrStartTask = {
                                if (challengeType == ChallengeType.NONE) {
                                    step = RingingStep.MORNING_ANALYTICS_AND_QUOTE
                                } else {
                                    step = RingingStep.ALERTNESS_CHALLENGE
                                }
                            },
                            taskLabel = if (challengeType == ChallengeType.NONE) "Slide to Stop Alarm" else "Slide to Wake Task ➔",
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                RingingStep.ALERTNESS_CHALLENGE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (challengeType) {
                            ChallengeType.MATH_PUZZLE -> {
                                MathPuzzleChallengeView(
                                    onSolved = { seconds ->
                                        solvedReactionTime = seconds
                                        solvedSummary = "Math Problem ($seconds s)"
                                        step = RingingStep.MORNING_ANALYTICS_AND_QUOTE
                                    }
                                )
                            }
                            ChallengeType.SKY_CAPTURE -> {
                                SkyCaptureChallengeView(
                                    onSolved = { seconds ->
                                        solvedReactionTime = seconds
                                        solvedSummary = "Sky Photo (Verified)"
                                        step = RingingStep.MORNING_ANALYTICS_AND_QUOTE
                                    }
                                )
                            }
                            ChallengeType.MEMORY_SEQUENCE -> {
                                MemorySequenceChallengeView(
                                    onSolved = { seconds ->
                                        solvedReactionTime = seconds
                                        solvedSummary = "Memory Reflex ($seconds s)"
                                        step = RingingStep.MORNING_ANALYTICS_AND_QUOTE
                                    }
                                )
                            }
                            ChallengeType.NONE -> {
                                step = RingingStep.MORNING_ANALYTICS_AND_QUOTE
                            }
                        }
                    }
                }

                RingingStep.MORNING_ANALYTICS_AND_QUOTE -> {
                    // Post-Task Motivation & Morning Analytics View
                    MorningAnalyticsView(
                        quote = quote,
                        reactionTimeSeconds = solvedReactionTime,
                        challengeSummary = solvedSummary,
                        onComplete = onDismissComplete
                    )
                }
            }
        }
    }
}

@Composable
fun MorningAnalyticsView(
    quote: com.example.data.MorningQuote,
    reactionTimeSeconds: Int,
    challengeSummary: String,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF50A0B14))
            .padding(horizontal = 20.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // Success Badge
        Row(
            modifier = Modifier
                .background(Color(0xFF1B3024), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Awake",
                tint = SuccessGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Awakening Verified • Alertness High",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Motivational Quote Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("morning_quote_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1E32))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Quote",
                        tint = SunriseGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Daily Inspiration • ${quote.category}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AlertCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "“${quote.quote}”",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "— ${quote.author}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunriseGold,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Morning Sleep Hygiene Analytics Card
        Text(
            text = "Morning Sleep Hygiene & Recovery",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick KPI Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MorningStatCard(
                label = "Sleep Duration",
                value = "7h 35m",
                subtext = "5.1 sleep cycles",
                modifier = Modifier.weight(1f)
            )
            MorningStatCard(
                label = "Wake Reaction",
                value = "${reactionTimeSeconds}s",
                subtext = "Fast alertness",
                modifier = Modifier.weight(1f)
            )
            MorningStatCard(
                label = "Sleep Quality",
                value = "92%",
                subtext = "Optimal rest",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Heart Rate Nocturnal Dip Chart
        HeartRateSleepChart(
            heartRatePoints = listOf(66, 62, 57, 52, 49, 48, 51, 56, 62),
            avgHr = 54,
            minHr = 48,
            maxHr = 66,
            hrDipPercent = 15.6f,
            deepPercent = 23,
            remPercent = 24,
            lightPercent = 48,
            awakePercent = 5
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Productivity Tip of the Day
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141724))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Tip",
                    tint = SunriseWarmOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Circadian Productivity Tip",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Expose your eyes to 10 mins of natural sunlight within 1 hour of waking to set your circadian timer.",
                        fontSize = 11.sp,
                        color = Color(0xFFA5ABC7)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Dismiss Button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("start_day_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SunriseGold)
        ) {
            Text(
                text = "Dismiss Alarm • Continue Day",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1500)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MorningStatCard(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B2C))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color(0xFF8E93B2))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SunriseGold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 9.sp, color = AlertCyan)
        }
    }
}
