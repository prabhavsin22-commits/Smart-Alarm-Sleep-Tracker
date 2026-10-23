package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SunriseGold
import com.example.ui.theme.SunriseWarmOrange
import kotlin.math.roundToInt

@Composable
fun BottomControlDock(
    onSnooze: () -> Unit,
    onDismissOrStartTask: () -> Unit,
    taskLabel: String = "Slide to Solve Task",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_control_dock"),
        color = Color(0xF010121C),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag-to-dismiss slider for easy morning reach and grogginess prevention
            SlideToDismissBar(
                text = taskLabel,
                onComplete = onDismissOrStartTask,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Large, easy-to-hit Snooze button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("bottom_snooze_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF1E2235),
                        contentColor = SunriseGold
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF3E4566), Color(0xFF2B3047))
                        )
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze Alarm",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Snooze (9 min)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun SlideToDismissBar(
    text: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val maxDrag = 580f
    val animatedOffset by animateFloatAsState(targetValue = dragOffset, label = "sliderOffset")

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF22263A))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset = (dragOffset + dragAmount.x).coerceIn(0f, maxDrag)
                    },
                    onDragEnd = {
                        if (dragOffset >= maxDrag * 0.75f) {
                            dragOffset = maxDrag
                            onComplete()
                        } else {
                            dragOffset = 0f
                        }
                    },
                    onDragCancel = {
                        dragOffset = 0f
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Background track gradient filling as you slide
        val fillFraction = (animatedOffset / maxDrag).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = fillFraction.coerceAtLeast(0.08f))
                .height(64.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(SunriseWarmOrange, SunriseGold)
                    )
                )
        )

        // Prompt text
        Text(
            text = text,
            color = if (fillFraction > 0.4f) Color(0xFF1A1A1A) else Color(0xFFE2E4F0),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .testTag("slide_task_label")
        )

        // Draggable Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .size(60.dp)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (fillFraction > 0.75f) Icons.Default.AlarmOff else Icons.Default.ChevronRight,
                contentDescription = "Swipe Handle",
                tint = Color(0xFFE65100),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
