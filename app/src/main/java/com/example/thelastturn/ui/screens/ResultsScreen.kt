package com.example.thelastturn.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ResultsScreen(
    result: String,
    navController: NavController,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (result) {
                "VICTORY" -> "¡Victoria!"
                "DEFEAT" -> "Derrota"
                else -> "Empate"
            },
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            onRestart() // reinicia el estado del juego, si aplica
            navController.navigate("home") {
                popUpTo("game") { inclusive = true } // limpia el backstack hasta la pantalla de juego
            }
        }) {
            Text("Volver al inicio")
        }
    }
}
