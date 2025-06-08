package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.thelastturn.data.PreferencesManager
import com.example.thelastturn.ui.components.AppActionButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onSaveAndBack: (playerName: String, numSlots: Int, totalTime: Int, actionTime: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    val initialPlayerName by preferencesManager.playerName.collectAsState(initial = "")
    val initialNumSlots by preferencesManager.numSlots.collectAsState(initial = 4)
    val initialTotalTime by preferencesManager.totalTime.collectAsState(initial = 180)
    val initialActionTime by preferencesManager.actionTime.collectAsState(initial = 30)

    var playerName by rememberSaveable { mutableStateOf("") }
    var numSlots by rememberSaveable { mutableStateOf(4) }
    var totalTime by rememberSaveable { mutableStateOf("") }
    var actionTime by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialPlayerName, initialNumSlots, initialTotalTime, initialActionTime) {
        if (playerName.isEmpty() && initialPlayerName.isNotEmpty()) {
            playerName = initialPlayerName
        }
        numSlots = initialNumSlots
        totalTime = initialTotalTime.toString()
        actionTime = initialActionTime.toString()
    }

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val contentWidthFraction = if (isLandscape) 0.7f else 0.9f
    val horizontalPadding = 24.dp
    val verticalPadding = 32.dp

    val smallSpacer = 12.dp
    val mediumSpacer = 20.dp
    val largeSpacer = 30.dp

    val isFormValid = playerName.isNotBlank() && totalTime.isNotBlank() && actionTime.isNotBlank() &&
            (totalTime.toIntOrNull() != null && totalTime.toIntOrNull()!! > 0) &&
            (actionTime.toIntOrNull() != null && actionTime.toIntOrNull()!! > 0)

    val nameError = submitted && playerName.isBlank()
    val totalError = submitted && (totalTime.isBlank() || totalTime.toIntOrNull() == null || totalTime.toIntOrNull()!! <= 0)
    val actionError = submitted && (actionTime.isBlank() || actionTime.toIntOrNull() == null || actionTime.toIntOrNull()!! <= 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferencias y Configuración", color = Color.White) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFD2B48C), Color(0xFF8B4513))
                        )
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Configuración de Partida",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = largeSpacer)
                )

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Tu Alias de Jugador") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(contentWidthFraction)
                        .padding(bottom = mediumSpacer),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA0522D),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color(0xFFA0522D),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
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

                Text(
                    text = "Número de cartas por tablero:",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(contentWidthFraction)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(3, 4, 5).forEach { n ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { numSlots = n }
                                .weight(1f)
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = numSlots == n,
                                onClick = { numSlots = n },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFA0522D),
                                    unselectedColor = Color.White
                                )
                            )
                            Text(
                                text = "${n}x$n",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(largeSpacer))

                OutlinedTextField(
                    value = totalTime,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() } && it.length <= 3) totalTime = it
                    },
                    label = { Text("Tiempo total (segundos, max. 999)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = totalError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(contentWidthFraction)
                        .padding(bottom = mediumSpacer),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA0522D),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color(0xFFA0522D),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                if (totalError) {
                    Text(
                        text = if (totalTime.isBlank()) "Este campo es obligatorio" else "El tiempo debe ser un número positivo",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
                Spacer(Modifier.height(smallSpacer))

                OutlinedTextField(
                    value = actionTime,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() } && it.length <= 3) actionTime = it
                    },
                    label = { Text("Tiempo por acción (segundos, max. 999)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = actionError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth(contentWidthFraction)
                        .padding(bottom = largeSpacer),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA0522D),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color(0xFFA0522D),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                if (actionError) {
                    Text(
                        text = if (actionTime.isBlank()) "Este campo es obligatorio" else "El tiempo debe ser un número positivo",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
                Spacer(Modifier.height(smallSpacer))

                AppActionButton(
                    text = "Guardar",
                    onClick = {
                        submitted = true
                        if (isFormValid) {
                            val totalSec = (totalTime.toIntOrNull() ?: 0).coerceAtMost(999)
                            val actionSec = (actionTime.toIntOrNull() ?: 0).coerceAtMost(999)

                            scope.launch {
                                preferencesManager.savePlayerName(playerName)
                                preferencesManager.saveGameSettings(numSlots, totalSec, actionSec)
                                onSaveAndBack(playerName, numSlots, totalSec, actionSec)
                            }
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth(contentWidthFraction)
                )

                Spacer(modifier = Modifier.height(mediumSpacer))

                AppActionButton(
                    text = "Cancelar",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(contentWidthFraction)
                )
            }
        }
    )
}