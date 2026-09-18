package com.example.masavudj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.masavudj.model.InbuiltLoopItem
import com.example.masavudj.ui.theme.*
import com.example.masavudj.viewmodel.DjViewModel

@Composable
fun LooperScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLoop by viewModel.selectedLoop.collectAsState()
    val isLooperPlaying by viewModel.isLooperPlaying.collectAsState()
    val deckA by viewModel.deckA.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjDarkChassis)
            .padding(10.dp)
            .testTag("looper_screen")
    ) {
        // Looper Header & Play/Stop Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DjPanelBg)
                .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MASAVU INBUILT LOOPER",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SYNC TO MASTER (${deckA.effectiveBpm.toInt()} BPM)",
                    color = DeckACyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { viewModel.toggleLooper() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLooperPlaying) DjCrimson else DjGreen
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("btn_toggle_looper")
            ) {
                Text(
                    text = if (isLooperPlaying) "STOP LOOP" else "START LOOP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 16-Step Beat Grid Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F131A))
                .border(1.dp, Color(0xFF1E2533), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentStep = if (isLooperPlaying) ((deckA.currentBeatFloat * 4f).toInt() % 16) else -1
                for (s in 0..15) {
                    val isLit = s == currentStep
                    val isDownbeat = (s % 4 == 0)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp)
                            .padding(horizontal = 1.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isLit -> DjCrimson
                                    isDownbeat -> Color(0xFF2A3448)
                                    else -> Color(0xFF171D28)
                                }
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "5 MASTER BEAT LOOPS",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // List of 5 Inbuilt Master Loops
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.inbuiltLoopsList) { loop ->
                val isSelected = selectedLoop.id == loop.id
                LoopCard(
                    loop = loop,
                    isSelected = isSelected,
                    onSelect = { viewModel.selectInbuiltLoop(loop) }
                )
            }
        }
    }
}

@Composable
private fun LoopCard(
    loop: InbuiltLoopItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF1B2332) else DjPanelBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) DeckACyan else DjPanelBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = loop.name,
                color = if (isSelected) DeckACyan else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${loop.genre} • ${loop.bpm.toInt()} BPM • ${loop.loopBeats} BEATS",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = loop.description,
                color = Color(0xFF64748B),
                fontSize = 9.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = { onSelect() },
            colors = RadioButtonDefaults.colors(selectedColor = DeckACyan)
        )
    }
}
