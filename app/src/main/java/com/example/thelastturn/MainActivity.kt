package com.example.thelastturn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.thelastturn.ui.screens.*
import com.example.thelastturn.ui.theme.TheLastTurnTheme
import com.example.thelastturn.viewmodel.GameViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheLastTurnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
private fun Navigation() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = viewModel()

    // Obtener el contexto de la actividad actual
    val activity = LocalActivity.current as? ComponentActivity

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartGame = { navController.navigate("settings") },
                onHelp = { navController.navigate("help") },
                onExit = { activity?.finish() } // Usa el contexto de la actividad
            )
        }

        composable("settings") {
            SettingsScreen { playerName, numSlots, totalTime, actionTime ->
                viewModel.startNewGame(numSlots, playerName, totalTime, actionTime)
                // Si necesitas usar totalTime/actionTime dentro del VM, lo guardarías aquí
                navController.navigate("game")
            }
        }

        composable("game") {
            GameScreen { result ->
                navController.navigate("results/$result")
            }
        }

        composable("results/{result}") { backStack ->
            val resultArg = backStack.arguments?.getString("result") ?: "DRAW"
            val sizeText = "${viewModel.numberOfSlots}×${viewModel.numberOfSlots}"
            ResultsScreen(
                playerName = viewModel.player.name,
                result = resultArg,
                boardSize = sizeText,
                gameLog      = viewModel.getGameLogSummary(),
                playerLives = viewModel.player.currentHits,
                enemyLives = viewModel.enemy.currentHits,
                onRestart = {
                    navController.popBackStack("home", inclusive = false)
                },
                onExit = { activity?.finish() } // Usa el contexto de la actividad
                ,onBackToHome = { navController.popBackStack("home", inclusive = false) }
            )
        }

        composable("help") {
            HelpScreen(onBack = { navController.popBackStack() })
        }
    }
}