package com.example.thelastturn.viewmodel

import kotlinx.coroutines.delay
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.GameState
import com.example.thelastturn.model.Player
import com.example.thelastturn.model.PlayerTurn
import kotlinx.coroutines.Job

class GameViewModel : ViewModel() {

    // Configuración de tablero y tiempos
    var numberOfSlots by mutableStateOf(0); private set
    var remainingTotalTime by mutableStateOf(0)
    var remainingActionTime by mutableStateOf(0)

    var totalTimeSeconds by mutableStateOf(0)
    var actionTimeSeconds by mutableStateOf(0)

    private var isTimerRunning by mutableStateOf(false)
    private var totalTimerJob: Job? = null
    private var actionTimerJob: Job? = null

    // Estado de la partida
    private val _gameState = mutableStateOf(GameState.ONGOING)
    val gameState get() = _gameState.value

    // Métricas para el log
    var cardsEliminated by mutableStateOf(0); private set
    var totalDamageDealt by mutableStateOf(0); private set
    var totalDamageReceived by mutableStateOf(0); private set

    // Jugadores
    private val _player = mutableStateOf(Player("Jugador", 3, 3, mutableListOf()))
    val player: Player get() = _player.value

    private val _enemy = mutableStateOf(Player("Enemigo", 3, 3, mutableListOf()))
    val enemy: Player get() = _enemy.value

    // Turno actual
    private val _currentTurn = mutableStateOf(PlayerTurn.PLAYER)
    val currentTurn get() = _currentTurn.value

    // Slots de tablero
    val playerSlots = mutableStateListOf<BoardSlot>()
    val enemySlots  = mutableStateListOf<BoardSlot>()

    // Manos
    private val _playerHand = mutableStateListOf<Card>()
    val playerHand get() = _playerHand
    private val _enemyHand = mutableStateListOf<Card>()
    val enemyHand get() = _enemyHand

    // Carta seleccionada
    private val _selectedCard = mutableStateOf<Card?>(null)
    val selectedCard get() = _selectedCard.value

    // Evento de navegación al final
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent get() = _navigationEvent.value

    /** Arranca de cero un nuevo juego con `numSlots` y nombre de jugador */
    fun startNewGame(
        numSlots: Int,
        playerName: String,
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

        // Reset de estado
        _gameState.value = GameState.ONGOING
        _currentTurn.value = PlayerTurn.PLAYER
        _navigationEvent.value = null

        startTimers()

        // Preparar mazos
        val baseDeck = Card.sampleDeck()
        val playerDeck = baseDeck.map { it.copy() }.toMutableList()
        val enemyDeck  = baseDeck.shuffled().map { it.copy() }.toMutableList()

        _player.value = Player(playerName, 3, 3, playerDeck)
        _enemy.value  = Player("Enemigo", 3, 3, enemyDeck)

        // Configurar slots
        playerSlots.clear()
        enemySlots.clear()
        repeat(numSlots) { i ->
            playerSlots.add(BoardSlot(i + 1))
            enemySlots.add(BoardSlot(i + 1 + numSlots))
        }

        // Limpiar manos y selección
        _playerHand.clear()
        _enemyHand.clear()
        _selectedCard.value = null

        // Robar inicial y colocar enemigo
        drawInitialCards()
        placeInitialEnemyCard()
    }

    private fun resetMetrics() {
        cardsEliminated = 0
        totalDamageDealt = 0
        totalDamageReceived = 0
    }

    private fun startTimers() {
        stopTimers()
        isTimerRunning = true

        totalTimerJob = viewModelScope.launch {
            while (isTimerRunning && remainingTotalTime > 0) {
                delay(1000L)
                remainingTotalTime--
                if (remainingTotalTime <= 0) {
                    _gameState.value = GameState.DRAW
                    _navigationEvent.value = "DRAW"
                }
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
                    _currentTurn.value = PlayerTurn.OPPONENT
                    delay(500L)
                    placeEnemyCardForTurn()
                    _currentTurn.value = PlayerTurn.COMBAT
                    delay(500L)
                    resolveCombat()
                    if (_gameState.value != GameState.ONGOING) break
                    _currentTurn.value = PlayerTurn.PLAYER
                }
            }
        }
    }

    private fun resetActionTimer() {
        remainingActionTime = actionTimeSeconds
    }

    private fun stopTimers() {
        totalTimerJob?.cancel()
        actionTimerJob?.cancel()
        isTimerRunning = false
    }

    private fun drawInitialCards() {
        repeat(3) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
    }

    fun placeCard(slotId: Int) {
        val card = _selectedCard.value ?: return
        val slot = playerSlots.firstOrNull { it.id == slotId } ?: return
        if (slot.isEmpty && _currentTurn.value == PlayerTurn.PLAYER) {
            slot.placeCard(card)
            _playerHand.remove(card)
            _selectedCard.value = null
            resetActionTimer()

            viewModelScope.launch {
                _currentTurn.value = PlayerTurn.COMBAT
                delay(400L)
                resolveCombat()
                placeEnemyCardForTurn()
                _currentTurn.value = PlayerTurn.PLAYER
                resetActionTimer()
            }
        }
    }

    private fun resolveCombat() {
        playerSlots.zip(enemySlots).forEach { (playerSlot, enemySlot) ->
            val playerCard = playerSlot.card
            val enemyCard  = enemySlot.card
            when {
                playerCard != null && enemyCard != null -> {
                    val damageToEnemy = playerCard.damage
                    val damageToPlayer = enemyCard.damage

                    // Actualizar métricas
                    totalDamageDealt += damageToEnemy
                    totalDamageReceived += damageToPlayer

                    val updatedPlayer = playerCard.withHealth(playerCard.health - damageToPlayer)
                    val updatedEnemy  = enemyCard.withHealth(enemyCard.health - damageToEnemy)

                    playerSlot.updateCard(updatedPlayer)
                    enemySlot.updateCard(updatedEnemy)

                    if (updatedEnemy.health <= 0) {
                        enemySlot.clear()
                        cardsEliminated++
                    }
                    if (updatedPlayer.health <= 0) {
                        playerSlot.clear()
                    }
                }
                playerCard != null && enemyCard == null -> {
                    // enemigo sin carta: cuenta como daño directo
                    totalDamageDealt += playerCard.damage
                    _enemy.value = _enemy.value.copy(currentHits = _enemy.value.currentHits - 1)
                }
                playerCard == null && enemyCard != null -> {
                    totalDamageReceived += enemyCard.damage
                    _player.value = _player.value.copy(currentHits = _player.value.currentHits - 1)
                }
            }
        }

        checkGameState()

        // Robar 2 cartas post-combate
        repeat(2) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }
    }

    private fun checkGameState() {
        val pHits = _player.value.currentHits
        val eHits = _enemy.value.currentHits
        val pCards = _player.value.deck.isNotEmpty() || _playerHand.isNotEmpty()
        val eCards = _enemy.value.deck.isNotEmpty() || _enemyHand.isNotEmpty()

        when {
            pHits <= 0 && eHits <= 0 -> { _gameState.value = GameState.DRAW;   _navigationEvent.value = "DRAW"   }
            pHits <= 0            -> { _gameState.value = GameState.DEFEAT; _navigationEvent.value = "DEFEAT" }
            eHits <= 0            -> { _gameState.value = GameState.VICTORY;_navigationEvent.value = "VICTORY"}
            !pCards && !eCards    -> {
                when {
                    pHits > eHits -> { _gameState.value = GameState.VICTORY; _navigationEvent.value = "VICTORY" }
                    pHits < eHits -> { _gameState.value = GameState.DEFEAT;  _navigationEvent.value = "DEFEAT"  }
                    else          -> { _gameState.value = GameState.DRAW;    _navigationEvent.value = "DRAW"    }
                }
            }
        }
    }

    private fun drawCard(deck: MutableList<Card>) = deck.removeFirstOrNull()

    private fun placeEnemyCardForTurn() {
        val empty = enemySlots.firstOrNull { it.isEmpty } ?: return
        if (_enemyHand.isNotEmpty()) {
            val c = _enemyHand.random()
            empty.placeCard(c)
            _enemyHand.remove(c)
        }
    }

    private fun placeInitialEnemyCard() {
        enemySlots.firstOrNull { it.isEmpty }?.let { slot ->
            _enemyHand.firstOrNull()?.also { c ->
                slot.placeCard(c)
                _enemyHand.remove(c)
            }
        }
    }

    /** Retorna un string resumen de las métricas para el log */
    fun getGameLogSummary(): String {
        return "Cartas eliminadas: $cardsEliminated\n" +
                "Daño infligido: $totalDamageDealt\n" +
                "Daño recibido: $totalDamageReceived"
    }

    fun resetNavigation() {
        _navigationEvent.value = null
    }
}
