package com.example.thelastturn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thelastturn.ui.screens.GameScreen
import com.example.thelastturn.ui.screens.HomeScreen
import com.example.thelastturn.ui.screens.ResultScreen
import com.example.thelastturn.ui.theme.TheLastTurnTheme
import com.example.thelastturn.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheLastTurnApp()
        }
    }
}

@Composable
fun TheLastTurnApp() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = viewModel()

    // Se obtiene el ganador desde el ViewModel (o desde alguna lógica que determines)
    val winner = viewModel.winner.value // Esto es solo un ejemplo, depende de tu lógica.

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }
        composable("game") {
            GameScreen(navController, viewModel)
        }
        composable("results") {
            // Aquí pasas el ganador y la función onSendEmail al ResultScreen
            ResultScreen(
                navController = navController,
                winner = winner,
                onSendEmail = { viewModel.sendEmail() } // Aquí se usa la función de enviar email desde el ViewModel
            )
        }
    }
}
