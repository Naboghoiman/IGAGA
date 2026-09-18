package com.example.masavudj.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masavudj.model.DeckState
import com.example.masavudj.ui.theme.DeckACyan
import com.example.masavudj.ui.theme.DeckBAmber
import com.example.masavudj.ui.theme.DjCrimson
import com.example.masavudj.ui.theme.DjGreen
import kotlin.math.abs

@Composable
fun PhaseVisualizer(
    deckA: DeckState,
    deckB: DeckState,
    phaseErrorMs: Float,
    masterDeckId: Char,
    modifier: Modifier = Modifier
) {
    val isPhaseMatched = abs(phaseErrorMs) < 8f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0D1016))
            .border(1.dp, Color(0xFF222836), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp)
            .testTag("phase_visualizer")
    ) {
        // Deck A Beat Indicators (1 2 3 4)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text("A", color = DeckACyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            for (b in 0..3) {
                val isCurrent = deckA.isPlaying && deckA.currentBeatInBar == b
                Box(
                    modifier = Modifier
                        .size(if (b == 0) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent && b == 0 -> DjCrimson
                                isCurrent -> DeckACyan
                                b == 0 -> Color(0xFF4A2530)
                                else -> Color(0xFF1E2533)
                            }
                        )
                )
            }
        }

        // Center Phase Deviation Meter
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(130.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF151922))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val h = size.height

                // Center zero line
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, h),
                    strokeWidth = 1.dp.toPx()
                )

                // Error indicator needle (-50ms .. +50ms mapped to width)
                val clampedMs = phaseErrorMs.coerceIn(-50f, 50f)
                val offsetPx = (clampedMs / 50f) * (centerX - 4.dp.toPx())
                val needleColor = if (isPhaseMatched) DjGreen else if (abs(phaseErrorMs) < 20f) Color(0xFFFFAB00) else DjCrimson

                drawCircle(
                    color = needleColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX + offsetPx, h / 2f)
                )
            }
        }

        // Deck B Beat Indicators (1 2 3 4)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (b in 0..3) {
                val isCurrent = deckB.isPlaying && deckB.currentBeatInBar == b
                Box(
                    modifier = Modifier
                        .size(if (b == 0) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent && b == 0 -> DjCrimson
                                isCurrent -> DeckBAmber
                                b == 0 -> Color(0xFF4A3520)
                                else -> Color(0xFF1E2533)
                            }
                        )
                )
            }
            Text("B", color = DeckBAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
