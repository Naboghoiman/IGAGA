package com.example.masavudj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masavudj.model.DeckState
import com.example.masavudj.ui.components.*
import com.example.masavudj.ui.theme.*
import com.example.masavudj.viewmodel.DjViewModel

@Composable
fun ConsoleScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val deckA by viewModel.deckA.collectAsState()
    val deckB by viewModel.deckB.collectAsState()
    val crossfader by viewModel.crossfader.collectAsState()
    val masterDeckId by viewModel.masterDeckId.collectAsState()
    val phaseErrorMs by viewModel.phaseErrorMs.collectAsState()
    val activeView by viewModel.consoleDeckView.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjDarkChassis)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("console_screen")
    ) {
        // Dual Waveforms at top
        DualWaveformView(
            deckA = deckA,
            deckB = deckB,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Phase Visualizer bar
        PhaseVisualizer(
            deckA = deckA,
            deckB = deckB,
            phaseErrorMs = phaseErrorMs,
            masterDeckId = masterDeckId,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Tab Selector for Console Sub-Views: DECK A | MIXER | DECK B
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ConsoleTabButton(
                label = "DECK A",
                isSelected = activeView == 'A',
                accentColor = DeckACyan,
                onClick = { viewModel.setConsoleDeckView('A') },
                modifier = Modifier.weight(1f)
            )
            ConsoleTabButton(
                label = "MIXER",
                isSelected = activeView == 'M',
                accentColor = Color(0xFF64748B),
                onClick = { viewModel.setConsoleDeckView('M') },
                modifier = Modifier.weight(1f)
            )
            ConsoleTabButton(
                label = "DECK B",
                isSelected = activeView == 'B',
                accentColor = DeckBAmber,
                onClick = { viewModel.setConsoleDeckView('B') },
                modifier = Modifier.weight(1f)
            )
        }

        // Selected View Content
        Box(modifier = Modifier.weight(1f)) {
            when (activeView) {
                'A' -> DeckView(
                    deckState = deckA,
                    accentColor = DeckACyan,
                    viewModel = viewModel
                )
                'B' -> DeckView(
                    deckState = deckB,
                    accentColor = DeckBAmber,
                    viewModel = viewModel
                )
                'M' -> MixerView(
                    deckA = deckA,
                    deckB = deckB,
                    crossfader = crossfader,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun ConsoleTabButton(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else DjPanelBg)
            .border(1.dp, if (isSelected) accentColor else DjPanelBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("tab_$label")
    ) {
        Text(
            text = label,
            color = if (isSelected) accentColor else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DeckView(
    deckState: DeckState,
    accentColor: Color,
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(DjPanelBg)
            .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        // Track Header Info & BPM Readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deckState.track?.title ?: "No Track Loaded",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${deckState.track?.artist ?: "—"} • ${deckState.track?.genre ?: "—"}",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // BPM readout badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.1f BPM", deckState.effectiveBpm),
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("%+.1f%%", deckState.pitchPercent),
                    color = if (deckState.pitchPercent != 0f) DjYellow else Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transport Action Buttons: MASTER | SYNC | CUE | PLAY
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DeckButton(
                label = "MASTER",
                isActive = deckState.isMaster,
                activeColor = DjYellow,
                onClick = { viewModel.setMasterDeck(deckState.deckId) },
                modifier = Modifier.weight(1f)
            )
            DeckButton(
                label = "SYNC",
                isActive = deckState.isSync,
                activeColor = DjGreen,
                onClick = { viewModel.toggleSync(deckState.deckId) },
                modifier = Modifier.weight(1f)
            )
            DeckButton(
                label = "CUE",
                isActive = deckState.isCueActive,
                activeColor = DjCrimson,
                onClick = { viewModel.triggerCue(deckState.deckId) },
                modifier = Modifier.weight(1f)
            )
            DeckButton(
                label = if (deckState.isPlaying) "PAUSE" else "PLAY",
                isActive = deckState.isPlaying,
                activeColor = accentColor,
                onClick = { viewModel.togglePlayPause(deckState.deckId) },
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Jog Wheel Platter + Pitch Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JogWheelPlatter(
                deckId = deckState.deckId,
                isPlaying = deckState.isPlaying,
                currentPositionSeconds = deckState.currentPositionSeconds,
                effectiveBpm = deckState.effectiveBpm,
                accentColor = accentColor,
                onScratch = { delta -> viewModel.scratchPlatter(deckState.deckId, delta) },
                onRelease = { viewModel.releasePlatter(deckState.deckId) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            // Pitch Fader on side
            MetallicVerticalFader(
                label = "PITCH",
                value = (deckState.pitchPercent + 8f) / 16f,
                accentColor = accentColor,
                height = 150.dp,
                onValueChange = { norm ->
                    val pct = (norm * 16f) - 8f
                    viewModel.setPitchPercent(deckState.deckId, pct)
                },
                modifier = Modifier.width(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Performance Pads: HOT CUE / SAMPLER / BEAT LOOP
        PerformancePads(
            deckId = deckState.deckId,
            deckState = deckState,
            samplerPads = viewModel.samplerPads,
            accentColor = accentColor,
            onTriggerHotCue = { cue -> viewModel.triggerHotCue(deckState.deckId, cue) },
            onTriggerSampler = { smp -> viewModel.triggerSamplerPad(smp) },
            onSetLoop = { loop -> viewModel.setLoop(deckState.deckId, loop) }
        )
    }
}

@Composable
private fun DeckButton(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.3f) else Color(0xFF1E2430))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) activeColor else Color(0xFF2E3848),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .testTag("btn_$label")
    ) {
        Text(
            text = label,
            color = if (isActive) activeColor else Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun MixerView(
    deckA: DeckState,
    deckB: DeckState,
    crossfader: Float,
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(DjPanelBg)
            .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        // Channel Strips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Deck A Channel Strip
            ChannelStrip(
                label = "CH A",
                accentColor = DeckACyan,
                deckState = deckA,
                onHighEq = { viewModel.setHighEq('A', it) },
                onMidEq = { viewModel.setMidEq('A', it) },
                onLowEq = { viewModel.setLowEq('A', it) },
                onFilter = { viewModel.setFilter('A', it) },
                onVolume = { viewModel.setVolume('A', it) },
                modifier = Modifier.weight(1f)
            )

            // Center Master Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(70.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Text("MASTER", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                RotaryKnob(
                    label = "VOL",
                    value = viewModel.audioEngine.masterVolume,
                    isBipolar = false,
                    accentColor = DjCrimson,
                    size = 40.dp,
                    onValueChange = { viewModel.setMasterVolume(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    VuMeterBar(level = viewModel.audioEngine.vuMasterL)
                    VuMeterBar(level = viewModel.audioEngine.vuMasterR)
                }
            }

            // Deck B Channel Strip
            ChannelStrip(
                label = "CH B",
                accentColor = DeckBAmber,
                deckState = deckB,
                onHighEq = { viewModel.setHighEq('B', it) },
                onMidEq = { viewModel.setMidEq('B', it) },
                onLowEq = { viewModel.setLowEq('B', it) },
                onFilter = { viewModel.setFilter('B', it) },
                onVolume = { viewModel.setVolume('B', it) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Crossfader
        SmoothCrossfader(
            value = crossfader,
            onValueChange = { viewModel.setCrossfader(it) }
        )
    }
}

@Composable
private fun ChannelStrip(
    label: String,
    accentColor: Color,
    deckState: DeckState,
    onHighEq: (Float) -> Unit,
    onMidEq: (Float) -> Unit,
    onLowEq: (Float) -> Unit,
    onFilter: (Float) -> Unit,
    onVolume: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF131722))
            .padding(6.dp)
    ) {
        Text(
            text = label,
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3-Band EQ Knobs + Filter
        RotaryKnob("HIGH", deckState.highEq, isBipolar = true, accentColor = accentColor, size = 38.dp, onValueChange = onHighEq)
        Spacer(modifier = Modifier.height(4.dp))
        RotaryKnob("MID", deckState.midEq, isBipolar = true, accentColor = accentColor, size = 38.dp, onValueChange = onMidEq)
        Spacer(modifier = Modifier.height(4.dp))
        RotaryKnob("LOW", deckState.lowEq, isBipolar = true, accentColor = accentColor, size = 38.dp, onValueChange = onLowEq)
        Spacer(modifier = Modifier.height(4.dp))
        RotaryKnob("FILTER", deckState.filter, isBipolar = true, accentColor = DjYellow, size = 38.dp, onValueChange = onFilter)

        Spacer(modifier = Modifier.height(8.dp))

        // Channel Fader & VU Meter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MetallicVerticalFader(
                label = "VOL",
                value = deckState.volume,
                accentColor = accentColor,
                height = 100.dp,
                onValueChange = onVolume
            )
            Spacer(modifier = Modifier.width(6.dp))
            VuMeterBar(level = deckState.vuLevel)
        }
    }
}
