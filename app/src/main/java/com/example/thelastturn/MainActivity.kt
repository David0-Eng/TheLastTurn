package com.example.thelastturn

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thelastturn.data.AppDatabase
import com.example.thelastturn.data.GameRepository
import com.example.thelastturn.ui.screens.*
import com.example.thelastturn.ui.theme.TheLastTurnTheme
import com.example.thelastturn.viewmodel.GameViewModel
import com.example.thelastturn.viewmodel.GameViewModelFactory // Import de factory

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
    val activity = LocalActivity.current as? ComponentActivity

    // Obtener el contexto de la base de datos y del repositorio
    val context = LocalContext.current
    // Cast necesario para trabajar con Factory
    val application = context.applicationContext as Application

    // Database y repositorio inicializados
    val database = remember { AppDatabase.getDatabase(context) }
    val gameRepository = remember { GameRepository(database.partidaDao()) }

    // ViewModelFactory
    val gameViewModelFactory = remember {
        GameViewModelFactory(application, gameRepository)
    }

    // Se instancia GameViewModel usando la factory
    val gameViewModel: GameViewModel = viewModel(factory = gameViewModelFactory)


    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartGame = { navController.navigate("settings") },
                onHelp = { navController.navigate("help") },
                onShowPreferences = { navController.navigate("preferences") },
                onShowPastGames = { navController.navigate("past_games") },
                onExit = { activity?.finish() }
            )
        }

        composable("settings") {
            SettingsScreen { playerName, numSlots, totalTime, actionTime ->
                gameViewModel.startNewGame(numSlots, playerName, totalTime, actionTime)
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

            ResultsScreen(
                playerName = gameViewModel.player.name,
                result = resultArg,
                boardSize = gameViewModel.numberOfSlots, // Pasa el Int directamente
                gameLog      = gameViewModel.getGameLogSummary(),
                playerLives = gameViewModel.player.currentHits,
                enemyLives = gameViewModel.enemy.currentHits,
                onRestart = {
                    navController.popBackStack("home", inclusive = false)
                    navController.navigate("settings")
                },
                onExit = { activity?.finish() },
                onBackToHome = { navController.popBackStack("home", inclusive = false) },
                gameRepository = gameRepository // FIX 2: Pasa la instancia de gameRepository
            )
        }

        composable("help") {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        composable("preferences") {
            PreferencesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("past_games") {
            PastGamesScreen(
                gameRepository = gameRepository,
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }
        // Remember to add PastGameDetailScreen if you haven't already
        composable("past_game_detail") {
            PastGameDetailScreen(navController = navController)
        }
    }
}