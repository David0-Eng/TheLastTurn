package com.example.thelastturn.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

    // Formatear la fecha/hora una vez
    val currentDateTime = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF8B4513)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo degradado “madera”
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()        // <-- ocupar toda la altura
                    .padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center  // <-- centrar verticalmente
            ) {
                // — Título según resultado —
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

                Spacer(Modifier.height(20.dp))

                // — Día y hora (readOnly) —
                OutlinedTextField(
                    value = currentDateTime,
                    onValueChange = {},
                    label = { Text("Día y hora", color = Color.White) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(Modifier.height(16.dp))

                // — Nombre del jugador (readOnly) —
                OutlinedTextField(
                    value = playerName,
                    onValueChange = {},
                    label = { Text("Nombre del jugador", color = Color.White) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(Modifier.height(16.dp))

                // — Medida del tablero (readOnly) —
                OutlinedTextField(
                    value = boardSize,
                    onValueChange = {},
                    label = { Text("Medida del tablero", color = Color.White) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(Modifier.height(16.dp))

                // — Campo para el e-mail del destinatario —
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Introduce tu email", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                )

                Spacer(Modifier.height(24.dp))

                // — Botón Enviar e-mail (abre la app de correo) —
                Button(
                    onClick = {
                        // Construir URI mailto:
                        val subject = Uri.encode("Resultados TheLastTurn: ${
                            when (result) {
                                "VICTORY" -> "Victoria"
                                "DEFEAT"  -> "Derrota"
                                else      -> "Empate"
                            }
                        }")
                        val body = Uri.encode("""
                            Fecha y hora: $currentDateTime
                            Jugador: $playerName
                            Tablero: $boardSize
                            Resultado: ${
                            when (result) {
                                "VICTORY" -> "¡Victoria!"
                                "DEFEAT"  -> "Derrota"
                                else      -> "Empate"
                            }
                        }
                        """.trimIndent())
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

                Spacer(Modifier.height(16.dp))

                // — Botón Nueva partida —
                Button(
                    onClick = onRestart,
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
                        "Nueva partida",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // — Botón Salir —
                Button(
                    onClick = onExit,
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
                        "Salir",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.SansSerif)
                    )
                }
            }
        }
    }
}
