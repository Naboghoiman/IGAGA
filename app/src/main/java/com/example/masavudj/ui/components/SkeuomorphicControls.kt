package com.example.masavudj.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masavudj.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RotaryKnob(
    label: String,
    value: Float, // -1f..+1f (bipolar) or 0f..1f (unipolar)
    isBipolar: Boolean = true,
    accentColor: Color = DeckACyan,
    size: Dp = 48.dp,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragAccumulator by remember { mutableFloatStateOf(value) }

    LaunchedEffect(value) {
        dragAccumulator = value
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("knob_$label")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .pointerInput(label) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val sensitivity = 0.015f
                        val delta = -dragAmount.y * sensitivity
                        val minVal = if (isBipolar) -1f else 0f
                        dragAccumulator = (dragAccumulator + delta).coerceIn(minVal, 1f)
                        onValueChange(dragAccumulator)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f - 4.dp.toPx()

                val startAngle = 135f
                val sweepAngle = 270f

                // Track Background
                drawArc(
                    color = Color(0xFF1E232E),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active Arc
                if (isBipolar) {
                    val centerAngle = 270f
                    val arcSweep = value * 135f
                    drawArc(
                        color = accentColor,
                        startAngle = centerAngle,
                        sweepAngle = arcSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    val arcSweep = value * sweepAngle
                    drawArc(
                        color = accentColor,
                        startAngle = startAngle,
                        sweepAngle = arcSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Center Metal Knob Cap
                val knobRadius = radius * 0.72f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF384254), Color(0xFF181C26)),
                        center = center,
                        radius = knobRadius
                    ),
                    radius = knobRadius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF4A5568),
                    radius = knobRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Indicator Line
                val currentAngle = if (isBipolar) {
                    270f + value * 135f
                } else {
                    135f + value * sweepAngle
                }
                val rad = Math.toRadians(currentAngle.toDouble())
                val indicatorStart = Offset(
                    center.x + (cos(rad) * knobRadius * 0.35f).toFloat(),
                    center.y + (sin(rad) * knobRadius * 0.35f).toFloat()
                )
                val indicatorEnd = Offset(
                    center.x + (cos(rad) * knobRadius * 0.9f).toFloat(),
                    center.y + (sin(rad) * knobRadius * 0.9f).toFloat()
                )
                drawLine(
                    color = Color.White,
                    start = indicatorStart,
                    end = indicatorEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun MetallicVerticalFader(
    label: String,
    value: Float, // 0f..1f
    accentColor: Color = DeckACyan,
    height: Dp = 120.dp,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("fader_$label")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(36.dp)
                .height(height)
                .pointerInput(label) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount.y / (size.height.toFloat())
                        onValueChange((value + delta).coerceIn(0f, 1f))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val h = size.height

                // Metal Track Slot
                drawRoundRect(
                    color = Color(0xFF0F1218),
                    topLeft = Offset(centerX - 3.dp.toPx(), 8.dp.toPx()),
                    size = Size(6.dp.toPx(), h - 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )

                // Tick lines
                val numTicks = 7
                for (t in 0 until numTicks) {
                    val y = 10.dp.toPx() + t * ((h - 20.dp.toPx()) / (numTicks - 1))
                    drawLine(
                        color = Color(0xFF333E52),
                        start = Offset(centerX - 10.dp.toPx(), y),
                        end = Offset(centerX - 4.dp.toPx(), y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF333E52),
                        start = Offset(centerX + 4.dp.toPx(), y),
                        end = Offset(centerX + 10.dp.toPx(), y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Fader Cap
                val faderY = 10.dp.toPx() + (1f - value) * (h - 24.dp.toPx())
                val capWidth = 28.dp.toPx()
                val capHeight = 16.dp.toPx()

                // Cap Body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B), Color(0xFF1E293B)),
                        startY = faderY,
                        endY = faderY + capHeight
                    ),
                    topLeft = Offset(centerX - capWidth / 2f, faderY),
                    size = Size(capWidth, capHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )

                // Cap Center Stripe
                drawLine(
                    color = accentColor,
                    start = Offset(centerX - capWidth / 2f + 2.dp.toPx(), faderY + capHeight / 2f),
                    end = Offset(centerX + capWidth / 2f - 2.dp.toPx(), faderY + capHeight / 2f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun SmoothCrossfader(
    value: Float, // -1f (A) .. 0 (Center) .. +1f (B)
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("crossfader")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("A", color = DeckACyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("CROSSFADER", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
            Text("B", color = DeckBAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = dragAmount.x / (size.width.toFloat() / 2f)
                        onValueChange((value + delta).coerceIn(-1f, 1f))
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val centerY = size.height / 2f

                // Track Slot
                drawRoundRect(
                    color = Color(0xFF0B0D12),
                    topLeft = Offset(12.dp.toPx(), centerY - 4.dp.toPx()),
                    size = Size(w - 24.dp.toPx(), 8.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )

                // Center Notch Line
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(w / 2f, centerY - 8.dp.toPx()),
                    end = Offset(w / 2f, centerY + 8.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Crossfader Cap
                val normalizedX = (value + 1f) / 2f
                val capX = 14.dp.toPx() + normalizedX * (w - 48.dp.toPx())
                val capWidth = 20.dp.toPx()
                val capHeight = 28.dp.toPx()

                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF1E293B)),
                        startX = capX,
                        endX = capX + capWidth
                    ),
                    topLeft = Offset(capX, centerY - capHeight / 2f),
                    size = Size(capWidth, capHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )

                // Center indicator line
                drawLine(
                    color = DjCrimson,
                    start = Offset(capX + capWidth / 2f, centerY - capHeight / 2f + 2.dp.toPx()),
                    end = Offset(capX + capWidth / 2f, centerY + capHeight / 2f - 2.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun VuMeterBar(
    level: Float, // 0f..1f
    modifier: Modifier = Modifier
) {
    val totalSegments = 10
    val activeSegments = (level * totalSegments).toInt().coerceIn(0, totalSegments)

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom),
        modifier = modifier
            .width(6.dp)
            .height(70.dp)
            .background(Color(0xFF0F1218), RoundedCornerShape(2.dp))
            .padding(1.dp)
    ) {
        for (i in (totalSegments - 1) downTo 0) {
            val isActive = i < activeSegments
            val segColor = when {
                i >= 8 -> LedRed
                i >= 6 -> LedAmber
                else -> LedGreen
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isActive) segColor else Color(0xFF1B202B))
            )
        }
    }
}
