package com.example.thelastturn.ui.screens

import android.app.Application
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
import androidx.compose.ui.platform.LocalContext // ¡Importación necesaria para LocalContext!
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thelastturn.data.AppDatabase // ¡Importación necesaria para la base de datos!
import com.example.thelastturn.data.GameRepository // ¡Importación necesaria para el repositorio!
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.PlayerTurn
import com.example.thelastturn.viewmodel.GameViewModel
import com.example.thelastturn.viewmodel.GameViewModelFactory // ¡Importación necesaria para la fábrica!

// Helper function to determine if the device is likely a tablet (based on screen width)
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    // A common breakpoint for compact vs medium/expanded in Jetpack Compose's WindowSizeClasses
    // is around 600dp. Devices wider than this are often considered tablets for UI purposes.
    return configuration.screenWidthDp.dp >= 600.dp
}

// Helper function to check landscape orientation
@Composable
fun isLandscape(): Boolean {
    return LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun GameScreen(onGameEnd: (String) -> Unit) {
    // Obtener el contexto local y la instancia de Application
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Obtener la instancia de la base de datos y el repositorio
    // Usamos remember para que estas instancias se mantengan durante las recomposiciones
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { GameRepository(database.partidaDao()) }

    // Crear la fábrica del ViewModel con las dependencias necesarias
    val factory = remember { GameViewModelFactory(application, repository) }

    // Obtener la instancia del ViewModel, proporcionando la fábrica
    val viewModel: GameViewModel =
        viewModel(viewModelStoreOwner = LocalActivity.current as ViewModelStoreOwner, factory = factory)

    // Observe navigation events from ViewModel to trigger game end navigation
    // viewModel.navigationEvent es State<String?>, por lo que .value es String?
    LaunchedEffect(viewModel.navigationEvent) {
        viewModel.navigationEvent?.let { result -> // 'result' se infiere correctamente como String
            onGameEnd(result) // Navigate to the game end screen
            viewModel.resetNavigation() // Reset the event so it doesn't trigger again
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)), // Light beige background for the game
        contentAlignment = Alignment.Center
    ) {
        if (isTablet()) {
            // Tablet layout: Bi-panel, adapts to orientation
            TabletGameLayout(viewModel = viewModel)
        } else {
            // Smartphone layout: Mono-panel
            SmartphoneGameLayout(viewModel = viewModel)
        }
    }
}

/**
 * Composable for tablet-specific game layout (bi-panel).
 * It dynamically adjusts based on orientation (landscape or portrait).
 */
@Composable
private fun TabletGameLayout(viewModel: GameViewModel) {
    if (isLandscape()) {
        // Tablet Landscape: Game board on left (main content), Log on right (secondary content)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Game Content (Board, Players, Hand, Timer) - occupies more space
            Column(
                modifier = Modifier.weight(2f), // Wider for game content
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameContent(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }

            // Secondary Panel: Game Log
            Column(
                modifier = Modifier
                    .weight(1f) // Narrower for log
                    .fillMaxHeight()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                GameLogDisplay(viewModel = viewModel)
            }
        }
    } else {
        // Tablet Portrait: Game board on top (main content), Log on bottom (secondary content)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Game Content (Board, Players, Hand, Timer) - occupies more space
            Column(
                modifier = Modifier.weight(2f), // Taller for game content
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameContent(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }

            // Secondary Panel: Game Log
            Column(
                modifier = Modifier
                    .weight(1f) // Shorter for log
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                GameLogDisplay(viewModel = viewModel)
            }
        }
    }
}

/**
 * Composable for smartphone-specific game layout (mono-panel).
 * The log is not displayed here.
 */
@Composable
private fun SmartphoneGameLayout(viewModel: GameViewModel) {
    // Smartphone: Always mono-panel, log is not shown.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()), // Make it scrollable for smaller screens
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameContent(viewModel = viewModel, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * Contains the core game UI elements (Timer, Player/Enemy Info, Board, Player Hand).
 * This composable can be reused for both tablet and smartphone layouts.
 */
@Composable
private fun GameContent(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscapeOrientation = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTabletDevice = isTablet() // Determine if it's a tablet for specific layout adjustments

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround // Adjust as needed for spacing
    ) {
        TimerDisplay(viewModel)
        Spacer(modifier = Modifier.height(16.dp))

        // Arrange player/enemy info and board based on orientation or device type
        // This condition creates the side-by-side layout for board sections only in tablet landscape
        if (isLandscapeOrientation && isTabletDevice) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EnemyInfo(viewModel)
                    BoardSection(slots = viewModel.enemySlots, isPlayer = false, viewModel = viewModel, cardSpacing = 8.dp)
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlayerInfo(viewModel)
                    BoardSection(slots = viewModel.playerSlots, isPlayer = true, viewModel = viewModel, cardSpacing = 8.dp)
                }
            }
        } else { // Default layout for portrait and smartphones (top to bottom)
            EnemyInfo(viewModel)
            BoardSection(slots = viewModel.enemySlots, isPlayer = false, viewModel = viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            PlayerInfo(viewModel)
            BoardSection(slots = viewModel.playerSlots, isPlayer = true, viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))
        PlayerHand(
            cards = viewModel.playerHand.take(4), // Display up to 4 cards in hand
            selectedCard = viewModel.selectedCard,
            onCardSelected = { viewModel.selectCard(it) }
        )
    }
}

/**
 * Displays the game log entries.
 */
@Composable
private fun GameLogDisplay(viewModel: GameViewModel) {
    val gameLog by viewModel.gameLog.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(4.dp)
    ) {
        Text("Registro de Partida", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        if (gameLog.isBlank()) {
            Text("El log aparecerá aquí a medida que juegues.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            // Display the log content
            Text(gameLog, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}

@Composable
private fun PlayerInfo(viewModel: GameViewModel) {
    // Observe the player alias from the ViewModel's Flow
    val playerAlias by viewModel.playerAlias.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // Align items vertically in center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(playerAlias, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) // Display observed alias
            Spacer(modifier = Modifier.width(8.dp))
            HealthIcons(viewModel.player.currentHits)
        }
        // Ensure that `currentTurn` is accessed with `.value`
        Text("Turno: ${viewModel.currentTurn.name}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EnemyInfo(viewModel: GameViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enemigo:", style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            HealthIcons(viewModel.enemy.currentHits)
        }
        Text("Cartas: ${viewModel.enemyHand.size}", style = MaterialTheme.typography.bodyMedium)
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
        // Calculate max width for slots considering spacing
        val maxSlotWidth = (maxWidth - (cardSpacing * (slotCount - 1))) / slotCount

        // Ensure slot width is not too small or too large, provide a reasonable range
        val idealSlotWidth = 73.dp // Original ideal width
        val calculatedSlotWidth = maxSlotWidth.coerceIn(50.dp, 100.dp) // Constrain size

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
            verticalAlignment = Alignment.CenterVertically // Center cards vertically in the row
        ) {
            items(slots, key = { it.id }) { slot -> // Added key for better performance in LazyRow
                BoardSlotUI(
                    slot = slot,
                    isPlayer = isPlayer,
                    viewModel = viewModel,
                    width = calculatedSlotWidth, // Use calculated width
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
    val slotColor = if (isPlayer) Color(0xFF2196F3) else Color(0xFFF44336) // Blue for player, Red for enemy

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(1.5.dp, slotColor, RoundedCornerShape(8.dp))
            .clickable(enabled = isPlayer && viewModel.currentTurn == PlayerTurn.PLAYER && viewModel.selectedCard != null) {
                // Only allow placing card if it's player's turn and a card is selected
                viewModel.placeCard(slot.id)
            }
            .background(slotColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Animate visibility of the card within the slot
        AnimatedVisibility(visible = slot.card != null) {
            slot.card?.let { CardInSlot(card = it) }
        }
        // Show placeholder if slot is empty
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
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)), // Clip image for rounded corners
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
            // Damage icon and text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔", style = MaterialTheme.typography.bodySmall, color = Color.White) // Changed text color for better contrast
                Text("${card.damage}", style = MaterialTheme.typography.bodyMedium, color = Color.White) // Changed text color
            }
            // Health icon and text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❤", style = MaterialTheme.typography.bodySmall, color = Color.White) // Changed text color
                Text("${card.health}/${card.maxHealth}", style = MaterialTheme.typography.bodyMedium, color = Color.White) // Changed text color
            }
        }
    }
}

@Composable
private fun HealthBar(health: Int, maxHealth: Int) {
    val animatedHealth by animateFloatAsState(
        targetValue = health.toFloat(),
        label = "HealthAnimation" // Added label for animation
    )
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
            containerColor = Color(0xFFD2B48C), // A warm, sandy brown color
            contentColor   = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically // Center cards vertically within the hand
        ) {
            items(cards, key = { it.id }) { card -> // Use key for better performance in LazyRow
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "CardScaleAnimation" // Added label for animation
    )

    Box(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale) // Apply scale transform
            .width(80.dp)
            .height(120.dp)
            .border(
                width = 3.dp, // Slightly thicker border for selected card
                color = if (isSelected) Color.Green else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .background(Color.White, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)) // Clip content to rounded corners
    ) {
        CardInSlot(card = card) // Reuse CardInSlot for consistency
    }
}

@Composable
private fun TimerDisplay(viewModel: GameViewModel) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .background(Color.Transparent), // Set background transparent if desired
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xAAFFFFFF).copy(alpha = 0.9f), // Semi-transparent white
            contentColor   = Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No shadow if elevation is 0
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tiempo total", style = MaterialTheme.typography.labelSmall)
                Text("${viewModel.remainingTotalTime}s", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Spacer(Modifier.width(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tiempo por acción", style = MaterialTheme.typography.labelSmall)
                Text("${viewModel.remainingActionTime}s", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}
