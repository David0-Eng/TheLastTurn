package com.example.thelastturn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thelastturn.ui.screens.GameScreen
import com.example.thelastturn.ui.screens.HomeScreen
import com.example.thelastturn.ui.theme.TheLastTurnTheme
import androidx.compose.ui.Modifier
import com.example.thelastturn.model.GameState
import com.example.thelastturn.ui.screens.ResultsScreen



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
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onStartGame = {
                    navController.navigate("game")
                }
            )
        }
        composable("game") {
            GameScreen(
                onGameEnd = { result ->
                    navController.navigate("results/$result")
                }
            )
        }
        composable("results/{result}") { backStackEntry ->
            val result = backStackEntry.arguments?.getString("result")
            ResultsScreen(
                result = when (result) {
                    "VICTORY" -> GameState.VICTORY
                    "DEFEAT" -> GameState.DEFEAT
                    "DRAW" -> GameState.DRAW
                    else -> GameState.ONGOING
                }.toString(),
                navController = navController,
                onRestart = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}
