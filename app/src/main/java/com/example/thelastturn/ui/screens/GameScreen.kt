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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.PlayerTurn
import com.example.thelastturn.viewmodel.GameViewModel



@Composable
fun isLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun GameScreen(onGameEnd: (String) -> Unit) {
    val viewModel: GameViewModel =
        viewModel(viewModelStoreOwner = LocalActivity.current as ViewModelStoreOwner)

    LaunchedEffect(viewModel.navigationEvent) {
        viewModel.navigationEvent?.let { result ->
            onGameEnd(result)
            viewModel.resetNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)),
        contentAlignment = Alignment.Center
    ) {
        if (isLandscape()) {
            LandscapeGame(viewModel, onGameEnd)
        } else {
            PortraitGame(viewModel)
        }
    }
}

@Composable
private fun LandscapeGame(viewModel: GameViewModel, onGameEnd: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Panel izquierdo para pantalla en horizontal
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerDisplay(viewModel)
            EnemyInfo(viewModel)
            PlayerInfo(viewModel)
            PlayerHand(
                cards = viewModel.playerHand.take(4),
                selectedCard = viewModel.selectedCard,
                onCardSelected = { viewModel.selectCard(it) }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Panel derecho (tablero) en la pantalla horizontal
        Column(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Enemigo:", style = MaterialTheme.typography.titleMedium)
            BoardSection(
                slots = viewModel.enemySlots,
                isPlayer = false,
                viewModel = viewModel,
                cardSpacing = 8.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Jugador:", style = MaterialTheme.typography.titleMedium)
            BoardSection(
                slots = viewModel.playerSlots,
                isPlayer = true,
                viewModel = viewModel,
                cardSpacing = 8.dp
            )
        }
    }
}

@Composable
private fun PortraitGame(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimerDisplay(viewModel)
        EnemyInfo(viewModel)
        BoardSection(slots = viewModel.enemySlots, isPlayer = false, viewModel = viewModel)
        BoardSection(slots = viewModel.playerSlots, isPlayer = true, viewModel = viewModel)
        PlayerInfo(viewModel)
        PlayerHand(
            cards = viewModel.playerHand.take(4),
            selectedCard = viewModel.selectedCard,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(viewModel.player.name, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            HealthIcons(viewModel.player.currentHits)
        }
        Text("Turno: ${viewModel.currentTurn.name}")
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
            AnimatedVisibility(visible = index < health) {
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val slotCount = slots.size.coerceAtLeast(1)
        val totalSpacing = cardSpacing * (slotCount - 1)
        val slotWidth = (maxWidth - totalSpacing) / slotCount

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing)
        ) {
            items(slots) { slot ->
                BoardSlotUI(
                    slot = slot,
                    isPlayer = isPlayer,
                    viewModel = viewModel,
                    width = slotWidth,
                    height = 120.dp
                )
            }
        }
    }
}

@Composable
private fun BoardSlotUI(
    slot: BoardSlot,
    isPlayer: Boolean,
    viewModel: GameViewModel,
    width: Dp = 73.dp,
    height: Dp = 120.dp
) {
    val slotColor = if (isPlayer) Color(0xFF2196F3) else Color(0xFFF44336)

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(1.5.dp, slotColor, RoundedCornerShape(8.dp))
            .clickable(enabled = isPlayer && viewModel.currentTurn == PlayerTurn.PLAYER) {
                viewModel.selectedCard?.let { viewModel.placeCard(slot.id) }
            }
            .background(slotColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = slot.card != null) {
            slot.card?.let { CardInSlot(card = it) }
        }
        if (slot.card == null) {
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
            modifier = Modifier
                .size(60.dp)
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
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
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
            .border(1.5.dp, color, RoundedCornerShape(10.dp))
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD2B48C),
            contentColor   = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            items(cards) { card ->
                CardUI(
                    card       = card,
                    isSelected = card.id == selectedCard?.id,
                    onClick    = { onCardSelected(card) }
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xAAFFFFFF).copy(alpha = 0.9f),
            contentColor   = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tiempo total", style = MaterialTheme.typography.labelSmall)
                Text("${viewModel.remainingTotalTime}s", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tiempo por acción", style = MaterialTheme.typography.labelSmall)
                Text("${viewModel.remainingActionTime}s", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

