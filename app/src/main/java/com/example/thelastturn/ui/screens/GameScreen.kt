package com.example.thelastturn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


import com.example.thelastturn.R
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.Player
import com.example.thelastturn.viewmodel.GameViewModel
import com.example.thelastturn.viewmodel.PlayerTurn
import com.example.thelastturn.viewmodel.GameState
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = viewModel(),
    onGameOver: () -> Unit = {}
) {
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val opponent by viewModel.opponent.collectAsState()
    val currentTurn by viewModel.currentTurn.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    LaunchedEffect(gameState) {
        if (gameState != GameState.ONGOING) {
            delay(2000)
            navController.navigate("results/${gameState.name}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador de turno
        Text(
            text = when (currentTurn) {
                PlayerTurn.PLAYER -> "Tu turno"
                PlayerTurn.OPPONENT -> "Turno del enemigo"
            },
            style = MaterialTheme.typography.titleLarge,
            color = when (currentTurn) {
                PlayerTurn.PLAYER -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vidas de los jugadores
        PlayerHealthDisplay(player = currentPlayer)
        Spacer(modifier = Modifier.height(24.dp))
        PlayerHealthDisplay(player = opponent)

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de batalla
        BattleField(
            playerCards = currentPlayer.deck,
            opponentCards = opponent.deck,
            onCardClicked = { card ->
                if (currentTurn == PlayerTurn.PLAYER) viewModel.attack(card)
            }
        )
    }
}

@Composable
private fun PlayerHealthDisplay(player: Player) {
    Text(
        text = "${player.name}: ❤️ ${player.health}",
        style = MaterialTheme.typography.titleMedium,
        color = if (player.health <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun BattleField(
    playerCards: List<Card>,
    opponentCards: List<Card>,
    onCardClicked: (Card) -> Unit
) {
    Column {
        // Zona del oponente
        CardZone(cards = opponentCards, isOpponent = true)
        Spacer(modifier = Modifier.height(32.dp))
        // Zona del jugador
        CardZone(cards = playerCards, isOpponent = false, onCardClicked = onCardClicked)
    }
}

@Composable
private fun CardZone(
    cards: List<Card>,
    isOpponent: Boolean = false,
    onCardClicked: (Card) -> Unit = {}
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(cards) { card ->  // 'items' en minúscula
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable(enabled = !isOpponent) { onCardClicked(card) },
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = card.imageRes),  // Asegúrate de tener imágenes en res/drawable
                        contentDescription = card.name,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "ATK: ${card.damage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "HP: ${card.health}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}