package com.example.thelastturn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thelastturn.R
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.viewmodel.GameViewModel

@Composable
fun GameScreen(onGameEnd: (String) -> Unit) {
    val viewModel: GameViewModel = viewModel()

    LaunchedEffect(viewModel.navigationEvent.value) {
        viewModel.navigationEvent.value?.let { result ->
            onGameEnd(result)
            viewModel.resetNavigation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        EnemyInfo(viewModel)

        // Tablero enemigo
        BoardSection(
            slots = viewModel.enemySlots,
            isPlayer = false,
            viewModel = viewModel,
            availableCards = viewModel.enemyHand
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tablero jugador
        BoardSection(
            slots = viewModel.playerSlots,
            isPlayer = true,
            viewModel = viewModel,
            availableCards = viewModel.playerHand
        )

        PlayerInfo(viewModel)

        // Baraja del jugador
        PlayerHand(
            cards = viewModel.playerHand,
            selectedCard = viewModel.selectedCard.value,
            onCardSelected = { viewModel.selectCard(it) }
        )
    }
}

@Composable
private fun PlayerInfo(viewModel: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Jugador: ${viewModel.player.value.health} ❤")
        Text("Turno: ${viewModel.currentTurn.value.name}")
    }
}

@Composable
private fun EnemyInfo(viewModel: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Enemigo: ${viewModel.enemy.value.health} ❤")
        Text("Cartas: ${viewModel.enemyHand.size}")
    }
}

@Composable
private fun BoardSection(
    slots: List<BoardSlot>,
    isPlayer: Boolean,
    viewModel: GameViewModel,
    availableCards: List<Card>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        slots.forEach { slot ->
            BoardSlotUI(
                slot = slot,
                isPlayer = isPlayer,
                viewModel = viewModel,
                availableCards = availableCards
            )
        }
    }
}

@Composable
private fun BoardSlotUI(
    slot: BoardSlot,
    isPlayer: Boolean,
    viewModel: GameViewModel,
    availableCards: List<Card>
) {
    val cardInSlot = slot.card // Obtener la Card directamente del slot
    val slotColor = if (isPlayer) Color(0xFF2196F3) else Color(0xFFF44336)

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp)
            .border(
                width = 2.dp,
                color = slotColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isPlayer) {
                if (viewModel.selectedCard.value != null) {
                    viewModel.placeCard(slot.id)
                }
            }
            .background(
                color = slotColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (cardInSlot != null) {
            CardInSlot(card = cardInSlot)
        } else {
            SlotPlaceholder(slotId = slot.id, color = slotColor)
        }
    }
}

@Composable
private fun CardInSlot(card: Card) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = card.imageRes),
                contentDescription = card.name,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = card.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.2f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${card.damage}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❤", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${card.health}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Green
                )
            }
        }
    }
}

@Composable
private fun SlotPlaceholder(slotId: Int, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "SLOT $slotId",
            color = color.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun PlayerHand(
    cards: List<Card>,
    selectedCard: Card?,
    onCardSelected: (Card) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        items(cards) { card ->
            CardUI(
                card = card,
                isSelected = card.id == selectedCard?.id,
                onClick = { onCardSelected(card) }
            )
        }
    }
}

@Composable
private fun CardUI(
    card: Card,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Green else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        CardInSlot(card = card)
    }
}