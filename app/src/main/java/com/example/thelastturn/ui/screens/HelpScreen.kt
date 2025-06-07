package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTablet = isTablet() // Reutilizamos la función isTablet de GameScreen.kt

    val horizontalPadding = if (isTablet) 80.dp else 30.dp
    val verticalPadding = if (isTablet) 40.dp else 30.dp
    val textSizeMultiplier = if (isTablet) 1.2f else 1f // Aumentar tamaño de texto en tablets
    val buttonHeight = if (isTablet) 80.dp else 70.dp
    val buttonPadding = if (isTablet) 30.dp else 20.dp

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .verticalScroll(rememberScrollState()) // Añadir scroll para asegurar visibilidad en pantallas pequeñas o landscape con mucho contenido
        ) {
            Text(
                text = "Cómo Jugar",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize * textSizeMultiplier
                )
            )

            Spacer(modifier = Modifier.height(20.dp * textSizeMultiplier))

            Text(
                text = "1. Coloca tus cartas en los slots disponibles.\n" +
                        "2. Combate contra el enemigo usando las cartas.\n" +
                        "3. Gana destruyendo todos los puntos de vida del enemigo.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    color = Color.White,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * textSizeMultiplier
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp * textSizeMultiplier))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .padding(buttonPadding)
                    .height(buttonHeight)
                    .fillMaxWidth(if (isTablet && isLandscape) 0.6f else 0.8f) // Ajustar ancho del botón
                    .border(
                        width = 3.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = Color(0xFFA0522D),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Regresar",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize * textSizeMultiplier
                    )
                )
            }
        }
    }
}