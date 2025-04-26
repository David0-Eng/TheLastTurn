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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ResultsScreen(
    result: String,
    navController: NavController,
    onRestart: () -> Unit
) {
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
                            colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (result) {
                        "VICTORY" -> "¡Victoria!"
                        "DEFEAT" -> "Derrota"
                        else -> "Empate"
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        onRestart()
                        navController.navigate("home") {
                            popUpTo("game") { inclusive = true }
                        }
                    },
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
                        text = "Volver al inicio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            }
        }
    }
}
