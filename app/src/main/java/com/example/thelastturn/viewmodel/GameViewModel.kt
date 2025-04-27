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
import kotlinx.coroutines.Job                                     //Consultar si se puede usar

class GameViewModel : ViewModel() {

    var numberOfSlots by mutableStateOf(0); private set
    var remainingTotalTime by mutableStateOf(0)             //Variable para modificar el tiempo total
    var remainingActionTime by mutableStateOf(0)            //Variable para modificar el tiempo de acción
    private var isTimerRunning by mutableStateOf(false)     //Variable para controlar el inicio del temporizador

    var totalTimeSeconds by mutableStateOf(0)               //Variable para recuperar el tiempo total
    var actionTimeSeconds by mutableStateOf(0)              //Variable para recuperar el tiempo de acción

    private var totalTimerJob: Job? = null                        //Consultar si se puede usar Job
    private var actionTimerJob: Job? = null                       //Consultar si se puede usar Job

    // Estado de la partida
    private val _gameState = mutableStateOf(GameState.ONGOING)
    val gameState get() = _gameState

    // Jugadores
    private val _player = mutableStateOf(Player("Jugador", 3, 3, mutableListOf()))
    val player: Player get() = _player.value

    private val _enemy = mutableStateOf(Player("Enemigo", 3, 3, mutableListOf()))
    val enemy: Player get() = _enemy.value

    // Turno actual
    private val _currentTurn = mutableStateOf(PlayerTurn.PLAYER)
    val currentTurn get() = _currentTurn

    // Slots
    val playerSlots = mutableStateListOf<BoardSlot>()
    val enemySlots  = mutableStateListOf<BoardSlot>()

    // Manos
    private val _playerHand = mutableStateListOf<Card>()
    val playerHand get() = _playerHand

    private val _enemyHand = mutableStateListOf<Card>()
    val enemyHand get() = _enemyHand

    // Carta seleccionada
    private val _selectedCard = mutableStateOf<Card?>(null)
    val selectedCard get() = _selectedCard

    // Evento de navegación al final
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent get() = _navigationEvent

    /** Arranca de cero un nuevo juego con `numSlots` y nombre de jugador */
    fun startNewGame(
        numSlots: Int,
        playerName: String,
        totalTime: Int,
        actionTime: Int
    ) {
        stopTimers()


        numberOfSlots = numSlots

        totalTimeSeconds = totalTime
        actionTimeSeconds = actionTime

        remainingTotalTime = totalTimeSeconds
        remainingActionTime = actionTimeSeconds


        // 1) Reset estado básico
        _gameState.value = GameState.ONGOING
        _currentTurn.value = PlayerTurn.PLAYER
        _navigationEvent.value = null

        startTimers()

        // 2) Prepara mazos fresh
        val baseDeck: List<Card> = Card.sampleDeck()

        // Para el jugador: mapeamos y copiamos cada carta
        val playerDeck: MutableList<Card> = baseDeck
            .map { card ->
                card.copy()       // hace una copia independiente
            }
            .toMutableList()

        // Para el enemigo: barajamos y luego copiamos
        val enemyDeck: MutableList<Card> = baseDeck
            .shuffled()
            .map { card ->
                card.copy()
            }
            .toMutableList()


        _player.value = Player(playerName,   3, 3, playerDeck)
        _enemy.value  = Player("Enemigo",    3, 3, enemyDeck)

        // 3) Slots dinámicos
        playerSlots.clear()
        enemySlots.clear()
        repeat(numSlots) { i ->
            playerSlots.add(BoardSlot(i + 1))
            enemySlots.add(BoardSlot(i + 1 + numSlots))
        }

        // 4) Manos y selección limpia
        _playerHand.clear()
        _enemyHand.clear()
        _selectedCard.value = null

        // 5) Roba inicial y coloca inicial del enemigo
        drawInitialCards()
        placeInitialEnemyCard()
    }

    private fun startTimers() {
        stopTimers()            //Parar jobs anteriores
        isTimerRunning = true

        totalTimerJob = viewModelScope.launch {
            while (isTimerRunning && remainingTotalTime > 0) {
                delay(1000L)
                remainingTotalTime--
                if (remainingTotalTime <= 0) {
                    _gameState.value = GameState.DRAW
                    _navigationEvent.value = "DRAW"
                    stopTimers()
                }
            }
        }

        // Temporizador de acción
        actionTimerJob = viewModelScope.launch {
            while (isTimerRunning && _gameState.value == GameState.ONGOING) {
                resetActionTimer()

                while (remainingActionTime > 0 && _gameState.value == GameState.ONGOING) {
                    delay(1000L)
                    remainingActionTime--
                }

                if (_gameState.value != GameState.ONGOING) break

                // Cambio de turno automático
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

            // Lanzamos la resolución con un breve delay
            viewModelScope.launch {
                _currentTurn.value = PlayerTurn.COMBAT
                delay(400L)                 // <— 0.4 s para que veas la carta en el tablero

                resolveCombat()
                placeEnemyCardForTurn()


                _currentTurn.value = PlayerTurn.PLAYER
                resetActionTimer()
            }
        }
    }

    private fun resolveCombat() {

        println("=== ANTES DE COMBATIR ===")
        println("Player slots: ${playerSlots.map { it.card?.name + "(" + it.card?.health + ")" }}")
        println("Enemy  slots: ${enemySlots .map { it.card?.name + "(" + it.card?.health + ")" }}")


        playerSlots.zip(enemySlots).forEach { (playerSlot, enemySlot) ->
            val playerCard = playerSlot.card
            val enemyCard = enemySlot.card

            when {
                playerCard != null && enemyCard != null -> {
                    // Crear nuevas instancias con salud actualizada
                    val updatedPlayerCard = playerCard.withHealth(playerCard.health - enemyCard.damage)
                    val updatedEnemyCard = enemyCard.withHealth(enemyCard.health - playerCard.damage)

                    // Actualizar slots con las nuevas instancias
                    playerSlot.updateCard(updatedPlayerCard)
                    enemySlot.updateCard(updatedEnemyCard)

                    // Eliminar cartas destruidas
                    if (updatedPlayerCard.health <= 0) playerSlot.clear()
                    if (updatedEnemyCard.health <= 0) enemySlot.clear()
                }
                playerCard != null && enemyCard == null -> _enemy.value = _enemy.value.copy(currentHits = _enemy.value.currentHits - 1)
                playerCard == null && enemyCard != null -> _player.value = _player.value.copy(currentHits = _player.value.currentHits - 1)
            }
        }

        // --- LOG AFTER ---
        println("=== DESPUÉS DE COMBATIR ===")
        println("Player slots: ${playerSlots.map { it.card?.name + "(" + it.card?.health + ")" }}")
        println("Enemy  slots: ${enemySlots .map { it.card?.name + "(" + it.card?.health + ")" }}")

        checkGameState()

        // Roban 2 cartas
        repeat(2) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }


    }

    private fun checkGameState() {
        val playerHits = _player.value.currentHits
        val enemyHits = _enemy.value.currentHits
        val playerHandCards = _player.value.deck.isNotEmpty() || _playerHand.isNotEmpty()
        val enemyHandCards = _enemy.value.deck.isNotEmpty() || _enemyHand.isNotEmpty()

        when {
            playerHits <= 0 && enemyHits <= 0 -> { _gameState.value = GameState.DRAW;   _navigationEvent.value = "DRAW"   }
            playerHits <= 0            -> { _gameState.value = GameState.DEFEAT; _navigationEvent.value = "DEFEAT" }
            enemyHits <= 0            -> { _gameState.value = GameState.VICTORY;_navigationEvent.value = "VICTORY"}
            !playerHandCards && !enemyHandCards -> {
                when {
                    playerHits > enemyHits -> { _gameState.value = GameState.VICTORY; _navigationEvent.value = "VICTORY" }
                    playerHits < enemyHits -> { _gameState.value = GameState.DEFEAT;  _navigationEvent.value = "DEFEAT"  }
                    else    -> { _gameState.value = GameState.DRAW;    _navigationEvent.value = "DRAW"    }
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

    fun resetNavigation() {
        _navigationEvent.value = null
    }

    private fun logState() { /*…*/ }
}
