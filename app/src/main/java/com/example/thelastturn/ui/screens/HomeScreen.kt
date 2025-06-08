package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thelastturn.ui.components.AppActionButton

@Composable
fun HomeScreen(
    onStartGameDirectly: () -> Unit,
    onHelp: () -> Unit,
    onShowPreferences: () -> Unit,
    onShowPastGames: () -> Unit,
    onExit: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val scrollState = rememberScrollState()

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
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .then(
                    if (isLandscape) Modifier.verticalScroll(scrollState)
                    else Modifier
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "The Last Turn",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 64.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 50.dp)
            )

            AppActionButton(
                text = "Empezar partida",
                onClick = onStartGameDirectly,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            AppActionButton(
                text = "Configuración",
                onClick = onShowPreferences,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            AppActionButton(
                text = "Historial",
                onClick = onShowPastGames,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            AppActionButton(
                text = "Ayuda",
                onClick = onHelp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            AppActionButton(
                text = "Salir",
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}