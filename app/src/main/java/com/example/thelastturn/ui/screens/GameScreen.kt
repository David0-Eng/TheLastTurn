package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thelastturn.R
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.viewmodel.GameViewModel
import com.example.thelastturn.model.PlayerTurn

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun GameScreen(onGameEnd: (String) -> Unit) {
    val viewModel: GameViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ViewModelStoreOwner)

    LaunchedEffect(viewModel.navigationEvent.value) {
        viewModel.navigationEvent.value?.let { result ->
            onGameEnd(result)
            viewModel.resetNavigation()
        }
    }


    val landscape = isLandscape()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)), // fondo beige
        contentAlignment = Alignment.Center
    ) {

        if (landscape) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimerDisplay(viewModel)
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .padding(end = 16.dp), // espacio entre enemigo y jugador
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EnemyInfo(viewModel)

                        BoardSection(
                            slots = viewModel.enemySlots,
                            isPlayer = false,
                            viewModel = viewModel,
                            cardSpacing = 8.dp
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight()
                            .padding(start = 16.dp), // espacio entre enemigo y jugador
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PlayerInfo(viewModel)

                        BoardSection(
                            slots = viewModel.playerSlots,
                            isPlayer = true,
                            viewModel = viewModel,
                            cardSpacing = 8.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp)) // espacio entre tablero y mano

                Box(
                    modifier = Modifier
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerHand(
                        cards = viewModel.playerHand.take(4),
                        selectedCard = viewModel.selectedCard.value,
                        onCardSelected = { viewModel.selectCard(it) }
                    )
                }
            }
        }

        else {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .matchParentSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TimerDisplay(viewModel)
                }

                EnemyInfo(viewModel)
                BoardSection(
                    slots = viewModel.enemySlots,
                    isPlayer = false,
                    viewModel = viewModel,
                )
                BoardSection(
                    slots = viewModel.playerSlots,
                    isPlayer = true,
                    viewModel = viewModel,
                )
                PlayerInfo(viewModel)
                PlayerHand(
                    cards = viewModel.playerHand.take(4),
                    selectedCard = viewModel.selectedCard.value,
                    onCardSelected = { viewModel.selectCard(it) }
                )
            }
        }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(viewModel.player.name, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            HealthIcons(viewModel.player.currentHits)
        }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enemigo:", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            HealthIcons(viewModel.enemy.currentHits)
        }
        Text("Cartas: ${viewModel.enemyHand.size}")
    }
}

@Composable
private fun HealthIcons(health: Int) {
    val maxHealth = 3
    Row {
        repeat(maxHealth) { index ->
            val isVisible = index < health
            AnimatedVisibility(visible = isVisible) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Corazón",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BoardSection(
    slots: List<BoardSlot>,
    isPlayer: Boolean,
    viewModel: GameViewModel,
    cardSpacing: Dp = 14.dp
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(cardSpacing)
    ) {
        items(slots) { slot ->
            BoardSlotUI(
                slot = slot,
                isPlayer = isPlayer,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun BoardSlotUI(
    slot: BoardSlot,
    isPlayer: Boolean,
    viewModel: GameViewModel
) {
    val cardInSlot = slot.card
    val slotColor = if (isPlayer) Color(0xFF2196F3) else Color(0xFFF44336)

    Box(
        modifier = Modifier
            .width(73.dp)
            .height(120.dp)
            .border(
                width = 1.5.dp,
                color = slotColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = isPlayer && viewModel.currentTurn.value == PlayerTurn.PLAYER) {
                if (viewModel.selectedCard.value != null) {
                    viewModel.placeCard(slot.id)
                }
            }
            .background(slotColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = cardInSlot != null) {
            cardInSlot?.let {
                CardInSlot(card = it)
            }
        }
        if (cardInSlot == null) {
            SlotPlaceholder(color = slotColor)
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
        Image(
            painter = painterResource(id = card.imageRes),
            contentDescription = card.name,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(4.5.dp))
        HealthBar(health = card.health, maxHealth = card.maxHealth)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.2f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔", style = MaterialTheme.typography.bodySmall)
                Text("${card.damage}", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❤", style = MaterialTheme.typography.bodySmall)
                Text("${card.health}/${card.maxHealth}", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            }
        }
    }
}

@Composable
private fun HealthBar(health: Int, maxHealth: Int) {
    val animatedHealth by animateFloatAsState(targetValue = health.toFloat())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color.Gray, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedHealth / maxHealth)
                .height(8.dp)
                .background(Color.Red, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun SlotPlaceholder(color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(18.dp))
            .border(1.5.dp, color = color, shape = RoundedCornerShape(10.dp))
    ) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "Slot vacío",
            tint = color.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun PlayerHand(
    cards: List<Card>,
    selectedCard: Card?,
    onCardSelected: (Card) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(2.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD2B48C)),
        shape = RoundedCornerShape(10.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
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
}


@Composable
private fun CardUI(
    card: Card,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)

    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .width(80.dp)
            .height(120.dp)
            .border(
                width = 2.dp,
                color = if (isSelected) Color.Green else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .background(Color.White, RoundedCornerShape(12.dp))
    ) {
        CardInSlot(card = card)
    }
}

@Composable
private fun TimerDisplay(viewModel: GameViewModel) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .background(Color.Transparent),
        colors = CardDefaults.cardColors(containerColor = Color(0xAAFFFFFF).copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tiempo total",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "${viewModel.remainingTotalTime}s",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tiempo por acción",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "${viewModel.remainingActionTime}s",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
