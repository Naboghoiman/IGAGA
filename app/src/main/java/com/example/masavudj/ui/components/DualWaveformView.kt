package com.example.masavudj.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.masavudj.ui.theme.DjPanelBg

@Composable
fun DualWaveformView(
    deckA: DeckState,
    deckB: DeckState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B0D12))
            .border(1.dp, Color(0xFF232A38), RoundedCornerShape(8.dp))
            .testTag("dual_waveform_view")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Deck A Waveform
            WaveformRow(
                deckState = deckA,
                accentColor = DeckACyan,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Center Separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF1E2430))
            )

            // Deck B Waveform
            WaveformRow(
                deckState = deckB,
                accentColor = DeckBAmber,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        // Center Playhead Needle
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            drawLine(
                color = DjCrimson,
                start = Offset(centerX, 0f),
                end = Offset(centerX, size.height),
                strokeWidth = 2.dp.toPx()
            )
            // Center Marker diamond/arrows
            drawCircle(
                color = DjCrimson,
                radius = 3.dp.toPx(),
                center = Offset(centerX, size.height / 2f)
            )
        }
    }
}

@Composable
private fun WaveformRow(
    deckState: DeckState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f
            val centerX = w / 2f

            // Beatgrid parameters
            val bpm = deckState.effectiveBpm.coerceAtLeast(60f)
            val secPerBeat = 60f / bpm
            val pixelsPerSecond = 120.dp.toPx()
            val pixelsPerBeat = secPerBeat * pixelsPerSecond

            // Current offset from playhead
            val currentPos = deckState.currentPositionSeconds
            val centerOffsetPixels = currentPos * pixelsPerSecond

            // Draw Background Grid Lines
            val firstVisibleBeatIndex = ((centerOffsetPixels - centerX) / pixelsPerBeat).toInt() - 2
            val lastVisibleBeatIndex = ((centerOffsetPixels + centerX) / pixelsPerBeat).toInt() + 2

            for (b in firstVisibleBeatIndex..lastVisibleBeatIndex) {
                val beatX = centerX + (b * pixelsPerBeat - centerOffsetPixels)
                val isDownbeat = (b % 4 == 0)

                drawLine(
                    color = if (isDownbeat) DjCrimson.copy(alpha = 0.7f) else Color(0x33566580),
                    start = Offset(beatX, if (isDownbeat) 0f else h * 0.15f),
                    end = Offset(beatX, if (isDownbeat) h else h * 0.85f),
                    strokeWidth = if (isDownbeat) 1.5.dp.toPx() else 1.dp.toPx()
                )
            }

            // Draw Simulated Audio Waveform Peaks
            val numBars = 70
            val barWidth = 3.dp.toPx()
            val spacing = w / numBars

            val peaks = deckState.track?.waveformPeaks ?: FloatArray(100) { 0.4f }

            for (i in 0 until numBars) {
                val x = i * spacing
                // Sample peak based on scrolling position
                val peakIndex = ((currentPos * 10f + i) % peaks.size).toInt()
                val peakVal = peaks[peakIndex].coerceIn(0.1f, 0.95f)

                val barHeight = peakVal * (h * 0.8f)

                drawRoundRect(
                    color = accentColor.copy(alpha = 0.85f),
                    topLeft = Offset(x, centerY - barHeight / 2f),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        // Deck Identifier Label
        Text(
            text = "${deckState.deckId} • ${deckState.effectiveBpm.toInt()} BPM",
            color = accentColor,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 6.dp, top = 2.dp)
        )
    }
}
