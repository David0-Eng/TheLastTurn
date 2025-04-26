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
fun HomeScreen(
    onStartGame: () -> Unit,
    onHelp: () -> Unit,
    onExit: () -> Unit
) {
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
            // Título con estilo "western" más grande
            Text(
                text = "The Last Turn",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(100.dp))

            // Botón "Empezar Partida"
            Button(
                onClick = { onStartGame() },
                modifier = Modifier
                    .padding(20.dp)
                    .height(70.dp)
                    .width(250.dp) // Ancho fijo para todos los botones
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
                    text = "Empezar Partida",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón "Ayuda"
            Button(
                onClick = { onHelp() },
                modifier = Modifier
                    .padding(20.dp)
                    .height(70.dp)
                    .width(250.dp) // Ancho fijo para todos los botones
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
                    text = "Ayuda",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón "Salir"
            Button(
                onClick = { onExit() },
                modifier = Modifier
                    .padding(20.dp)
                    .height(70.dp)
                    .width(250.dp) // Ancho fijo para todos los botones
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
                    text = "Salir",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                    )
                )
            }
        }
    }
}