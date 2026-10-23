package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HeartRateRed
import com.example.ui.theme.SleepDeepPurple
import com.example.ui.theme.SleepLightBlue
import com.example.ui.theme.SleepRemLavender
import com.example.ui.theme.SuccessGreen

@Composable
fun HeartRateSleepChart(
    heartRatePoints: List<Int>,
    avgHr: Int,
    minHr: Int,
    maxHr: Int,
    hrDipPercent: Float,
    deepPercent: Int,
    remPercent: Int,
    lightPercent: Int,
    awakePercent: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("heart_rate_sleep_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141726))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF381418), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart Rate",
                            tint = HeartRateRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Night Resting Heart Rate",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Synced with Health Connect",
                            fontSize = 11.sp,
                            color = Color(0xFFA0A5C2)
                        )
                    }
                }

                // Dip badge
                Row(
                    modifier = Modifier
                        .background(Color(0xFF1B3028), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = "Dip",
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "-${hrDipPercent}% Dip",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(title = "Resting Avg", value = "$avgHr bpm", color = HeartRateRed)
                StatPill(title = "Lowest Dip", value = "$minHr bpm", color = SuccessGreen)
                StatPill(title = "Peak Morning", value = "$maxHr bpm", color = Color(0xFFFFB347))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Heart Rate Curve
            val validPoints = if (heartRatePoints.isNotEmpty()) heartRatePoints else listOf(65, 61, 56, 52, 49, 48, 51, 57, 63)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val minVal = 40f
                    val maxVal = 75f
                    val range = maxVal - minVal

                    // Draw optimal dip zone background
                    val optimalTopY = h - ((60f - minVal) / range) * h
                    val optimalBottomY = h - ((46f - minVal) / range) * h
                    drawRect(
                        color = Color(0x1A69F0AE),
                        topLeft = Offset(0f, optimalTopY),
                        size = androidx.compose.ui.geometry.Size(w, optimalBottomY - optimalTopY)
                    )

                    // Line grid
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(0f, h * 0.5f),
                        end = Offset(w, h * 0.5f),
                        strokeWidth = 1f
                    )

                    // Plot HR path
                    val path = Path()
                    val fillPath = Path()
                    val stepX = w / (validPoints.size - 1).coerceAtLeast(1)

                    validPoints.forEachIndexed { index, bpm ->
                        val x = index * stepX
                        val normalized = (bpm.toFloat().coerceIn(minVal, maxVal) - minVal) / range
                        val y = h - (normalized * h)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevBpm = validPoints[index - 1]
                            val prevY = h - (((prevBpm.toFloat().coerceIn(minVal, maxVal) - minVal) / range) * h)
                            val midX = (prevX + x) / 2f
                            path.cubicTo(midX, prevY, midX, y, x, y)
                            fillPath.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Draw gradient under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(HeartRateRed.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw HR line
                    drawPath(
                        path = path,
                        color = HeartRateRed,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw point dots
                    validPoints.forEachIndexed { index, bpm ->
                        val x = index * stepX
                        val normalized = (bpm.toFloat().coerceIn(minVal, maxVal) - minVal) / range
                        val y = h - (normalized * h)
                        drawCircle(
                            color = if (bpm == minHr) SuccessGreen else HeartRateRed,
                            radius = if (bpm == minHr) 5.dp.toPx() else 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sleep Stages Ratio Bar
            Text(
                text = "Sleep Stage Proportions (Cycles)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA5ABC7)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(deepPercent.toFloat().coerceAtLeast(1f))
                        .height(10.dp)
                        .background(SleepDeepPurple, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(remPercent.toFloat().coerceAtLeast(1f))
                        .height(10.dp)
                        .background(SleepRemLavender)
                )
                Box(
                    modifier = Modifier
                        .weight(lightPercent.toFloat().coerceAtLeast(1f))
                        .height(10.dp)
                        .background(SleepLightBlue)
                )
                Box(
                    modifier = Modifier
                        .weight(awakePercent.toFloat().coerceAtLeast(1f))
                        .height(10.dp)
                        .background(Color(0xFFFF8A80), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StageLegend(label = "Deep $deepPercent%", color = SleepDeepPurple)
                StageLegend(label = "REM $remPercent%", color = SleepRemLavender)
                StageLegend(label = "Light $lightPercent%", color = SleepLightBlue)
                StageLegend(label = "Awake $awakePercent%", color = Color(0xFFFF8A80))
            }
        }
    }
}

@Composable
private fun StatPill(title: String, value: String, color: Color) {
    Column(
        modifier = Modifier
            .background(Color(0xFF1E2235), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = title, fontSize = 10.sp, color = Color(0xFF8F94B2))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun StageLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color(0xFFB0B5D0))
    }
}
