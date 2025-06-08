package com.example.thelastturn.ui.screens

import android.content.Intent
import android.widget.Toast
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
import com.example.thelastturn.data.GameRepository
import com.example.thelastturn.ui.components.AppActionButton
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
    onStartNewGame: () -> Unit,
    onExit: () -> Unit,
    onBackToHome: () -> Unit,
    gameRepository: GameRepository
) {
    val validResult = when (result.uppercase(Locale.getDefault())) {
        "VICTORY", "DEFEAT", "DRAW", "TIEMPO AGOTADO" -> result.uppercase(Locale.getDefault())
        else -> "DRAW"
    }

    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val displayBoardSize = "${boardSize}x$boardSize"

    val fullGameLogText = buildString {
        append("Jugador: $playerName\n")
        append("Tablero: $displayBoardSize\n")
        append("Vidas del jugador: $playerLives\n")
        append("Vidas del enemigo: $enemyLives\n")
        append("--- Registro de Acciones ---\n")
        append(gameLog)
    }

    val currentDateTime = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
    }

    BackHandler { onBackToHome() }

    val sendEmail = {
        val subject = "Resultados TheLastTurn: ${
            when (validResult) {
                "VICTORY" -> "Victoria"
                "DEFEAT" -> "Derrota"
                "DRAW" -> "Empate"
                "TIEMPO AGOTADO" -> "Tiempo Agotado"
                else -> "Desconocido"
            }
        } – $currentDateTime"

        val bodyText = buildString {
            append("Fecha y hora: $currentDateTime\n")
            append("Jugador: $playerName\n")
            append("Tablero: $displayBoardSize\n\n")
            append("Log de la partida:\n$fullGameLogText")
            append("\n\nResultado Final: ${
                when (validResult) {
                    "VICTORY" -> "¡Victoria!"
                    "DEFEAT" -> "Derrota"
                    "DRAW" -> "Empate"
                    "TIEMPO AGOTADO" -> "Tiempo Agotado"
                    else -> "Desconocido"
                }
            }\n")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, bodyText)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar correo usando..."))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se encontró una aplicación de correo compatible.",
                Toast.LENGTH_SHORT
            ).show()
        }
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
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            ResultHeader(validResult)
                            Spacer(Modifier.height(16.dp))
                            ReadOnlyField("Día y hora", currentDateTime)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Valores del Log:",
                                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp, max = 200.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
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
                            AppActionButton("Enviar e-mail", onClick = sendEmail, enabled = email.isNotBlank())
                            Spacer(Modifier.height(16.dp))
                            AppActionButton("Nueva partida", onStartNewGame)
                            Spacer(Modifier.height(16.dp))
                            AppActionButton("Volver al inicio", onBackToHome)
                            Spacer(Modifier.height(16.dp))
                            AppActionButton("Salir", onExit)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(30.dp)
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ResultHeader(validResult)
                        Spacer(Modifier.height(20.dp))
                        ReadOnlyField("Día y hora", currentDateTime)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Valores del Log:",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 250.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
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
                        AppActionButton("Enviar e-mail", onClick = sendEmail, enabled = email.isNotBlank())
                        Spacer(Modifier.height(16.dp))
                        AppActionButton("Nueva partida", onStartNewGame)
                        Spacer(Modifier.height(16.dp))
                        AppActionButton("Volver al inicio", onBackToHome)
                        Spacer(Modifier.height(16.dp))
                        AppActionButton("Salir", onExit)
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
            "DEFEAT" -> "Derrota"
            "DRAW" -> "Empate"
            "TIEMPO AGOTADO" -> "Tiempo Agotado"
            else -> "Resultado Desconocido"
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
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.Gray,
            disabledTextColor = Color.White
        )
    )
}