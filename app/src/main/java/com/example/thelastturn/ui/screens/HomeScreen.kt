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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(onStartGame: () -> Unit) {
    // Fondo rústico (opcionalmente puedes usar una imagen de fondo)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF8B4513) // Marrón terroso
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Fondo de madera simulado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513)) // Degradado marrón
                        )
                    )
            )

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(30.dp)
            ) {
                // Título con estilo "western" más grande
                Text(
                    text = "The Last Turn",
                    style = MaterialTheme.typography.headlineLarge.copy( // Cambiado a headlineLarge
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif, // Fuente SansSerif
                        color = Color.White // Texto blanco para contraste
                    )
                )

                Spacer(modifier = Modifier.height(40.dp)) // Aumentado el espacio

                // Botón estilizado más grande
                Button(
                    onClick = { onStartGame() },
                    modifier = Modifier
                        .padding(20.dp)
                        .height(70.dp) // Altura del botón aumentada
                        .border(
                            width = 3.dp, // Borde más grueso
                            color = Color.Black,
                            shape = RoundedCornerShape(12.dp) // Bordes más redondeados
                        )
                        .background(
                            color = Color(0xFFA0522D), // Marrón oscuro
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Empezar Partida",
                        style = MaterialTheme.typography.titleLarge.copy( // Cambiado a titleLarge
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif // Fuente SansSerif
                        )
                    )
                }
            }
        }
    }
}