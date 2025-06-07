package com.example.thelastturn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.thelastturn.data.PreferencesManager // Importa tu PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    // Observa el nombre de jugador actual desde las preferencias
    val playerName by preferencesManager.playerName.collectAsState(initial = "")
    var newPlayerName by remember { mutableStateOf(playerName) }

    // Actualiza newPlayerName cuando playerName cambia (ej. al cargar por primera vez)
    LaunchedEffect(playerName) {
        newPlayerName = playerName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", color = Color.White) },
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
                    containerColor = Color(0xFFA0522D), // Color de tu tema
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Preferencias de Usuario",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.SansSerif,
                            color = Color(0xFF8B4513)
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = newPlayerName,
                        onValueChange = { newPlayerName = it },
                        label = { Text("Tu Alias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA0522D),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFFA0522D),
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                preferencesManager.savePlayerName(newPlayerName)
                                onBack() // Volver a la pantalla anterior después de guardar
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(60.dp)
                            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(Color(0xFFA0522D), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Guardar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(60.dp)
                            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                    }
                }
            }
        }
    )
}