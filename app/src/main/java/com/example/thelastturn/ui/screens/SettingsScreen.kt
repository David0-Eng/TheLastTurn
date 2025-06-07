package com.example.thelastturn.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background // Importar si se usa background en lugar de solo Surface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape // Importar si se usa en algún lado
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush // Importar si se usa para degradados
import androidx.compose.ui.graphics.Color // Importar si se usa para colores específicos
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thelastturn.viewmodel.GameViewModel

@Composable
fun SettingsScreen(
    // Callback que se ejecuta cuando el juego debe iniciar
    onStartGame: (playerName: String, numSlots: Int, totalTime: Int, actionTime: Int) -> Unit
) {
    // Obtiene la instancia de ViewModelStoreOwner del Activity local
    // Necesario para que el ViewModel sobreviva a cambios de configuración
    val owner = LocalActivity.current as ViewModelStoreOwner
    // Instancia del GameViewModel, asociada al ciclo de vida del Activity
    val viewModel: GameViewModel = viewModel(viewModelStoreOwner = owner)

    // Estados para los campos de entrada, usando rememberSaveable para persistir en cambios de configuración
    var playerName by rememberSaveable { mutableStateOf("") }
    var numSlots by rememberSaveable { mutableStateOf(4) } // Tamaño predeterminado del tablero
    var totalTime by rememberSaveable { mutableStateOf("") } // Tiempo total de la partida
    var actionTime by rememberSaveable { mutableStateOf("") } // Tiempo por acción
    var submitted by rememberSaveable { mutableStateOf(false) } // Indica si el formulario ha sido intentado enviar

    // Obtiene la configuración actual para adaptar la UI a orientación (horizontal/vertical)
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // Estado de scroll para el contenido de la columna
    val scrollState = rememberScrollState()

    // Definición de las fracciones de ancho y espaciadores según la orientación
    val widthFraction = if (isLandscape) 0.6f else 1f
    val smallSpacer = if (isLandscape) 8.dp else 16.dp
    val mediumSpacer = if (isLandscape) 12.dp else 16.dp
    val largeSpacer = if (isLandscape) 16.dp else 24.dp

    // Validación del formulario: todos los campos de texto deben tener contenido
    val isFormValid = playerName.isNotBlank() && totalTime.isNotBlank() && actionTime.isNotBlank()
    // Errores específicos para cada campo después de un intento de envío
    val nameError = submitted && playerName.isBlank()
    val totalError = submitted && totalTime.isBlank()
    val actionError = submitted && actionTime.isBlank()

    // Contenedor principal de la pantalla, con scroll y padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isLandscape) Arrangement.Top else Arrangement.Center // Alineación vertical
    ) {
        // Campo para el nombre del jugador
        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Nombre del jugador") },
            isError = nameError, // Muestra el error si el campo está vacío al intentar enviar
            singleLine = true,
            modifier = Modifier.fillMaxWidth(widthFraction) // Ancho adaptado a la orientación
        )
        // Mensaje de error para el nombre
        if (nameError) {
            Text(
                text = "Este campo es obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(smallSpacer))

        // Selección del número de cartas por tablero (3x3, 4x4, 5x5)
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
            // Itera para crear los RadioButtons para cada opción de tamaño
            listOf(3, 4, 5).forEach { n ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { numSlots = n } // Permite seleccionar al hacer click en toda la fila
                        .padding(4.dp)
                ) {
                    RadioButton(
                        selected = numSlots == n, // Seleccionado si coincide con el estado actual
                        onClick = { numSlots = n } // Actualiza el estado al hacer click
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

        // Campo para el tiempo total de la partida
        OutlinedTextField(
            value = totalTime,
            onValueChange = {
                // Permite solo dígitos y un máximo de 3 caracteres
                if (it.all { c -> c.isDigit() } && it.length <= 3) totalTime = it
            },
            label = { Text("Tiempo total (segundos, max. 999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Teclado numérico
            isError = totalError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(widthFraction)
        )
        // Mensaje de error para el tiempo total
        if (totalError) {
            Text(
                text = "Este campo es obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(smallSpacer))

        // Campo para el tiempo por acción
        OutlinedTextField(
            value = actionTime,
            onValueChange = {
                // Permite solo dígitos y un máximo de 3 caracteres
                if (it.all { c -> c.isDigit() } && it.length <= 3) actionTime = it
            },
            label = { Text("Tiempo por acción (segundos, max. 999)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Teclado numérico
            isError = actionError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(widthFraction)
        )
        // Mensaje de error para el tiempo por acción
        if (actionError) {
            Text(
                text = "Este campo es obligatorio",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(largeSpacer))

        // Botón para iniciar el juego
        Button(
            onClick = {
                submitted = true // Marca que se intentó enviar el formulario
                if (isFormValid) { // Si el formulario es válido
                    // Convierte los tiempos a Int, manejando valores nulos y limitando a 999
                    val totalSec = (totalTime.toIntOrNull() ?: 0).coerceAtMost(999)
                    val actionSec = (actionTime.toIntOrNull() ?: 0).coerceAtMost(999)
                    // Llama al callback para iniciar el juego con los parámetros validados
                    onStartGame(playerName, numSlots, totalSec, actionSec)
                }
            },
            enabled = isFormValid, // El botón solo está habilitado si el formulario es válido
            modifier = Modifier
                .fillMaxWidth(widthFraction) // Ancho adaptado a la orientación
                .padding(bottom = if (isLandscape) 16.dp else 0.dp) // Padding inferior en landscape
        ) {
            Text("Empezar")
        }
    }
}