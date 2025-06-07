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
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import kotlinx.coroutines.flow.Flow


// Helper function (if not already defined elsewhere)
@Composable
fun isTabletDevice(): Boolean {
    val configuration = LocalConfiguration.current
    // A common breakpoint for compact vs medium/expanded in Jetpack Compose's WindowSizeClasses
    // is around 600dp. Devices wider than this are often considered tablets for UI purposes.
    return configuration.screenWidthDp.dp >= 600.dp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGamesScreen(
    gameRepository: GameRepository, // Se inyecta el repositorio para acceder a los datos
    navController: NavController,   // NavController para la navegación en smartphones
    onBack: () -> Unit
) {
    // Observa todas las partidas desde el repositorio
    val allPartidas by gameRepository.allPartidas.collectAsState(initial = emptyList())
    var selectedPartida by remember { mutableStateOf<Partida?>(null) }

    // Determina la clase de tamaño de ventana para el responsive layout
    // Opcional: puedes usar calculateWindowSizeClass() si estás usando WindowSizeClass para más granularidad
    // val windowSizeClass = calculateWindowSizeClass(activity = LocalContext.current as Activity)
    val isTablet = isTabletDevice() // Usa tu helper para simplificar el ejemplo
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                if (isTablet && isLandscape) {
                    // Tablet Landscape (Bi-panel: Lista a la izquierda, Detalle a la derecha)
                    Row(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            PartidaList(allPartidas = allPartidas) { partida ->
                                selectedPartida = partida
                            }
                        }
                        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            if (selectedPartida != null) {
                                PartidaDetail(partida = selectedPartida!!)
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
                } else if (isTablet && !isLandscape) {
                    // Tablet Portrait (Bi-panel: Lista arriba, Detalle abajo)
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            PartidaList(allPartidas = allPartidas) { partida ->
                                selectedPartida = partida
                            }
                        }
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (selectedPartida != null) {
                                PartidaDetail(partida = selectedPartida!!)
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
                    // Smartphone (Mono-panel: Solo lista, navegación a otra pantalla para el detalle)
                    PartidaList(allPartidas = allPartidas) { partida ->
                        // Navegar a la pantalla de detalle con los datos de la partida
                        // Como Partida es Parcelable, podemos pasarla directamente como argumento
                        navController.currentBackStackEntry?.arguments?.putParcelable("partidaDetail", partida)
                        navController.navigate("past_game_detail")
                    }
                }
            }
        }
    )
}

@Composable
fun PartidaList(allPartidas: List<Partida>, onPartidaClick: (Partida) -> Unit) {
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
            items(allPartidas) { partida ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPartidaClick(partida) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5DC).copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Alias: ${partida.alias}", fontWeight = FontWeight.Bold)
                        Text("Fecha: ${partida.fecha}")
                        Text("Resultado: ${partida.resultado}", fontWeight = FontWeight.Bold,
                            color = when (partida.resultado) {
                                "VICTORY" -> Color.Green.copy(alpha = 0.8f)
                                "DEFEAT" -> Color.Red.copy(alpha = 0.8f)
                                "DRAW" -> Color.Blue.copy(alpha = 0.8f)
                                "TIEMPO AGOTADO" -> Color.Magenta.copy(alpha = 0.8f) // Ajusta si tienes este resultado
                                else -> Color.Black
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PartidaDetail(partida: Partida) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Detalle de Partida", style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.SansSerif, color = Color(0xFF8B4513)))
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        Text("ID Partida: ${partida.id.take(8)}...") // Mostrar solo una parte del ID
        Text("Alias: ${partida.alias}")
        Text("Fecha: ${partida.fecha}")
        Text("Resultado: ${partida.resultado}",
            color = when (partida.resultado) {
                "VICTORY" -> Color.Green.copy(alpha = 0.8f)
                "DEFEAT" -> Color.Red.copy(alpha = 0.8f)
                "DRAW" -> Color.Blue.copy(alpha = 0.8f)
                "TIEMPO AGOTADO" -> Color.Magenta.copy(alpha = 0.8f)
                else -> Color.Black
            }
        )
        Text("Tamaño Tablero: ${partida.sizeTablero}")
        Text("Daño Infligido: ${partida.dmgInfligido}")
        Text("Daño Recibido: ${partida.dmgRecibido}")
        Text("Cartas Eliminadas: ${partida.cartasEliminadas}")
        // Aquí puedes añadir más detalles si tu clase Partida los tiene y los necesitas mostrar
        // Por ejemplo, si tu juego fuera Buscaminas, añadirías:
        // Text("Nº Casillas Escogidas: ${partida.casillasEscogidas}")
        // Text("Nº Minas: ${partida.numMinas}")
        // Text("Tiempo Total Empleado: ${partida.tiempoTotal}")
        // Text("Casilla Bomba: (${partida.bombaX}, ${partida.bombaY})")
    }
}

// Para smartphones, necesitas una pantalla de detalle separada
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastGameDetailScreen(
    navController: NavController
) {
    // Recupera la partida del argumento de navegación
    val partida = navController.previousBackStackEntry?.arguments?.getParcelable<Partida>("partidaDetail")

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
                    PartidaDetail(partida = partida)
                } else {
                    Text("Error: No se pudo cargar el detalle de la partida.")
                }
            }
        }
    )
}