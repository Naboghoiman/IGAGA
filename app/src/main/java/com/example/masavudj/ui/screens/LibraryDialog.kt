package com.example.masavudj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.masavudj.model.TrackItem
import com.example.masavudj.ui.theme.*
import com.example.masavudj.viewmodel.DjViewModel

@Composable
fun LibraryScreen(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    var selectedGenre by remember { mutableStateOf("ALL") }
    val genres = listOf("ALL", "House", "Tech House", "Afrobeat", "Amapiano", "Techno", "Hip Hop")

    val filteredTracks = remember(selectedGenre) {
        if (selectedGenre == "ALL") {
            viewModel.trackLibrary
        } else {
            viewModel.trackLibrary.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjDarkChassis)
            .padding(10.dp)
            .testTag("library_screen")
    ) {
        Text(
            text = "SONG LIBRARY & BEATGRID REFINER",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Genre Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(genres) { g ->
                val isSel = selectedGenre == g
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSel) DeckACyan else DjPanelBg)
                        .border(1.dp, if (isSel) DeckACyan else DjPanelBorder, RoundedCornerShape(14.dp))
                        .clickable { selectedGenre = g }
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = g,
                        color = if (isSel) DjDarkChassis else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Track List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTracks) { track ->
                TrackCard(
                    track = track,
                    onLoadA = {
                        viewModel.loadTrack('A', track)
                        viewModel.setActiveScreen("CONSOLE")
                        viewModel.setConsoleDeckView('A')
                    },
                    onLoadB = {
                        viewModel.loadTrack('B', track)
                        viewModel.setActiveScreen("CONSOLE")
                        viewModel.setConsoleDeckView('B')
                    }
                )
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: TrackItem,
    onLoadA: () -> Unit,
    onLoadB: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DjPanelBg)
            .border(1.dp, DjPanelBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${track.artist} • ${track.genre} • Key: ${track.musicalKey}",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${track.bpm.toInt()} BPM • BeatGrid: STRAIGHT (100% Locked)",
                color = DjGreen,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = onLoadA,
                colors = ButtonDefaults.buttonColors(containerColor = DeckACyan.copy(alpha = 0.25f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeckACyan),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("LOAD A", color = DeckACyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = onLoadB,
                colors = ButtonDefaults.buttonColors(containerColor = DeckBAmber.copy(alpha = 0.25f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeckBAmber),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("LOAD B", color = DeckBAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
