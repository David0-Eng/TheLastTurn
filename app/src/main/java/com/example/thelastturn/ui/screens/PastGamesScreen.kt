package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.thelastturn.data.GameRepository
import com.example.thelastturn.data.Partida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import com.example.thelastturn.ui.components.isTabletDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGamesScreen(
    gameRepository: GameRepository,
    navController: NavController,
    onBack: () -> Unit
) {
    val allPartidas by gameRepository.allPartidas.collectAsState(initial = emptyList())
    var selectedPartida by remember { mutableStateOf<Partida?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val isTablet = isTabletDevice()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val onDeletePartida: (Partida) -> Unit = { partidaToDelete ->
        coroutineScope.launch {
            gameRepository.deletePartida(partidaToDelete)
            if (selectedPartida == partidaToDelete) {
                selectedPartida = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partidas Anteriores", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFA0522D),
                    titleContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isTablet) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                PartidaList(
                                    allPartidas = allPartidas,
                                    onPartidaClick = { partida -> selectedPartida = partida },
                                    onDeleteClick = onDeletePartida
                                )
                            }
                            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                if (selectedPartida != null) {
                                    PartidaDetailContent(partida = selectedPartida!!)
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Selecciona una partida para ver los detalles",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                PartidaList(
                                    allPartidas = allPartidas,
                                    onPartidaClick = { partida -> selectedPartida = partida },
                                    onDeleteClick = onDeletePartida
                                )
                            }
                            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                if (selectedPartida != null) {
                                    PartidaDetailContent(partida = selectedPartida!!)
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Selecciona una partida para ver los detalles",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    PartidaList(
                        allPartidas = allPartidas,
                        onPartidaClick = { partida ->
                            navController.navigate("past_game_detail/${partida.id}")
                        },
                        onDeleteClick = onDeletePartida
                    )
                }
            }
        }
    )
}

@Composable
fun PartidaList(
    allPartidas: List<Partida>,
    onPartidaClick: (Partida) -> Unit,
    onDeleteClick: (Partida) -> Unit
) {
    if (allPartidas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay partidas registradas aún.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allPartidas, key = { it.id }) { partida ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPartidaClick(partida) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC).copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alias: ${partida.alias}", fontWeight = FontWeight.Bold)
                            Text("Fecha: ${partida.fecha}")
                            Text("Resultado: ${partida.resultado}", fontWeight = FontWeight.Bold,
                                color = when (partida.resultado) {
                                    "Victoria" -> Color.Green.copy(alpha = 0.8f)
                                    "Derrota" -> Color.Red.copy(alpha = 0.8f)
                                    "Empate" -> Color.Blue.copy(alpha = 0.8f)
                                    "Tiempo Agotado" -> Color.Magenta.copy(alpha = 0.8f)
                                    else -> Color.Black
                                }
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClick(partida) },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar partida",
                                tint = Color.Red.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartidaDetailContent(partida: Partida) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Detalle de Partida", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, color = Color(0xFF8B4513)))
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        Text("ID Partida: ${partida.id.take(8)}...")
        Text("Alias: ${partida.alias}")
        Text("Fecha: ${partida.fecha}")
        Text("Resultado: ${partida.resultado}",
            color = when (partida.resultado) {
                "Victoria" -> Color.Green.copy(alpha = 0.8f)
                "Derrota" -> Color.Red.copy(alpha = 0.8f)
                "Empate" -> Color.Blue.copy(alpha = 0.8f)
                "Tiempo Agotado" -> Color.Magenta.copy(alpha = 0.8f)
                else -> Color.Black
            }
        )
        Text("Tamaño Tablero: ${partida.sizeTablero}")
        Text("Daño Infligido: ${partida.dmgInfligido}")
        Text("Daño Recibido: ${partida.dmgRecibido}")
        Text("Cartas Eliminadas: ${partida.cartasEliminadas}")

        Text(
            "Log de Partida:\n${partida.logPartida}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 250.dp)
                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGameDetailScreen(
    navController: NavController,
    gameRepository: GameRepository,
    partidaId: String?
) {
    val partida by remember(partidaId) {
        if (partidaId != null) {
            gameRepository.getPartidaById(partidaId)
        } else {
            MutableStateFlow(null)
        }
    }.collectAsState(initial = null)

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Partida", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (partida != null) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                gameRepository.deletePartida(partida!!)
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar esta partida",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFA0522D),
                    titleContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (partida != null) {
                    PartidaDetailContent(partida = partida!!)
                } else {
                    Text(
                        "Cargando detalles de la partida o partida no encontrada.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }
    )
}