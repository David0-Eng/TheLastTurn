package com.example.thelastturn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ResultScreen(
    navController: NavController,
    winner: String,
    onSendEmail: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Resultado", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Ganador: $winner")

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onSendEmail) {
                Text("Enviar resultado por Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navController.popBackStack("home", inclusive = false)
            }) {
                Text("Volver al Inicio")
            }
        }
    }
}
