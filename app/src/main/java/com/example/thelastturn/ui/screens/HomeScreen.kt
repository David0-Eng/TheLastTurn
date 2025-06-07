package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartGame: () -> Unit,
    onHelp: () -> Unit,
    onShowPreferences: () -> Unit, // New callback for preferences
    onShowPastGames: () -> Unit,   // New callback for past games
    onExit: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = isTablet() // Reusing the isTablet helper function

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decide layout based on orientation and device type
        if (isLandscape && isTablet) {
            // Tablet landscape: Title on left, buttons on right
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "The Last Turn",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                            color = Color.White
                        )
                    )
                }

                // Buttons on the right side
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeButtons(
                        onStartGame = onStartGame,
                        onHelp = onHelp,
                        onShowPreferences = onShowPreferences,
                        onShowPastGames = onShowPastGames,
                        onExit = onExit
                    )
                }
            }
        } else {
            // Portrait orientation (smartphones and tablets) and Smartphone Landscape:
            // Title on top, buttons below.
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
            ) {
                Text(
                    text = "The Last Turn",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(80.dp))

                HomeButtons(
                    onStartGame = onStartGame,
                    onHelp = onHelp,
                    onShowPreferences = onShowPreferences,
                    onShowPastGames = onShowPastGames,
                    onExit = onExit
                )
            }
        }
    }
}

/**
 * A reusable composable for the main menu buttons.
 */
@Composable
private fun HomeButtons(
    onStartGame: () -> Unit,
    onHelp: () -> Unit,
    onShowPreferences: () -> Unit,
    onShowPastGames: () -> Unit,
    onExit: () -> Unit
) {
    val buttonModifier = Modifier
        .padding(8.dp)
        .height(70.dp)
        .fillMaxWidth(0.8f)
        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
        .background(Color(0xFFA0522D), RoundedCornerShape(12.dp))

    Button(
        onClick = onStartGame,
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Empezar Partida",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
        )
    }

    Button(
        onClick = onShowPastGames, // New button for past games
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Partidas Anteriores",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
        )
    }

    Button(
        onClick = onShowPreferences, // New button for preferences
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
        )
    }

    Button(
        onClick = onHelp,
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Ayuda",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
        )
    }

    Button(
        onClick = onExit,
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Salir",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
            )
        )
    }
}