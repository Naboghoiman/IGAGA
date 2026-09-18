package com.example.masavudj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masavudj.ui.screens.*
import com.example.masavudj.ui.theme.*
import com.example.masavudj.viewmodel.DjViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DjViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MasavuDjTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DjDarkChassis
                ) { innerPadding ->
                    MasavuDjApp(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MasavuDjApp(
    viewModel: DjViewModel,
    modifier: Modifier = Modifier
) {
    val activeScreen by viewModel.activeScreen.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DjDarkChassis)
    ) {
        // Top Master Navigation Bar
        TopNavBar(
            activeScreen = activeScreen,
            onScreenSelected = { viewModel.setActiveScreen(it) }
        )

        // Active Screen Body
        Box(modifier = Modifier.weight(1f)) {
            when (activeScreen) {
                "CONSOLE" -> ConsoleScreen(viewModel = viewModel)
                "LOOPER" -> LooperScreen(viewModel = viewModel)
                "MASTER_RACK" -> MasterRackScreen(viewModel = viewModel)
                "LIBRARY" -> LibraryScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun TopNavBar(
    activeScreen: String,
    onScreenSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DjPanelBg)
            .border(1.dp, DjPanelBorder)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Branding
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(DeckACyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "MASAVU DJ",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Navigation Tabs
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            NavTabItem(
                label = "DECKS",
                icon = Icons.Default.Radio,
                isSelected = activeScreen == "CONSOLE",
                onClick = { onScreenSelected("CONSOLE") }
            )
            NavTabItem(
                label = "LOOPER",
                icon = Icons.Default.Loop,
                isSelected = activeScreen == "LOOPER",
                onClick = { onScreenSelected("LOOPER") }
            )
            NavTabItem(
                label = "MASTER FX",
                icon = Icons.Default.Equalizer,
                isSelected = activeScreen == "MASTER_RACK",
                onClick = { onScreenSelected("MASTER_RACK") }
            )
            NavTabItem(
                label = "LIBRARY",
                icon = Icons.Default.LibraryMusic,
                isSelected = activeScreen == "LIBRARY",
                onClick = { onScreenSelected("LIBRARY") }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) DeckACyan.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (isSelected) DeckACyan else Color.Transparent, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
            .testTag("nav_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) DeckACyan else Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = if (isSelected) DeckACyan else Color(0xFFCBD5E1),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
