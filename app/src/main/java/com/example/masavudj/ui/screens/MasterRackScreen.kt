package com.example.masavudj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.masavudj.ui.components.MetallicVerticalFader
import com.example.masavudj.ui.components.RotaryKnob
import com.example.masavudj.ui.theme.*
import com.example.masavudj.viewmodel.DjViewModel

@Composable
fun MasterRackScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val eq31Bands by viewModel.eq31Bands.collectAsState()
    val scrollState = rememberScrollState()

    val freqLabels = listOf(
        "20", "25", "31", "40", "50", "63", "80", "100", "125", "160",
        "200", "250", "315", "400", "500", "630", "800", "1k", "1.2k", "1.6k",
        "2k", "2.5k", "3.1k", "4k", "5k", "6.3k", "8k", "10k", "12.5k", "16k", "20k"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjDarkChassis)
            .padding(10.dp)
            .testTag("master_rack_screen")
    ) {
        // Master Dynamics & Limiter Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DjPanelBg)
                .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "31-BAND HIGH PRECISION MASTER EQ",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ISO 1/3 OCTAVE FREQUENCY SPECTRUM (±12 dB)",
                    color = DeckACyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { viewModel.resetMasterEq() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3848)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("RESET FLAT", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 31 Sliders Horizontal Scroll View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DjPanelBg)
                .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 31) {
                    val gainDb = eq31Bands.getOrElse(i) { 0f }
                    val normalized = (gainDb + 12f) / 24f
                    val label = freqLabels.getOrElse(i) { "${i}" }

                    MetallicVerticalFader(
                        label = label,
                        value = normalized,
                        accentColor = if (i < 10) DjCrimson else if (i < 20) DjYellow else DeckACyan,
                        height = 140.dp,
                        onValueChange = { norm ->
                            val db = (norm * 24f) - 12f
                            viewModel.setMasterBand(i, db)
                        },
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamics Mastering Panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141822))
                .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RotaryKnob("LIMITER", 0.85f, isBipolar = false, accentColor = DjCrimson, size = 40.dp, onValueChange = {})
            RotaryKnob("COMPRESS", 0.5f, isBipolar = false, accentColor = DeckBAmber, size = 40.dp, onValueChange = {})
            RotaryKnob("WARMTH", 0.65f, isBipolar = false, accentColor = DjYellow, size = 40.dp, onValueChange = {})
            RotaryKnob("STEREO WIDE", 0.5f, isBipolar = false, accentColor = DeckACyan, size = 40.dp, onValueChange = {})
        }
    }
}
