package com.example.thelastturn.viewmodel

import android.app.Application
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    private val prefs = PreferencesManager(application)

    private val _playerAlias = MutableStateFlow("Jugador")
    val playerAlias: StateFlow<String> = _playerAlias.asStateFlow()

    var numberOfSlots by mutableStateOf(0); private set
    var remainingTotalTime by mutableStateOf(0)
    var remainingActionTime by mutableStateOf(0)
    var totalTimeSeconds by mutableStateOf(0)
    var actionTimeSeconds by mutableStateOf(0)

    private var isTimerRunning by mutableStateOf(false)
    private var totalTimerJob: Job? = null
    private var actionTimerJob: Job? = null

    private val _gameState = mutableStateOf(GameState.ONGOING)
    val gameState get() = _gameState.value

    var cardsEliminated by mutableStateOf(0); private set
    var totalDamageDealt by mutableStateOf(0); private set
    var totalDamageReceived by mutableStateOf(0); private set

    private val _gameLog = MutableStateFlow("")
    val gameLog: StateFlow<String> get() = _gameLog.asStateFlow()

    private val _player = mutableStateOf(Player("Jugador", 3, 3, mutableListOf()))
    val player: Player get() = _player.value
    private val _enemy = mutableStateOf(Player("Enemigo", 3, 3, mutableListOf()))
    val enemy: Player get() = _enemy.value

    private val _currentTurn = mutableStateOf(PlayerTurn.PLAYER)
    val currentTurn get() = _currentTurn.value

    val playerSlots = mutableStateListOf<BoardSlot>()
    val enemySlots = mutableStateListOf<BoardSlot>()

    private val _playerHand = mutableStateListOf<Card>()
    val playerHand: List<Card> get() = _playerHand
    private val _enemyHand = mutableStateListOf<Card>()
    val enemyHand: List<Card> get() = _enemyHand

    private val _selectedCard = mutableStateOf<Card?>(null)
    val selectedCard get() = _selectedCard.value

    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent get() = _navigationEvent.value

    private val _preferencesLoaded = MutableStateFlow(false)
    val preferencesLoaded: StateFlow<Boolean> = _preferencesLoaded.asStateFlow()

    init {
        Log.d("GameViewModel", "GameViewModel inicializado.")
        resetMetrics()

        viewModelScope.launch {
            val loadedPlayerName = prefs.playerName.first()
            val loadedNumSlots = prefs.numSlots.first()
            val loadedTotalTime = prefs.totalTime.first()
            val loadedActionTime = prefs.actionTime.first()

            _playerAlias.value = loadedPlayerName
            _player.value = _player.value.copy(name = loadedPlayerName)
            Log.d("GameViewModel", "Alias del jugador cargado de preferencias: $loadedPlayerName")

            startNewGameInternal(
                numSlots = loadedNumSlots,
                playerName = loadedPlayerName,
                totalTime = loadedTotalTime,
                actionTime = loadedActionTime
            )

            _preferencesLoaded.value = true
            Log.d("GameViewModel", "Preferencias iniciales cargadas y juego iniciado.")
        }
    }

    private fun resetMetrics() {
        cardsEliminated = 0
        totalDamageDealt = 0
        totalDamageReceived = 0
        _gameLog.value = ""
        Log.d("GameViewModel", "Métricas de juego reiniciadas.")
    }

    fun addLogEntry(entry: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _gameLog.value += "$timestamp - $entry\n"
        Log.d("GameViewModel", "Log: $entry")
    }

    fun startNewGame(
        numSlots: Int,
        playerName: String,
        totalTime: Int,
        actionTime: Int
    ) {
        viewModelScope.launch {
            prefs.savePlayerName(playerName)
            prefs.saveGameSettings(numSlots, totalTime, actionTime)
            Log.d("GameViewModel", "Preferencias guardadas al iniciar nueva partida desde UI.")
        }
        startNewGameInternal(numSlots, playerName, totalTime, actionTime)
    }

    private fun startNewGameInternal(
        numSlots: Int,
        playerName: String,
        totalTime: Int,
        actionTime: Int
    ) {
        stopTimers()
        resetMetrics()

        this.numberOfSlots = numSlots
        this.totalTimeSeconds = totalTime
        this.actionTimeSeconds = actionTime
        this.remainingTotalTime = totalTimeSeconds
        this.remainingActionTime = actionTimeSeconds

        _gameState.value = GameState.ONGOING
        _currentTurn.value = PlayerTurn.PLAYER
        _navigationEvent.value = null

        _player.value = player.copy(name = playerName, maxHits = 3, currentHits = 3, deck = Card.sampleDeck().toMutableList())
        _enemy.value = enemy.copy(name = "Enemigo", maxHits = 3, currentHits = 3, deck = Card.sampleDeck().shuffled().toMutableList())
        Log.d("GameViewModel", "Jugadores inicializados. Mazos: Jugador ${player.deck.size}, Enemigo ${enemy.deck.size}")

        playerSlots.clear()
        enemySlots.clear()
        repeat(numSlots) { i ->
            playerSlots.add(BoardSlot(i + 1))
            enemySlots.add(BoardSlot(i + 1 + numSlots))
        }
        Log.d("GameViewModel", "Slots del tablero inicializados: ${playerSlots.size} para jugador, ${enemySlots.size} para enemigo.")

        _playerHand.clear()
        _enemyHand.clear()
        _selectedCard.value = null
        Log.d("GameViewModel", "Manos y carta seleccionada limpiadas.")

        drawInitialCards()
        placeInitialEnemyCard()

        checkGameState()
        startTimers()
        addLogEntry("Nueva partida iniciada con $numSlots slots. Tiempo total: $totalTime s, Tiempo por acción: $actionTime s.")
    }

    private fun startTimers() {
        stopTimers()
        isTimerRunning = true
        Log.d("GameViewModel", "Temporizadores iniciados.")

        totalTimerJob = viewModelScope.launch {
            while (isTimerRunning && remainingTotalTime > 0 && _gameState.value == GameState.ONGOING) {
                delay(1000L)
                remainingTotalTime--
            }
            if (remainingTotalTime <= 0 && _gameState.value == GameState.ONGOING) {
                evaluateEndGameByTime()
            }
        }

        actionTimerJob = viewModelScope.launch {
            while (isTimerRunning && _gameState.value == GameState.ONGOING) {
                resetActionTimer()
                while (remainingActionTime > 0 && _gameState.value == GameState.ONGOING) {
                    delay(1000L)
                    remainingActionTime--
                }

                if (_gameState.value != GameState.ONGOING) break

                if (_currentTurn.value == PlayerTurn.PLAYER) {
                    addLogEntry("Tiempo de acción del jugador agotado. Turno automático del enemigo.")
                    _currentTurn.value = PlayerTurn.OPPONENT
                    delay(500L)
                    placeEnemyCardForTurn()
                    _currentTurn.value = PlayerTurn.COMBAT
                    delay(500L)
                    resolveCombat()
                    if (_gameState.value != GameState.ONGOING) break
                    _currentTurn.value = PlayerTurn.PLAYER
                    resetActionTimer()
                }
            }
        }
    }

    private fun evaluateEndGameByTime() {
        if (_gameState.value != GameState.ONGOING) return

        addLogEntry("Tiempo total de partida agotado.")

        when {
            player.currentHits > enemy.currentHits -> setGameState(GameState.VICTORY)
            player.currentHits < enemy.currentHits -> setGameState(GameState.DEFEAT)
            else -> {
                when {
                    totalDamageDealt > totalDamageReceived -> setGameState(GameState.VICTORY)
                    totalDamageDealt < totalDamageReceived -> setGameState(GameState.DEFEAT)
                    else -> setGameState(GameState.DRAW)
                }
            }
        }
        Log.d("GameViewModel", "Juego finalizado por tiempo: ${_gameState.value}")
    }

    private fun resetActionTimer() {
        remainingActionTime = actionTimeSeconds
        Log.d("GameViewModel", "Temporizador de acción reiniciado a $remainingActionTime segundos.")
    }

    private fun stopTimers() {
        totalTimerJob?.cancel()
        actionTimerJob?.cancel()
        isTimerRunning = false
        Log.d("GameViewModel", "Temporizadores detenidos.")
    }

    private fun drawInitialCards() {
        Log.d("GameViewModel", "Robando cartas iniciales...")
        repeat(3) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }
        Log.d("GameViewModel", "Mano del jugador: ${_playerHand.size} cartas. Mano del enemigo: ${_enemyHand.size} cartas.")
        checkGameState()
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
        addLogEntry("Carta seleccionada: ${card.name}")
    }

    fun placeCard(slotId: Int) {
        val card = _selectedCard.value ?: run {
            addLogEntry("Error: No hay carta seleccionada para colocar.")
            return
        }
        val slot = playerSlots.firstOrNull { it.id == slotId } ?: run {
            addLogEntry("Error: Slot ${slotId} no encontrado.")
            return
        }

        if (slot.isEmpty && _currentTurn.value == PlayerTurn.PLAYER) {
            slot.placeCard(card)
            _playerHand.remove(card)
            _selectedCard.value = null
            resetActionTimer()

            addLogEntry("Carta ${card.name} colocada en slot ${slotId}.")

            viewModelScope.launch {
                _currentTurn.value = PlayerTurn.COMBAT
                delay(400L)
                resolveCombat()

                if (_gameState.value == GameState.ONGOING) {
                    placeEnemyCardForTurn()
                    _currentTurn.value = PlayerTurn.PLAYER
                    resetActionTimer()
                }
            }
        } else {
            addLogEntry("No se puede colocar la carta: slot no vacío o no es el turno del jugador.")
        }
    }

    private fun resolveCombat() {
        addLogEntry("Iniciando fase de combate.")
        val currentplayerSlots = playerSlots.toList()
        val currentenemySlots = enemySlots.toList()

        currentplayerSlots.zip(currentenemySlots).forEach { (playerSlot, enemySlot) ->
            if (_gameState.value != GameState.ONGOING) {
                addLogEntry("Combate detenido prematuramente, juego finalizado.")
                return
            }

            val playerCard = playerSlot.card
            val enemyCard = enemySlot.card

            when {
                playerCard != null && enemyCard != null -> {
                    val damageToEnemy = playerCard.damage
                    val damageToPlayer = enemyCard.damage

                    totalDamageDealt += damageToEnemy
                    totalDamageReceived += damageToPlayer

                    val updatedPlayerCard = playerCard.withHealth(playerCard.health - damageToPlayer)
                    val updatedEnemyCard = enemyCard.withHealth(enemyCard.health - damageToEnemy)

                    playerSlot.updateCard(updatedPlayerCard)
                    enemySlot.updateCard(updatedEnemyCard)

                    addLogEntry("Combate: ${playerCard.name} (${playerCard.health}) vs ${enemyCard.name} (${enemyCard.health}).")

                    if (updatedEnemyCard.health <= 0) {
                        enemySlot.clear()
                        cardsEliminated++
                        addLogEntry("Carta enemiga ${enemyCard.name} eliminada.")
                    }
                    if (updatedPlayerCard.health <= 0) {
                        playerSlot.clear()
                        addLogEntry("Carta del jugador ${playerCard.name} eliminada.")
                    }
                }
                playerCard != null && enemyCard == null -> {
                    val directDamageToEnemyPlayer = 1
                    totalDamageDealt += directDamageToEnemyPlayer
                    _enemy.value = _enemy.value.copy(currentHits = _enemy.value.currentHits - directDamageToEnemyPlayer)
                    addLogEntry("Carta ${playerCard.name} del jugador ataca directamente al enemigo, infligiendo $directDamageToEnemyPlayer daño. Vidas enemigo: ${_enemy.value.currentHits}")
                }
                playerCard == null && enemyCard != null -> {
                    val directDamageToPlayerPlayer = 1
                    totalDamageReceived += directDamageToPlayerPlayer
                    _player.value = _player.value.copy(currentHits = _player.value.currentHits - directDamageToPlayerPlayer)
                    addLogEntry("Carta ${enemyCard.name} del enemigo ataca directamente al jugador, infligiendo $directDamageToPlayerPlayer daño. Vidas jugador: ${_player.value.currentHits}")
                }
                playerCard == null && enemyCard == null -> {
                    addLogEntry("Slots vacíos. No hay combate en este par.")
                }
            }
            checkGameState()
        }

        if (_gameState.value == GameState.ONGOING) {
            repeat(2) {
                drawCard(player.deck)?.let { _playerHand.add(it) }
                drawCard(enemy.deck)?.let { _enemyHand.add(it) }
            }
            addLogEntry("Se robaron 2 cartas para cada jugador después del combate.")
            checkGameState()
        }
    }

    private fun checkGameState() {
        if (_gameState.value != GameState.ONGOING) return

        val pHits = _player.value.currentHits
        val eHits = _enemy.value.currentHits

        val playerHasCardsInDeck = _player.value.deck.isNotEmpty()
        val enemyHasCardsInDeck = _enemy.value.deck.isNotEmpty()

        val playerHasCardsAvailable = playerHasCardsInDeck || _playerHand.isNotEmpty() || playerSlots.any { !it.isEmpty }
        val enemyHasCardsAvailable = enemyHasCardsInDeck || _enemyHand.isNotEmpty() || enemySlots.any { !it.isEmpty }

        Log.d("GameViewModel", "CheckGameState: Player Hits: $pHits, Enemy Hits: $eHits")
        Log.d("GameViewModel", "Player has cards: $playerHasCardsAvailable, Enemy has cards: $enemyHasCardsAvailable")

        when {
            pHits <= 0 && eHits <= 0 -> {
                addLogEntry("Ambos jugadores sin vidas.")
                when {
                    totalDamageDealt > totalDamageReceived -> setGameState(GameState.VICTORY)
                    totalDamageDealt < totalDamageReceived -> setGameState(GameState.DEFEAT)
                    else -> setGameState(GameState.DRAW)
                }
            }
            pHits <= 0 -> {
                addLogEntry("El jugador se quedó sin vidas.")
                setGameState(GameState.DEFEAT)
            }
            eHits <= 0 -> {
                addLogEntry("El enemigo se quedó sin vidas.")
                setGameState(GameState.VICTORY)
            }
            !playerHasCardsAvailable && !enemyHasCardsAvailable -> {
                addLogEntry("¡Ambos jugadores se quedaron sin cartas ni cartas en tablero!")
                when {
                    pHits > eHits -> setGameState(GameState.VICTORY)
                    pHits < eHits -> setGameState(GameState.DEFEAT)
                    else -> {
                        when {
                            totalDamageDealt > totalDamageReceived -> setGameState(GameState.VICTORY)
                            totalDamageDealt < totalDamageReceived -> setGameState(GameState.DEFEAT)
                            else -> setGameState(GameState.DRAW)
                        }
                    }
                }
            }
            !playerHasCardsAvailable && enemyHasCardsAvailable -> {
                addLogEntry("El jugador se quedó sin cartas ni cartas en tablero.")
                setGameState(GameState.DEFEAT)
            }
            !enemyHasCardsAvailable && playerHasCardsAvailable -> {
                addLogEntry("El enemigo se quedó sin cartas ni cartas en tablero.")
                setGameState(GameState.VICTORY)
            }
            else -> Log.d("GameViewModel", "Juego continúa ONGOING.")
        }
    }

    private fun setGameState(state: GameState) {
        if (_gameState.value == state) return

        _gameState.value = state
        _navigationEvent.value = when (state) {
            GameState.VICTORY -> "VICTORY"
            GameState.DEFEAT -> "DEFEAT"
            GameState.DRAW -> "DRAW"
            GameState.TIME_OVER -> "TIEMPO_AGOTADO"
            else -> null
        }
        stopTimers()
        saveGameResult()
        addLogEntry("Juego finalizado con estado: $state. Resultado para navegación: ${_navigationEvent.value}")
    }

    private fun drawCard(deck: MutableList<Card>): Card? {
        val drawnCard = deck.removeFirstOrNull()
        if (drawnCard != null) {
            Log.d("GameViewModel", "Carta robada: ${drawnCard.name}. Quedan ${deck.size} en el mazo.")
        } else {
            Log.d("GameViewModel", "Intento de robar carta de mazo vacío.")
        }
        checkGameState()
        return drawnCard
    }

    private fun placeEnemyCardForTurn() {
        val emptySlot = enemySlots.firstOrNull { it.isEmpty }
        if (emptySlot != null && _enemyHand.isNotEmpty()) {
            val cardToPlace = _enemyHand.random()
            emptySlot.placeCard(cardToPlace)
            _enemyHand.remove(cardToPlace)
            addLogEntry("El enemigo colocó ${cardToPlace.name} en slot ${emptySlot.id}.")
            Log.d("GameViewModel", "Mano del enemigo después de colocar: ${_enemyHand.size} cartas.")
        } else {
            addLogEntry("El enemigo no pudo colocar una carta (sin slots vacíos o sin cartas en mano).")
            Log.d("GameViewModel", "Slots vacíos para enemigo: ${enemySlots.count { it.isEmpty }}. Cartas en mano enemigo: ${_enemyHand.size}.")
            checkGameState()
        }
    }

    private fun placeInitialEnemyCard() {
        Log.d("GameViewModel", "Intentando colocar carta inicial del enemigo...")
        enemySlots.firstOrNull { it.isEmpty }?.let { slot ->
            _enemyHand.firstOrNull()?.also { card ->
                slot.placeCard(card)
                _enemyHand.remove(card)
                addLogEntry("El enemigo colocó la carta inicial ${card.name} en slot ${slot.id}.")
                Log.d("GameViewModel", "Carta inicial del enemigo colocada. Mano del enemigo: ${_enemyHand.size} cartas.")
                checkGameState()
            } ?: run {
                addLogEntry("Error: El enemigo no tiene cartas en mano para la carta inicial.")
                Log.d("GameViewModel", "Mano del enemigo está vacía para la carta inicial.")
                checkGameState()
            }
        } ?: run {
            addLogEntry("No hay slots disponibles para la carta inicial del enemigo.")
            Log.d("GameViewModel", "Todos los slots de enemigo están ocupados para la carta inicial.")
            checkGameState()
        }
    }

    private fun saveGameResult() {
        if (_gameState.value == GameState.ONGOING) return

        viewModelScope.launch {
            val resultadoString = when (_gameState.value) {
                GameState.VICTORY -> "Victoria"
                GameState.DEFEAT -> "Derrota"
                GameState.DRAW -> "Empate"
                GameState.TIME_OVER -> "Tiempo Agotado"
                else -> "Desconocido"
            }

            val partida = Partida(
                id = UUID.randomUUID().toString(),
                alias = _playerAlias.value,
                fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                resultado = resultadoString,
                sizeTablero = numberOfSlots,
                dmgInfligido = totalDamageDealt,
                dmgRecibido = totalDamageReceived,
                cartasEliminadas = cardsEliminated,
                logPartida = gameLog.value
            )
            repository.insertPartida(partida)
            addLogEntry("Partida finalizada y guardada: $resultadoString")
            Log.d("GameViewModel", "Partida guardada en la DB. ID: ${partida.id}, Resultado: ${partida.resultado}")
        }
    }

    fun getGameLogSummary(): String {
        return "Cartas eliminadas: $cardsEliminated\n" +
                "Daño infligido: $totalDamageDealt\n" +
                "Daño recibido: $totalDamageReceived"
    }

    fun resetNavigation() {
        _navigationEvent.value = null
        Log.d("GameViewModel", "Evento de navegación reseteado.")
    }
}