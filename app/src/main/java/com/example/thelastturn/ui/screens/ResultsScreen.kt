package com.example.thelastturn.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.thelastturn.data.GameRepository // Import your GameRepository
import com.example.thelastturn.data.Partida // Import your Partida data class
import kotlinx.coroutines.launch // Needed for coroutine scope
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResultsScreen(
    playerName: String,
    result: String,
    boardSize: Int,
    gameLog: String,
    playerLives: Int,
    enemyLives: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onBackToHome: () -> Unit,
    gameRepository: GameRepository // NEW: Inject GameRepository
) {
    val validResult = when (result.uppercase(Locale.getDefault())) {
        "VICTORY", "DEFEAT", "DRAW", "TIEMPO AGOTADO" -> result.uppercase(Locale.getDefault()) // Added "TIEMPO AGOTADO"
        else -> "DRAW"
    }

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val displayBoardSize = remember(boardSize) { "${boardSize}x${boardSize}" }


    val fullGameLogText = remember(playerName, boardSize, playerLives, enemyLives, gameLog) {
        buildString {
            append("Jugador: $playerName\n")
            append("Tablero: $displayBoardSize\n")
            append("Vidas del jugador: $playerLives\n")
            append("Vidas del enemigo: $enemyLives\n")
            append("--- Registro de Acciones ---\n")
            append(gameLog)
        }
    }

    val currentDateTime = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val newPartida = Partida(
                id = UUID.randomUUID().toString(), // ID unica para la partida
                alias = playerName,
                fecha = currentDateTime,
                resultado = validResult,
                sizeTablero = boardSize,
                dmgInfligido = if (validResult == "VICTORY") 100 else 0,
                dmgRecibido = if (validResult == "DEFEAT") 100 else 0,
                cartasEliminadas = 0,
                logPartida = fullGameLogText // Guarda el log completo
            )
            gameRepository.insertPartida(newPartida)
        }
    }

    BackHandler {
        onBackToHome()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF8B4513)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                    )
                )
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isLandscape = maxWidth > maxHeight
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState), // Use the remembered scroll state
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            ResultHeader(validResult)
                            Spacer(Modifier.height(16.dp))
                            ReadOnlyField("Día y hora", currentDateTime)
                            Spacer(Modifier.height(12.dp))
                            // Display the game log in a scrollable Text, not an editable TextField
                            Text(
                                text = "Valores del Log:",
                                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 200.dp) // Adjust max height as needed
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState()) // Separate scroll for log
                            ) {
                                Text(
                                    text = fullGameLogText,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-mail destinatario", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = Color.Gray,
                                    cursorColor = Color.White
                                )
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SendEmailButton(
                                email = email,
                                result = validResult, // Use validResult
                                dateTime = currentDateTime,
                                playerName = playerName,
                                boardSize = displayBoardSize,
                                gameLog = fullGameLogText, // Send the full log
                                context = context
                            )
                            Spacer(Modifier.height(16.dp))
                            ActionButton("Nueva partida", onRestart)
                            Spacer(Modifier.height(16.dp))
                            ActionButton("Salir", onExit)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp)
                            .verticalScroll(scrollState), // Use the remembered scroll state
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ResultHeader(validResult) // Use validResult
                        Spacer(Modifier.height(20.dp))
                        ReadOnlyField("Día y hora", currentDateTime)
                        Spacer(Modifier.height(16.dp))

                        // Display the game log in a scrollable Text, not an editable TextField
                        Text(
                            text = "Valores del Log:",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 250.dp) // Adjust heights for portrait
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()) // Separate scroll for log
                        ) {
                            Text(
                                text = fullGameLogText,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail destinatario", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.Gray,
                                cursorColor = Color.White
                            )
                        )
                        Spacer(Modifier.height(24.dp))
                        SendEmailButton(
                            email = email,
                            result = validResult, // Use validResult
                            dateTime = currentDateTime,
                            playerName = playerName,
                            boardSize = displayBoardSize,
                            gameLog = fullGameLogText, // Send the full log
                            context = context
                        )
                        Spacer(Modifier.height(16.dp))
                        ActionButton("Nueva partida", onRestart)
                        Spacer(Modifier.height(16.dp))
                        ActionButton("Salir", onExit)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultHeader(result: String) {
    Text(
        text = when (result) {
            "VICTORY" -> "¡Victoria!"
            "DEFEAT"  -> "Derrota"
            "DRAW"    -> "Empate"
            "TIEMPO AGOTADO" -> "Tiempo Agotado" // Added
            else      -> "Resultado Desconocido" // Fallback
        },
        style = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = FontFamily.SansSerif,
            color = Color.White
        )
    )
}

@Composable
private fun ReadOnlyField(labelText: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(labelText, color = Color.White) },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            disabledBorderColor = Color.Gray, // For readOnly fields
            disabledLabelColor = Color.Gray,
            disabledTextColor = Color.White
        )
    )
}

@Composable
private fun SendEmailButton(
    email: String,
    result: String,
    dateTime: String,
    playerName: String,
    boardSize: String,
    gameLog: String,
    context: android.content.Context
) {
    Button(
        onClick = {
            val subject = Uri.encode(
                "Resultados TheLastTurn: ${
                    when (result) {
                        "VICTORY" -> "Victoria"
                        "DEFEAT"  -> "Derrota"
                        "DRAW"    -> "Empate"
                        "TIEMPO AGOTADO" -> "Tiempo Agotado"
                        else      -> "Desconocido"
                    }
                } – $dateTime"
            )
            val bodyText = buildString {
                append("Fecha y hora: $dateTime\n")
                append("Jugador: $playerName\n")
                append("Tablero: $boardSize\n\n")
                append("Log de la partida:\n$gameLog")
                append("\n\nResultado Final: ${
                    when (result) {
                        "VICTORY" -> "¡Victoria!"
                        "DEFEAT"  -> "Derrota"
                        "DRAW"    -> "Empate"
                        "TIEMPO AGOTADO" -> "Tiempo Agotado"
                        else      -> "Desconocido"
                    }
                }\n")
            }
            val body = Uri.encode(bodyText)
            val mailUri = Uri.parse("mailto:$email?subject=$subject&body=$body")
            val intent = Intent(Intent.ACTION_SENDTO, mailUri)
            // Ensure the intent can be resolved to prevent crashes if no email app is found
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Optionally show a Toast or Snackbar if no email app is available
                // Toast.makeText(context, "No hay aplicación de correo disponible.", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFFA0522D), RoundedCornerShape(12.dp)), // Added background color here
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Transparent because background is already set
            contentColor = Color.White
        )
    ) {
        Text(
            "Enviar e-mail",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif)
        )
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color(0xFFA0522D), RoundedCornerShape(12.dp)), // Added background color here
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent, // Transparent because background is already set
            contentColor = Color.White
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif)
        )
    }
}