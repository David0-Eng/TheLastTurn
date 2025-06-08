package com.example.thelastturn

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.thelastturn.data.AppDatabase
import com.example.thelastturn.data.GameRepository
import com.example.thelastturn.data.PreferencesManager
import com.example.thelastturn.ui.screens.*
import com.example.thelastturn.ui.theme.TheLastTurnTheme
import com.example.thelastturn.viewmodel.GameViewModel
import com.example.thelastturn.viewmodel.GameViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val activity = LocalActivity.current as? ComponentActivity // Obtiene la actividad para `finish()`

    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Instancias de base de datos, repository y preferencias, recordadas para eficiencia
    val database = remember { AppDatabase.getDatabase(context) }
    val gameRepository = remember { GameRepository(database.partidaDao()) }
    val preferencesManager = remember { PreferencesManager(context) }

    // Factory y ViewModel para el juego
    val gameViewModelFactory = remember {
        GameViewModelFactory(application, gameRepository)
    }
    val gameViewModel: GameViewModel = viewModel(factory = gameViewModelFactory)
    val coroutineScope = rememberCoroutineScope() // Scope para operaciones asíncronas

    // Callback para iniciar una nueva partida con preferencias guardadas
    val onStartNewGameWithPreferences: () -> Unit = {
        coroutineScope.launch {
            // Carga las preferencias actuales
            val playerName = preferencesManager.playerName.firstOrNull() ?: ""
            val numSlots = preferencesManager.numSlots.firstOrNull() ?: 4
            val totalTime = preferencesManager.totalTime.firstOrNull() ?: 180
            val actionTime = preferencesManager.actionTime.firstOrNull() ?: 30

            // Si el nombre de jugador está vacío, redirige a preferencias para que lo configure
            if (playerName.isBlank()) {
                navController.navigate("preferences")
            } else {
                gameViewModel.startNewGame(numSlots, playerName, totalTime, actionTime)
                navController.navigate("game")
            }
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartGameDirectly = onStartNewGameWithPreferences,
                onHelp = { navController.navigate("help") },
                onShowPreferences = { navController.navigate("preferences") },
                onShowPastGames = { navController.navigate("past_games") },
                onExit = { activity?.finish() } // Cierra la actividad al salir
            )
        }

        composable("game") {
            GameScreen(
                onGameEnd = { result ->
                    // Navega a la pantalla de resultados con el resultado de la partida
                    navController.navigate("results/$result")
                }
            )
        }

        composable(
            route = "results/{result}", // Ruta con argumento dinámico
            arguments = listOf(navArgument("result") { type = NavType.StringType })
        ) { backStackEntry ->
            val resultArg = backStackEntry.arguments?.getString("result") ?: "DRAW" // Obtiene el resultado

            ResultsScreen(
                playerName = gameViewModel.player.name,
                result = resultArg,
                boardSize = gameViewModel.numberOfSlots,
                gameLog = gameViewModel.getGameLogSummary(), // Resumen del log para mostrar
                playerLives = gameViewModel.player.currentHits,
                enemyLives = gameViewModel.enemy.currentHits,
                onStartNewGame = {
                    // Limpia la pila de navegación hasta "home" y luego inicia una nueva partida
                    navController.popBackStack("home", inclusive = false)
                    onStartNewGameWithPreferences()
                },
                onExit = { activity?.finish() }, // Cierra la app
                onBackToHome = {
                    navController.popBackStack("home", inclusive = false) // Vuelve a la pantalla de inicio
                },
                gameRepository = gameRepository // Pasa el repositorio para guardar resultados
            )
        }

        composable("help") {
            HelpScreen(onBack = { navController.popBackStack() }) // Vuelve a la pantalla anterior
        }

        composable("preferences") {
            PreferencesScreen(
                onSaveAndBack = { _, _, _, _ -> // Los parámetros no se usan aquí, solo se vuelve atrás
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() } // Vuelve a la pantalla anterior
            )
        }

        composable("past_games") {
            PastGamesScreen(
                gameRepository = gameRepository,
                navController = navController,
                onBack = { navController.popBackStack() } // Vuelve a la pantalla anterior
            )
        }

        composable(
            route = "past_game_detail/{partidaId}", // Ruta con argumento para el ID de la partida
            arguments = listOf(navArgument("partidaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val partidaId = backStackEntry.arguments?.getString("partidaId")
            PastGameDetailScreen(
                navController = navController,
                gameRepository = gameRepository,
                partidaId = partidaId // Pasa el ID de la partida al detalle
            )
        }
    }
}