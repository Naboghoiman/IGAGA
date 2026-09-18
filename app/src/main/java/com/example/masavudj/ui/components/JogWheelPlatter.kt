package com.example.masavudj.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JogWheelPlatter(
    deckId: Char,
    isPlaying: Boolean,
    currentPositionSeconds: Float,
    effectiveBpm: Float,
    accentColor: Color,
    onScratch: (deltaAngle: Float) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var lastTouchAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Automatic rotation when playing
    LaunchedEffect(isPlaying, currentPositionSeconds, effectiveBpm) {
        if (!isDragging && isPlaying) {
            // 33.3 RPM = ~200 deg/sec
            rotationAngle = (currentPositionSeconds * (effectiveBpm / 60f) * 90f) % 360f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("jog_wheel_$deckId")
            .pointerInput(deckId) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val center = Offset(size.width / 2f, size.height / 2f)
                        lastTouchAngle = Math.toDegrees(
                            atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())
                        ).toFloat()
                    },
                    onDragEnd = {
                        isDragging = false
                        onRelease()
                    },
                    onDragCancel = {
                        isDragging = false
                        onRelease()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val currentAngle = Math.toDegrees(
                            atan2((change.position.y - center.y).toDouble(), (change.position.x - center.x).toDouble())
                        ).toFloat()
                        var delta = currentAngle - lastTouchAngle
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        rotationAngle = (rotationAngle + delta) % 360f
                        lastTouchAngle = currentAngle
                        onScratch(delta)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 6.dp.toPx()

            // Outer Chassis Rim
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2C3240), Color(0xFF141820)),
                    center = center,
                    radius = radius + 4.dp.toPx()
                ),
                radius = radius + 4.dp.toPx(),
                center = center
            )

            // Outer Strobe Dots
            val numDots = 48
            for (i in 0 until numDots) {
                val dotAngle = (i * (360f / numDots) + rotationAngle) * (Math.PI / 180f).toFloat()
                val dotR = radius - 6.dp.toPx()
                val dotPos = Offset(center.x + cos(dotAngle) * dotR, center.y + sin(dotAngle) * dotR)
                drawCircle(
                    color = if (i % 4 == 0) accentColor else Color(0xFF7A869E),
                    radius = 1.5.dp.toPx(),
                    center = dotPos
                )
            }

            // Vinyl Record Surface
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1B1F27), Color(0xFF0B0D12), Color(0xFF181C24)),
                    center = center,
                    radius = radius - 12.dp.toPx()
                ),
                radius = radius - 12.dp.toPx(),
                center = center
            )

            // Vinyl Micro-grooves
            for (g in 1..4) {
                val grooveR = (radius - 16.dp.toPx()) * (0.35f + g * 0.15f)
                drawCircle(
                    color = Color(0x333F4A5C),
                    radius = grooveR,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Center Platter Ring
            drawCircle(
                color = accentColor.copy(alpha = 0.8f),
                radius = radius * 0.38f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotating Center Label
            rotate(rotationAngle, center) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.4f), Color(0xFF0F131A)),
                        center = center,
                        radius = radius * 0.35f
                    ),
                    radius = radius * 0.35f,
                    center = center
                )

                // Marker line
                drawLine(
                    color = Color.White,
                    start = Offset(center.x, center.y - radius * 0.32f),
                    end = Offset(center.x, center.y - radius * 0.12f),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Center Spindle
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color(0xFF0A0C10),
                radius = 3.dp.toPx(),
                center = center
            )
        }
    }
}
