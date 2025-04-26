package com.example.thelastturn.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thelastturn.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    onStartGame: (playerName: String, numSlots: Int, totalTime: Int, actionTime: Int) -> Unit
) {
    val owner = LocalActivity.current as ViewModelStoreOwner
    val viewModel: GameViewModel = viewModel(viewModelStoreOwner = owner)

    var playerName by remember { mutableStateOf("") }
    var numSlots   by remember { mutableStateOf(4) }
    var totalTime  by remember { mutableStateOf("") }
    var actionTime by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Nombre del jugador") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text("Número de cartas por tablero:")
        Row(horizontalArrangement = Arrangement.Center) {
            listOf(3,4,5).forEach { n ->
                RadioButton(
                    selected = numSlots == n,
                    onClick  = { numSlots = n }
                )
                Text("$n")
                Spacer(Modifier.width(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = totalTime,
            onValueChange = {
                // Solo dígitos y hasta 3 caracteres
                if (it.all { c -> c.isDigit() } && it.length <= 3) {
                    totalTime = it
                }
            },
            label = { Text("Tiempo total (segundos, max. 999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = actionTime,
            onValueChange = {
                // Solo dígitos y hasta 3 caracteres
                if (it.all { c -> c.isDigit() } && it.length <= 3) {
                    actionTime = it
                }
            },
            label = { Text("Tiempo por acción (segundos, max. 999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (playerName.isNotBlank() && totalTime.isNotBlank() && actionTime.isNotBlank()) {
                    // Convertir y truncar a 999 si se excede
                    val totalSec  = (totalTime.toIntOrNull() ?: 0).coerceAtMost(999)
                    val actionSec = (actionTime.toIntOrNull() ?: 0).coerceAtMost(999)

                    onStartGame(
                        playerName,
                        numSlots,
                        totalSec,
                        actionSec
                    )
                } else {
                    // Aquí podrías mostrar un Toast o Snackbar en lugar de println
                    println("Completa todos los campos")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Empezar")
        }
    }
}
