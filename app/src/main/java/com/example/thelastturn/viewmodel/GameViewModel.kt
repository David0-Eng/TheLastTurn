package com.example.thelastturn.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thelastturn.data.GameRepository
import com.example.thelastturn.data.Partida
import com.example.thelastturn.data.PreferencesManager
import com.example.thelastturn.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.getValue // Import for property delegation
import androidx.compose.runtime.mutableStateOf // Import for state
import androidx.compose.runtime.setValue // Import for property delegation
import androidx.compose.runtime.mutableStateListOf // Import for observable lists

class GameViewModel(
    application: Application,
    val repository: GameRepository // Inyección del repositorio de partidas
) : AndroidViewModel(application) {

    // Gestor de preferencias (se inicializa una sola vez)
    private val prefs = PreferencesManager(application)

    // Alias del jugador, observado de las preferencias
    // Se inicializa con un valor por defecto y se actualiza al recoger de DataStore
    private val _playerAlias = MutableStateFlow("Jugador")
    val playerAlias: StateFlow<String> = _playerAlias.asStateFlow()

    // Configuración de tablero y tiempos
    var numberOfSlots by mutableStateOf(0); private set
    var remainingTotalTime by mutableStateOf(0)
    var remainingActionTime by mutableStateOf(0)

    var totalTimeSeconds by mutableStateOf(0)
    var actionTimeSeconds by mutableStateOf(0)

    private var isTimerRunning by mutableStateOf(false)
    private var totalTimerJob: Job? = null                      // Uso de Job para gestionar mejor los tiempos
    private var actionTimerJob: Job? = null                     // Consultado su uso en clase

    // Estado de la partida
    private val _gameState = mutableStateOf(GameState.ONGOING)
    val gameState get() = _gameState.value

    // Métricas para el log y para guardar en la BD
    var cardsEliminated by mutableStateOf(0); private set
    var totalDamageDealt by mutableStateOf(0); private set
    var totalDamageReceived by mutableStateOf(0); private set

    // Log detallado (para el panel secundario en tablets)
    private val _gameLog = MutableStateFlow("")
    val gameLog: StateFlow<String> get() = _gameLog.asStateFlow()

    // Jugadores (se actualizan para reflejar el alias desde preferencias)
    private val _player = mutableStateOf(Player("Jugador", 3, 3, mutableListOf()))
    val player: Player get() = _player.value

    private val _enemy = mutableStateOf(Player("Enemigo", 3, 3, mutableListOf()))
    val enemy: Player get() = _enemy.value

    // Turno actual
    private val _currentTurn = mutableStateOf(PlayerTurn.PLAYER)
    val currentTurn get() = _currentTurn.value

    // Slots de tablero
    // Utiliza `mutableStateListOf` para que Compose reaccione a los cambios en la lista misma
    val playerSlots = mutableStateListOf<BoardSlot>()
    val enemySlots  = mutableStateListOf<BoardSlot>()

    // Manos / mazos
    private val _playerHand = mutableStateListOf<Card>()
    val playerHand get() = _playerHand
    private val _enemyHand = mutableStateListOf<Card>()
    val enemyHand get() = _enemyHand

    // Carta seleccionada
    private val _selectedCard = mutableStateOf<Card?>(null)
    val selectedCard get() = _selectedCard.value

    // Evento de navegación para finalizar la partida (se usa para un solo disparo)
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent get() = _navigationEvent.value

    // Se ejecuta al inicializar el ViewModel
    init {
        resetMetrics()
        // Recoger el alias del jugador de las preferencias de forma reactiva
        // Esto asegura que el ViewModel siempre tenga el alias actualizado
        viewModelScope.launch {
            prefs.playerName.collect { alias ->
                _playerAlias.value = alias
                // Actualizar el nombre del jugador en el modelo del jugador principal
                // sin re-inicializar el resto del estado del jugador
                _player.value = _player.value.copy(name = alias)
            }
        }
    }

    // Método para resetear las métricas de la partida
    private fun resetMetrics() {
        cardsEliminated = 0
        totalDamageDealt = 0
        totalDamageReceived = 0
        _gameLog.value = "" // Limpiar el log al reiniciar
    }

    // Método para agregar entradas al log de la partida
    fun addLogEntry(entry: String) {
        _gameLog.value += "${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())} - $entry\n"
    }

    /**
     * Inicia una nueva partida con la configuración dada.
     * @param numSlots Número de slots en el tablero.
     * @param playerName Nombre del jugador (se guarda en preferencias).
     * @param totalTime Tiempo total permitido para la partida.
     * @param actionTime Tiempo para cada acción del jugador.
     */
    fun startNewGame(
        numSlots: Int,
        playerName: String, // Este playerName se usa para guardar la preferencia.
        totalTime: Int,
        actionTime: Int
    ) {
        stopTimers()
        resetMetrics()

        numberOfSlots = numSlots
        totalTimeSeconds = totalTime
        actionTimeSeconds = actionTime
        remainingTotalTime = totalTimeSeconds
        remainingActionTime = actionTimeSeconds

        // Reset de estado de la partida
        _gameState.value = GameState.ONGOING
        _currentTurn.value = PlayerTurn.PLAYER
        _navigationEvent.value = null // Resetear el evento de navegación

        // Guardar el nombre del jugador en Preferences DataStore
        // El Flow _playerAlias se actualizará automáticamente.
        viewModelScope.launch {
            prefs.savePlayerName(playerName)
        }

        startTimers() // Iniciar los temporizadores de la partida

        // Preparar mazos: se copian las cartas del mazo base para cada jugador
        val baseDeck = Card.sampleDeck()
        val playerDeck = baseDeck.map { it.copy() }.toMutableList()
        val enemyDeck  = baseDeck.shuffled().map { it.copy() }.toMutableList()

        // _player.value.name ya se actualiza mediante el Flow de _playerAlias
        _player.value = player.copy(maxHits = 3, currentHits = 3, deck = playerDeck) // Reutiliza el alias actual
        _enemy.value  = Player("Enemigo", 3, 3, enemyDeck)

        // Configurar slots vacíos para el tablero
        playerSlots.clear()
        enemySlots.clear()
        repeat(numSlots) { i ->
            playerSlots.add(BoardSlot(i + 1))
            enemySlots.add(BoardSlot(i + 1 + numSlots))
        }

        // Limpiar manos y selección de cartas
        _playerHand.clear()
        _enemyHand.clear()
        _selectedCard.value = null

        // Robar cartas iniciales y colocar carta inicial del enemigo
        drawInitialCards()
        placeInitialEnemyCard()
    }

    // Inicia los temporizadores de la partida
    private fun startTimers() {
        stopTimers() // Asegurarse de que no haya temporizadores corriendo ya
        isTimerRunning = true

        // Temporizador total de la partida
        totalTimerJob = viewModelScope.launch {
            while (isTimerRunning && remainingTotalTime > 0 && _gameState.value == GameState.ONGOING) {
                delay(1000L)
                remainingTotalTime--
            }
            // Si el tiempo total se agota y la partida sigue en curso, evaluamos el final por tiempo
            if (remainingTotalTime <= 0 && _gameState.value == GameState.ONGOING) {
                evaluateEndGameByTime()
            }
        }

        // Temporizador de acción por turno
        actionTimerJob = viewModelScope.launch {
            while (isTimerRunning && _gameState.value == GameState.ONGOING) {
                resetActionTimer() // Resetear tiempo de acción al inicio de cada ciclo de turno
                while (remainingActionTime > 0 && _gameState.value == GameState.ONGOING) {
                    delay(1000L)
                    remainingActionTime--
                }

                // Si la partida no ha terminado por otras condiciones y el tiempo de acción se agotó
                if (_gameState.value != GameState.ONGOING) break

                // Lógica de turno automático si el tiempo de acción se agota en el turno del jugador
                if (_currentTurn.value == PlayerTurn.PLAYER) {
                    addLogEntry("Tiempo de acción del jugador agotado. Turno automático del enemigo.")
                    _currentTurn.value = PlayerTurn.OPPONENT
                    delay(500L) // Pequeña pausa para simular acción del enemigo
                    placeEnemyCardForTurn() // El enemigo coloca una carta
                    _currentTurn.value = PlayerTurn.COMBAT
                    delay(500L) // Pequeña pausa antes del combate
                    resolveCombat() // Resolver combate
                    if (_gameState.value != GameState.ONGOING) break // Salir si el combate terminó la partida
                    _currentTurn.value = PlayerTurn.PLAYER // Volver al turno del jugador
                    resetActionTimer() // Resetear el tiempo de acción para el jugador
                }
            }
        }
    }

    // Evalúa el final de la partida cuando se agota el tiempo total
    private fun evaluateEndGameByTime() {
        if (_gameState.value != GameState.ONGOING) return // Evitar re-evaluar si ya terminó

        addLogEntry("Tiempo total de partida agotado.")

        // Comparar vidas restantes para determinar el resultado
        when {
            player.currentHits > enemy.currentHits -> {
                _gameState.value = GameState.VICTORY
                _navigationEvent.value = "VICTORY"
            }
            player.currentHits < enemy.currentHits -> {
                _gameState.value = GameState.DEFEAT
                _navigationEvent.value = "DEFEAT"
            }
            else -> { // Vidas iguales: usar daño como desempate
                when {
                    totalDamageDealt > totalDamageReceived -> {
                        _gameState.value = GameState.VICTORY
                        _navigationEvent.value = "VICTORY"
                    }
                    totalDamageDealt < totalDamageReceived -> {
                        _gameState.value = GameState.DEFEAT
                        _navigationEvent.value = "DEFEAT"
                    }
                    else -> { // Daño igual: empate
                        _gameState.value = GameState.DRAW
                        _navigationEvent.value = "DRAW"
                    }
                }
            }
        }
        saveGameResult() // Guardar el resultado de la partida al finalizar
    }

    // Resetea el contador de tiempo de acción
    private fun resetActionTimer() {
        remainingActionTime = actionTimeSeconds
    }

    // Detiene todos los temporizadores
    private fun stopTimers() {
        totalTimerJob?.cancel()
        actionTimerJob?.cancel()
        isTimerRunning = false
    }

    // Roba las cartas iniciales para ambos jugadores
    private fun drawInitialCards() {
        repeat(3) { // Robar 3 cartas para cada jugador
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }
    }

    // Selecciona una carta para ser colocada en el tablero
    fun selectCard(card: Card) {
        _selectedCard.value = card
        addLogEntry("Carta seleccionada: ${card.name}")
    }

    /**
     * Coloca la carta seleccionada en un slot del tablero del jugador.
     * @param slotId ID del slot donde se intentará colocar la carta.
     */
    fun placeCard(slotId: Int) {
        val card = _selectedCard.value ?: return // No hay carta seleccionada
        val slot = playerSlots.firstOrNull { it.id == slotId } ?: return // Slot no encontrado

        // Solo permitir colocar si el slot está vacío y es el turno del jugador
        if (slot.isEmpty && _currentTurn.value == PlayerTurn.PLAYER) {
            slot.placeCard(card)
            _playerHand.remove(card) // Eliminar la carta de la mano
            _selectedCard.value = null // Deseleccionar la carta
            resetActionTimer() // Resetear el tiempo de acción del jugador

            addLogEntry("Carta ${card.name} colocada en slot ${slotId}.")

            // Iniciar la secuencia de combate y turno del enemigo
            viewModelScope.launch {
                _currentTurn.value = PlayerTurn.COMBAT
                delay(400L) // Pausa para simular la transición
                resolveCombat() // Resolver el combate

                // Si la partida no ha terminado después del combate, procede con el turno del enemigo
                if (_gameState.value == GameState.ONGOING) {
                    placeEnemyCardForTurn() // El enemigo coloca su carta
                    _currentTurn.value = PlayerTurn.PLAYER // Volver al turno del jugador
                    resetActionTimer() // Resetear el tiempo de acción para el jugador
                }
            }
        } else {
            addLogEntry("No se puede colocar la carta en el slot ${slotId}.")
        }
    }

    // Resuelve el combate entre las cartas en los slots de ambos jugadores
    private fun resolveCombat() {
        addLogEntry("Iniciando fase de combate.")
        playerSlots.zip(enemySlots).forEach { (playerSlot, enemySlot) ->
            val playerCard = playerSlot.card
            val enemyCard  = enemySlot.card

            when {
                // Ambas cartas presentes
                playerCard != null && enemyCard != null -> {
                    val damageToEnemy = playerCard.damage
                    val damageToPlayer = enemyCard.damage

                    // Actualizar métricas de daño
                    totalDamageDealt += damageToEnemy
                    totalDamageReceived += damageToPlayer

                    // Aplicar daño a las cartas
                    val updatedPlayer = playerCard.withHealth(playerCard.health - damageToPlayer)
                    val updatedEnemy  = enemyCard.withHealth(enemyCard.health - damageToEnemy)

                    playerSlot.updateCard(updatedPlayer)
                    enemySlot.updateCard(updatedEnemy)

                    addLogEntry("Combate: ${playerCard.name} (${playerCard.health}) vs ${enemyCard.name} (${enemyCard.health}).")

                    // Si la carta del enemigo es eliminada
                    if (updatedEnemy.health <= 0) {
                        enemySlot.clear()
                        cardsEliminated++
                        addLogEntry("Carta enemiga ${enemyCard.name} eliminada.")
                    }
                    // Si la carta del jugador es eliminada
                    if (updatedPlayer.health <= 0) {
                        playerSlot.clear()
                        addLogEntry("Carta del jugador ${playerCard.name} eliminada.")
                    }
                }
                // Solo carta del jugador presente (daño directo al enemigo)
                playerCard != null && enemyCard == null -> {
                    totalDamageDealt += playerCard.damage
                    _enemy.value = _enemy.value.copy(currentHits = _enemy.value.currentHits - 1)
                    playerSlot.clear() // La carta del jugador también se elimina tras el ataque directo
                    addLogEntry("Carta ${playerCard.name} del jugador inflige daño directo al enemigo. Vidas enemigo: ${_enemy.value.currentHits}")
                }
                // Solo carta del enemigo presente (daño directo al jugador)
                playerCard == null && enemyCard != null -> {
                    totalDamageReceived += enemyCard.damage
                    _player.value = _player.value.copy(currentHits = _player.value.currentHits - 1)
                    enemySlot.clear() // La carta del enemigo también se elimina tras el ataque directo
                    addLogEntry("Carta ${enemyCard.name} del enemigo inflige daño directo al jugador. Vidas jugador: ${_player.value.currentHits}")
                }
            }
        }

        checkGameState() // Comprobar si la partida ha terminado después del combate

        // Robar 2 cartas tras el duelo, si la partida sigue en curso
        if (_gameState.value == GameState.ONGOING) {
            repeat(2) {
                drawCard(_player.value.deck)?.let { _playerHand.add(it) }
                drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
            }
            addLogEntry("Se robaron 2 cartas para cada jugador.")
        }
    }

    // Lógica para comprobar el resultado de la partida
    private fun checkGameState() {
        if (_gameState.value != GameState.ONGOING) return // Si ya la partida terminó, no re-evaluar

        val pHits = _player.value.currentHits
        val eHits = _enemy.value.currentHits
        val pCards = _player.value.deck.isNotEmpty() || _playerHand.isNotEmpty() || playerSlots.any { !it.isEmpty }
        val eCards = _enemy.value.deck.isNotEmpty() || _enemyHand.isNotEmpty() || enemySlots.any { !it.isEmpty }

        when {
            // Ambos jugadores sin vidas
            pHits <= 0 && eHits <= 0 -> {
                when {
                    totalDamageDealt > totalDamageReceived -> {
                        _gameState.value = GameState.VICTORY
                        _navigationEvent.value = "VICTORY"
                    }
                    totalDamageDealt < totalDamageReceived -> {
                        _gameState.value = GameState.DEFEAT
                        _navigationEvent.value = "DEFEAT"
                    }
                    else -> {
                        _gameState.value = GameState.DRAW
                        _navigationEvent.value = "DRAW"
                    }
                }
            }

            // Jugador sin vidas
            pHits <= 0 -> {
                _gameState.value = GameState.DEFEAT
                _navigationEvent.value = "DEFEAT"
            }

            // Enemigo sin vidas
            eHits <= 0 -> {
                _gameState.value = GameState.VICTORY
                _navigationEvent.value = "VICTORY"
            }

            // Ambos sin cartas y sin cartas en tablero
            !pCards && !eCards -> {
                when {
                    pHits > eHits -> {
                        _gameState.value = GameState.VICTORY
                        _navigationEvent.value = "VICTORY"
                    }
                    pHits < eHits -> {
                        _gameState.value = GameState.DEFEAT
                        _navigationEvent.value = "DEFEAT"
                    }
                    else -> { // Vidas iguales: usar daño como desempate
                        when {
                            totalDamageDealt > totalDamageReceived -> {
                                _gameState.value = GameState.VICTORY
                                _navigationEvent.value = "VICTORY"
                            }
                            totalDamageDealt < totalDamageReceived -> {
                                _gameState.value = GameState.DEFEAT
                                _navigationEvent.value = "DEFEAT"
                            }
                            else -> {
                                _gameState.value = GameState.DRAW
                                _navigationEvent.value = "DRAW"
                            }
                        }
                    }
                }
            }
        }

        // Si el estado de la partida ha cambiado a finalizado, guardar en la BD
        if (_gameState.value != GameState.ONGOING) {
            stopTimers() // Detener temporizadores
            saveGameResult() // Guardar el resultado de la partida
        }
    }

    // Roba una carta del mazo dado, si no está vacío
    private fun drawCard(deck: MutableList<Card>) = deck.removeFirstOrNull()

    // El enemigo coloca una carta en un slot vacío durante su turno
    private fun placeEnemyCardForTurn() {
        val emptySlot = enemySlots.firstOrNull { it.isEmpty }
        if (emptySlot != null && _enemyHand.isNotEmpty()) {
            val cardToPlace = _enemyHand.random() // Elige una carta al azar de la mano del enemigo
            emptySlot.placeCard(cardToPlace)
            _enemyHand.remove(cardToPlace)
            addLogEntry("El enemigo colocó ${cardToPlace.name} en slot ${emptySlot.id}.")
        } else {
            addLogEntry("El enemigo no pudo colocar una carta (sin slots vacíos o sin cartas).")
        }
    }

    // Coloca la carta inicial del enemigo al principio de la partida
    private fun placeInitialEnemyCard() {
        enemySlots.firstOrNull { it.isEmpty }?.let { slot ->
            _enemyHand.firstOrNull()?.also { card ->
                slot.placeCard(card)
                _enemyHand.remove(card)
                addLogEntry("El enemigo colocó la carta inicial ${card.name} en slot ${slot.id}.")
            }
        }
    }

    /**
     * Guarda los resultados de la partida actual en la base de datos de Room.
     * Se llama cuando la partida finaliza (victoria, derrota, empate o tiempo agotado).
     */
    private fun saveGameResult() {
        // Asegurarse de que el estado es un estado final antes de guardar
        if (_gameState.value == GameState.ONGOING) return

        viewModelScope.launch {
            val resultadoString = when (_gameState.value) {
                GameState.VICTORY -> "Victoria"
                GameState.DEFEAT -> "Derrota"
                GameState.DRAW -> "Empate"
                else -> "Desconocido" // No debería llegar aquí
            }

            val partida = Partida(
                alias = _playerAlias.value, // Usar el alias actual del jugador desde Preferences
                fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                resultado = resultadoString,
                sizeTablero = numberOfSlots, // Asumiendo que numberOfSlots se mantiene del inicio de la partida
                dmgInfligido = totalDamageDealt,
                dmgRecibido = totalDamageReceived,
                cartasEliminadas = cardsEliminated,
                logPartida = gameLog.value
            )
            repository.insert(partida) // Insertar la partida en la BD
            addLogEntry("Partida finalizada y guardada: $resultadoString")
        }
    }

    // Obtiene un resumen de las métricas de la partida para el log
    fun getGameLogSummary(): String {
        return "Cartas eliminadas: $cardsEliminated\n" +
                "Daño infligido: $totalDamageDealt\n" +
                "Daño recibido: $totalDamageReceived"
    }

    // Resetea el evento de navegación (importante para eventos de un solo uso)
    fun resetNavigation() {
        _navigationEvent.value = null
    }
}
