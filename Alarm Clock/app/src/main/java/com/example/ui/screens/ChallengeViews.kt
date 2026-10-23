package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertCyan
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SunriseBrightSun
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// -------------------------------------------------------------
// 1. ANALYTICAL MATH PUZZLE CHALLENGE
// -------------------------------------------------------------
@Composable
fun MathPuzzleChallengeView(
    onSolved: (secondsTaken: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var num1 by remember { mutableIntStateOf(Random.nextInt(12, 28)) }
    var num2 by remember { mutableIntStateOf(Random.nextInt(3, 9)) }
    var num3 by remember { mutableIntStateOf(Random.nextInt(11, 35)) }
    var inputAnswer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }

    val correctAnswer = (num1 * num2) + num3

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("math_challenge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161828))
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
                    text = "🧠 Wake Alertness Challenge",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunriseGold
                )
                Text(
                    text = "${secondsElapsed}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlertCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Solve to prove mental alertness and stop alarm:",
                fontSize = 12.sp,
                color = Color(0xFFA5ABC7)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Problem Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF20243C), RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "($num1 × $num2) + $num3 = ?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User input display
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .border(
                        width = 2.dp,
                        color = if (isError) Color(0xFFFF5252) else SunriseGold,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color(0xFF10121C), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (inputAnswer.isEmpty()) "Tap answer below" else inputAnswer,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (inputAnswer.isEmpty()) Color(0xFF6E7494) else Color.White
                )
            }

            if (isError) {
                Text(
                    text = "Incorrect, try again to wake your brain!",
                    color = Color(0xFFFF5252),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Numeric Numpad (0-9, Backspace, Submit)
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "OK")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (key) {
                                        "OK" -> SunriseGold
                                        "C" -> Color(0xFF2C2230)
                                        else -> Color(0xFF22263C)
                                    }
                                )
                                .clickable {
                                    when (key) {
                                        "C" -> {
                                            inputAnswer = ""
                                            isError = false
                                        }
                                        "OK" -> {
                                            if (inputAnswer.toIntOrNull() == correctAnswer) {
                                                onSolved(secondsElapsed)
                                            } else {
                                                isError = true
                                            }
                                        }
                                        else -> {
                                            if (inputAnswer.length < 6) {
                                                inputAnswer += key
                                                isError = false
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (key == "OK") Color(0xFF151000) else Color.White
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 2. CLICK PICTURE OF THE SKY CHALLENGE
// -------------------------------------------------------------
@Composable
fun SkyCaptureChallengeView(
    onSolved: (secondsTaken: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var daylightScore by remember { mutableIntStateOf(0) }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    // Camera launcher contract
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            isVerifying = true
            scope.launch {
                delay(800)
                // Analyze brightness / daylight spectrum
                var totalBrightness = 0L
                var blueWarmth = 0L
                val sampleStep = 10
                var samples = 0
                for (x in 0 until bitmap.width step sampleStep) {
                    for (y in 0 until bitmap.height step sampleStep) {
                        val pixel = bitmap.getPixel(x, y)
                        val r = AndroidColor.red(pixel)
                        val g = AndroidColor.green(pixel)
                        val b = AndroidColor.blue(pixel)
                        val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                        totalBrightness += lum
                        blueWarmth += (b + g) / 2
                        samples++
                    }
                }

                val avgLum = if (samples > 0) (totalBrightness / samples).toInt() else 120
                // Calculate score 0-100%
                val score = ((avgLum / 220f) * 100f).toInt().coerceIn(35, 98)
                daylightScore = score
                isVerifying = false

                if (score >= 45) {
                    verificationResult = "Light Verified! ($score% Luminance)"
                    delay(1200)
                    onSolved(secondsElapsed)
                } else {
                    verificationResult = "Too dark! Point camera towards window, sky, or ambient room light."
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("sky_challenge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161828))
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
                    text = "🌤️ Light & Sky Scan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunriseGold
                )
                Text(
                    text = "${secondsElapsed}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlertCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Get up & snap a picture of the sky, window, or ambient light:",
                fontSize = 12.sp,
                color = Color(0xFFA5ABC7)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo preview / target zone
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF20243C))
                    .border(2.dp, Color(0xFF3B4060), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Sky Capture Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Morning Sky",
                            tint = SunriseBrightSun,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Awaiting Sky Photo",
                            color = Color(0xFFB0B5D2),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isVerifying) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = SunriseGold
                )
                Text(
                    text = "Analyzing lux & sky color spectrum...",
                    fontSize = 11.sp,
                    color = SunriseGold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (verificationResult != null) {
                Text(
                    text = verificationResult!!,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (daylightScore >= 45) SuccessGreen else Color(0xFFFF5252),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Open Camera / Quick Sky Simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("snap_sky_camera_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SunriseGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = Color(0xFF1E1700)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Snap Sky",
                        color = Color(0xFF1E1700),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Fallback / Window daylight test button
                Button(
                    onClick = {
                        isVerifying = true
                        scope.launch {
                            delay(600)
                            daylightScore = 92
                            isVerifying = false
                            verificationResult = "Daylight Verified! (92% Luminance)"
                            delay(900)
                            onSolved(secondsElapsed)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("simulate_sky_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282D46))
                ) {
                    Text(
                        text = "Simulate Sky ☀️",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. MEMORY SEQUENCE CHALLENGE
// -------------------------------------------------------------
@Composable
fun MemorySequenceChallengeView(
    onSolved: (secondsTaken: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sequence = remember { listOf(0, 2, 1, 3, 2) }
    var userIndex by remember { mutableIntStateOf(0) }
    var activeFlashIndex by remember { mutableIntStateOf(-1) }
    var isShowingSequence by remember { mutableStateOf(true) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    LaunchedEffect(Unit) {
        delay(600)
        for (step in sequence) {
            activeFlashIndex = step
            delay(500)
            activeFlashIndex = -1
            delay(250)
        }
        isShowingSequence = false
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("memory_challenge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161828))
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
                    text = "🧩 Memory Reflex Challenge",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SunriseGold
                )
                Text(
                    text = "${secondsElapsed}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlertCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isShowingSequence) "Watch the light pattern sequence..." else "Repeat the 5-step pattern (${userIndex}/5):",
                fontSize = 12.sp,
                color = if (isShowingSequence) SunriseBrightSun else Color(0xFFA5ABC7)
            )

            Spacer(modifier = Modifier.height(18.dp))

            val pads = listOf(
                Pair("Amber", SunriseGold),
                Pair("Cyan", AlertCyan),
                Pair("Lavender", SleepRemLavender),
                Pair("Sun Orange", SunriseWarmOrange)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MemoryPad(
                        index = 0,
                        color = pads[0].second,
                        isFlashing = activeFlashIndex == 0,
                        onClick = {
                            if (!isShowingSequence) {
                                if (sequence[userIndex] == 0) {
                                    userIndex++
                                    if (userIndex >= sequence.size) onSolved(secondsElapsed)
                                } else {
                                    userIndex = 0
                                }
                            }
                        }
                    )
                    MemoryPad(
                        index = 1,
                        color = pads[1].second,
                        isFlashing = activeFlashIndex == 1,
                        onClick = {
                            if (!isShowingSequence) {
                                if (sequence[userIndex] == 1) {
                                    userIndex++
                                    if (userIndex >= sequence.size) onSolved(secondsElapsed)
                                } else {
                                    userIndex = 0
                                }
                            }
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MemoryPad(
                        index = 2,
                        color = pads[2].second,
                        isFlashing = activeFlashIndex == 2,
                        onClick = {
                            if (!isShowingSequence) {
                                if (sequence[userIndex] == 2) {
                                    userIndex++
                                    if (userIndex >= sequence.size) onSolved(secondsElapsed)
                                } else {
                                    userIndex = 0
                                }
                            }
                        }
                    )
                    MemoryPad(
                        index = 3,
                        color = pads[3].second,
                        isFlashing = activeFlashIndex == 3,
                        onClick = {
                            if (!isShowingSequence) {
                                if (sequence[userIndex] == 3) {
                                    userIndex++
                                    if (userIndex >= sequence.size) onSolved(secondsElapsed)
                                } else {
                                    userIndex = 0
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryPad(
    index: Int,
    color: Color,
    isFlashing: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isFlashing) Color.White else color.copy(alpha = 0.35f))
            .border(2.dp, if (isFlashing) Color.White else color, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    )
}
