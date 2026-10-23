package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.AmbientTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunriseCanvasView(
    progress: Float, // 0.0f (deep darkness) -> 1.0f (full radiant ambient illumination)
    ambientTheme: AmbientTheme = AmbientTheme.SUNRISE_GOLD,
    modifier: Modifier = Modifier
) {
    val clampedProgress = progress.coerceIn(0.05f, 1.0f)

    val infiniteTransition = rememberInfiniteTransition(label = "ambientPulse")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rayRotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Palette configuration by Ambient Theme
        val (baseTopColor, glowColor, centerColor) = when (ambientTheme) {
            AmbientTheme.SUNRISE_GOLD -> {
                Triple(
                    if (clampedProgress < 0.4f) Color(0xFF090A12) else Color(0xFF2E1724),
                    when {
                        clampedProgress < 0.35f -> Color(0xFF4A1900)
                        clampedProgress < 0.7f -> Color(0xFFB33D00)
                        else -> Color(0xFFFF8F00)
                    },
                    if (clampedProgress < 0.5f) Color(0xFFFF6D00) else Color(0xFFFFE082)
                )
            }
            AmbientTheme.OCEAN_AURORA -> {
                Triple(
                    if (clampedProgress < 0.4f) Color(0xFF060D17) else Color(0xFF0A2234),
                    when {
                        clampedProgress < 0.35f -> Color(0xFF00363A)
                        clampedProgress < 0.7f -> Color(0xFF00695C)
                        else -> Color(0xFF00897B)
                    },
                    if (clampedProgress < 0.5f) Color(0xFF00B4D8) else Color(0xFF80DEEA)
                )
            }
            AmbientTheme.SUNSET_TWILIGHT -> {
                Triple(
                    if (clampedProgress < 0.4f) Color(0xFF0D0B14) else Color(0xFF22112C),
                    when {
                        clampedProgress < 0.35f -> Color(0xFF381028)
                        clampedProgress < 0.7f -> Color(0xFF7B1FA2)
                        else -> Color(0xFFAB47BC)
                    },
                    if (clampedProgress < 0.5f) Color(0xFFE91E63) else Color(0xFFF48FB1)
                )
            }
            AmbientTheme.CRISP_DAYLIGHT -> {
                Triple(
                    if (clampedProgress < 0.4f) Color(0xFF10121A) else Color(0xFF1E2433),
                    when {
                        clampedProgress < 0.35f -> Color(0xFF2C3247)
                        clampedProgress < 0.7f -> Color(0xFF4A5568)
                        else -> Color(0xFF718096)
                    },
                    if (clampedProgress < 0.5f) Color(0xFFE2E8F0) else Color(0xFFFFFFFF)
                )
            }
            AmbientTheme.NEON_PULSE -> {
                Triple(
                    if (clampedProgress < 0.4f) Color(0xFF0F0814) else Color(0xFF2B0A28),
                    when {
                        clampedProgress < 0.35f -> Color(0xFF5A0835)
                        clampedProgress < 0.7f -> Color(0xFFD81B60)
                        else -> Color(0xFFFF4081)
                    },
                    if (clampedProgress < 0.5f) Color(0xFFFF007F) else Color(0xFFFF80BF)
                )
            }
            AmbientTheme.AUDIO_ONLY -> {
                Triple(
                    Color(0xFF0A0C13),
                    Color(0xFF161A26),
                    Color(0xFF242A3D)
                )
            }
        }

        // Background Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(baseTopColor, glowColor.copy(alpha = 0.45f), glowColor.copy(alpha = 0.88f)),
                startY = 0f,
                endY = height
            )
        )

        if (ambientTheme != AmbientTheme.AUDIO_ONLY) {
            // Ambient Light Horizon Center
            val lightCenter = Offset(width / 2f, height * (1.15f - clampedProgress * 0.35f))
            val maxRadius = (width * 0.95f) * (0.5f + clampedProgress * 0.6f) * pulseScale

            // Radial Ambient Light Bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        centerColor.copy(alpha = 0.9f * clampedProgress),
                        glowColor.copy(alpha = 0.65f * clampedProgress),
                        glowColor.copy(alpha = 0.25f * clampedProgress),
                        Color.Transparent
                    ),
                    center = lightCenter,
                    radius = maxRadius
                ),
                center = lightCenter,
                radius = maxRadius
            )

            // Warm Ambient Light Rays
            val rayCount = 12
            val rayLength = width * 1.2f * clampedProgress
            for (i in 0 until rayCount) {
                val angleDeg = (i * (360f / rayCount)) + rayRotation
                val angleRad = angleDeg * (PI / 180.0)
                val rayEnd = Offset(
                    x = (lightCenter.x + cos(angleRad) * rayLength).toFloat(),
                    y = (lightCenter.y - sin(angleRad).coerceAtLeast(0.0) * rayLength).toFloat()
                )

                drawLine(
                    color = centerColor.copy(alpha = 0.12f * clampedProgress),
                    start = lightCenter,
                    end = rayEnd,
                    strokeWidth = 14f * clampedProgress
                )
            }
        }
    }
}
