package com.example.thelastturn.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HelpScreen(onBack: () -> Unit) {
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
            modifier = Modifier.padding(30.dp)
        ) {
            Text(
                text = "Cómo Jugar",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "1. Coloca tus cartas en los slots disponibles.\n" +
                        "2. Combate contra el enemigo usando las cartas.\n" +
                        "3. Gana destruyendo todos los puntos de vida del enemigo.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .padding(20.dp)
                    .height(70.dp)
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
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                )
            }
        }
    }
}