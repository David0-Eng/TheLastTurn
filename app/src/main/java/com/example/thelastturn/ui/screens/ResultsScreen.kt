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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResultsScreen(
    playerName: String,
    result: String,
    boardSize: String,
    gameLog: String,
    playerLives: Int,
    enemyLives: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var logText by remember {
        mutableStateOf(
            buildString {
                append("Jugador: $playerName\n")    // Aquí añado el nombre del jugador al log para hacerlo más fácil
                append("Tablero: $boardSize\n")   // Aquí añado el tamaño del tablero
                append("Vidas del jugador: $playerLives\n")   // 👈 Añadido
                append("Vidas del enemigo: $enemyLives\n")
                append(gameLog)
            }
        )
    }
    val currentDateTime = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
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
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            ResultHeader(result)
                            Spacer(Modifier.height(16.dp))
                            ReadOnlyField("Día y hora", currentDateTime)
                            Spacer(Modifier.height(12.dp))
                            // Campo log
                            OutlinedTextField(
                                value = logText,
                                onValueChange = { logText = it },
                                label = { Text("Valores del Log", color = Color.White) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                singleLine = false,
                                maxLines = 8,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-mail destinatario", color = Color.White) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
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
                                result = result,
                                dateTime = currentDateTime,
                                playerName = playerName,
                                boardSize = boardSize,
                                gameLog = logText,
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
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ResultHeader(result)
                        Spacer(Modifier.height(20.dp))
                        ReadOnlyField("Día y hora", currentDateTime)
                        Spacer(Modifier.height(16.dp))

                        // Campo log
                        ReadOnlyField(
                            labelText = "Valores del Log",
                            value = logText
                        )

                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail destinatario", color = Color.White) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Spacer(Modifier.height(24.dp))
                        SendEmailButton(
                            email = email,
                            result = result,
                            dateTime = currentDateTime,
                            playerName = playerName,
                            boardSize = boardSize,
                            gameLog = logText,
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
            else      -> "Empate"
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
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
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
                        else      -> "Empate"
                    }
                } – $dateTime"
            )
            val bodyText = buildString {
                append("Fecha y hora: $dateTime\n\n")
                append("Log de la partida:\n$gameLog")
                append("\nResultado: ${
                    when (result) {
                        "VICTORY" -> "¡Victoria!"
                        "DEFEAT"  -> "Derrota"
                        else      -> "Empate"
                    }
                }\n")
            }
            val body = Uri.encode(bodyText)
            val mailUri = Uri.parse("mailto:$email?subject=$subject&body=$body")
            val intent = Intent(Intent.ACTION_SENDTO, mailUri)
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFA0522D),
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
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFA0522D),
            contentColor = Color.White
        )
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif)
        )
    }
}