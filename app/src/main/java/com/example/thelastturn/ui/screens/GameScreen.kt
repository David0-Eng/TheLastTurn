package com.example.thelastturn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thelastturn.viewmodel.GameViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(navController: NavController, viewModel: GameViewModel = viewModel()) {
    val state = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Game") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Vidas Jugador: ${state.value.playerLives}")
            Text("Vidas Enemigo: ${state.value.cpuLives}")

            Button(onClick = {
                viewModel.endGame()
                navController.navigate("result_screen")
            }) {
                Text("Terminar partida")
            }
        }
    }
}