package com.example.masavudj.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masavudj.model.DeckState
import com.example.masavudj.model.SamplerPadItem
import com.example.masavudj.ui.theme.DeckACyan
import com.example.masavudj.ui.theme.DjCrimson
import com.example.masavudj.ui.theme.DjPanelBg

enum class PadMode {
    HOT_CUE,
    SAMPLER,
    BEAT_LOOP
}

@Composable
fun PerformancePads(
    deckId: Char,
    deckState: DeckState,
    samplerPads: List<SamplerPadItem>,
    accentColor: Color,
    onTriggerHotCue: (Int) -> Unit,
    onTriggerSampler: (Int) -> Unit,
    onSetLoop: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMode by remember { mutableStateOf(PadMode.HOT_CUE) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF131720))
            .border(1.dp, Color(0xFF222938), RoundedCornerShape(8.dp))
            .padding(6.dp)
            .testTag("performance_pads_$deckId")
    ) {
        // Mode Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PadMode.values().forEach { mode ->
                val isSelected = currentMode == mode
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.25f) else Color(0xFF1A202C))
                        .border(1.dp, if (isSelected) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { currentMode = mode }
                ) {
                    Text(
                        text = mode.name.replace('_', ' '),
                        color = if (isSelected) accentColor else Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 8 Pads Grid (2 rows x 4 cols)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0..3) {
                        val index = row * 4 + col
                        PadButton(
                            index = index,
                            mode = currentMode,
                            deckState = deckState,
                            samplerPads = samplerPads,
                            accentColor = accentColor,
                            onTriggerHotCue = onTriggerHotCue,
                            onTriggerSampler = onTriggerSampler,
                            onSetLoop = onSetLoop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PadButton(
    index: Int,
    mode: PadMode,
    deckState: DeckState,
    samplerPads: List<SamplerPadItem>,
    accentColor: Color,
    onTriggerHotCue: (Int) -> Unit,
    onTriggerSampler: (Int) -> Unit,
    onSetLoop: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val loopBeatsList = listOf(1, 2, 4, 8, 16, 32, 64, 128)

    val (label, sublabel, padColor, isActive) = when (mode) {
        PadMode.HOT_CUE -> {
            val hasCue = deckState.hotCues.containsKey(index)
            Quadruple("CUE ${index + 1}", if (hasCue) "SET" else "EMPTY", accentColor, hasCue)
        }
        PadMode.SAMPLER -> {
            val s = samplerPads.getOrNull(index)
            val c = s?.colorHex?.let { Color(it) } ?: accentColor
            Quadruple(s?.name ?: "PAD", s?.soundType ?: "SMP", c, false)
        }
        PadMode.BEAT_LOOP -> {
            val beats = loopBeatsList.getOrElse(index) { 4 }
            val active = deckState.activeLoopBeats == beats
            Quadruple("${beats}B", "LOOP", DjCrimson, active)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) padColor.copy(alpha = 0.4f) else Color(0xFF1E2432))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) padColor else Color(0xFF2E3748),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable {
                when (mode) {
                    PadMode.HOT_CUE -> onTriggerHotCue(index)
                    PadMode.SAMPLER -> onTriggerSampler(index)
                    PadMode.BEAT_LOOP -> {
                        val beats = loopBeatsList.getOrElse(index) { 4 }
                        if (deckState.activeLoopBeats == beats) {
                            onSetLoop(null)
                        } else {
                            onSetLoop(beats)
                        }
                    }
                }
            }
            .testTag("pad_${index}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isActive) Color.White else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = sublabel,
                color = padColor.copy(alpha = 0.85f),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
