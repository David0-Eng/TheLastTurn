package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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

    var playerName by rememberSaveable { mutableStateOf("") }
    var numSlots   by rememberSaveable { mutableStateOf(4) }
    var totalTime  by rememberSaveable { mutableStateOf("") }
    var actionTime by rememberSaveable { mutableStateOf("") }
    var submitted  by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    // Adjust width fraction and spacing for landscape vs portrait
    val widthFraction = if (isLandscape) 0.6f else 1f
    val smallSpacer = if (isLandscape) 8.dp else 16.dp
    val mediumSpacer = if (isLandscape) 12.dp else 16.dp
    val largeSpacer = if (isLandscape) 16.dp else 24.dp

    // Validation flags
    val isFormValid = playerName.isNotBlank() && totalTime.isNotBlank() && actionTime.isNotBlank()
    val nameError   = submitted && playerName.isBlank()
    val totalError  = submitted && totalTime.isBlank()
    val actionError = submitted && actionTime.isBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isLandscape) Arrangement.Top else Arrangement.Center
    ) {
        // Nombre del jugador
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Nombre del jugador") },
            isError = nameError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(widthFraction)
        )
        if (nameError) {
            Text(
                text = "Este campo es obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(smallSpacer))

        // Número de cartas por tablero
        Text(
            text = "Número de cartas por tablero:",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(mediumSpacer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(3, 4, 5).forEach { n ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { numSlots = n }
                        .padding(4.dp)
                ) {
                    RadioButton(
                        selected = numSlots == n,
                        onClick = { numSlots = n }
                    )
                    Text(
                        text = "$n",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(smallSpacer))

        // Tiempo total
        OutlinedTextField(
            value = totalTime,
            onValueChange = {
                if (it.all { c -> c.isDigit() } && it.length <= 3) totalTime = it
            },
            label = { Text("Tiempo total (segundos, max. 999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = totalError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(widthFraction)
        )
        if (totalError) {
            Text(
                text = "Este campo es obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(smallSpacer))

        // Tiempo por acción
        OutlinedTextField(
            value = actionTime,
            onValueChange = {
                if (it.all { c -> c.isDigit() } && it.length <= 3) actionTime = it
            },
            label = { Text("Tiempo por acción (segundos, max. 999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = actionError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(widthFraction)
        )
        if (actionError) {
            Text(
                text = "Este campo es obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(largeSpacer))

        // Botón iniciar
        Button(
            onClick = {
                submitted = true
                if (isFormValid) {
                    val totalSec  = (totalTime.toIntOrNull() ?: 0).coerceAtMost(999)
                    val actionSec = (actionTime.toIntOrNull() ?: 0).coerceAtMost(999)
                    onStartGame(playerName, numSlots, totalSec, actionSec)
                }
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .padding(bottom = if (isLandscape) 16.dp else 0.dp)
        ) {
            Text("Empezar")
        }
    }
}
