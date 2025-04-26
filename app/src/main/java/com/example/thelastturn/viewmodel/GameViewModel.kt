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

class GameViewModel : ViewModel() {

    var numberOfSlots by mutableStateOf(0); private set
    var remainingTotalTime by mutableStateOf(0)
    var remainingActionTime by mutableStateOf(0)
    private var isTimerRunning by mutableStateOf(false)

    var totalTimeSeconds by mutableStateOf(0)
    var actionTimeSeconds by mutableStateOf(0)

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
        numberOfSlots = numSlots

        totalTimeSeconds = totalTime
        actionTimeSeconds = actionTime

        remainingTotalTime = totalTimeSeconds
        remainingActionTime = actionTimeSeconds


        startTimers()

        // 1) Reset estado básico
        _gameState.value = GameState.ONGOING
        _currentTurn.value = PlayerTurn.PLAYER
        _navigationEvent.value = null

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
        isTimerRunning = true
        viewModelScope.launch {
            while (isTimerRunning && remainingTotalTime > 0) {
                delay(1000L)
                remainingTotalTime--

                // Lógica tiempo total agotado
                if (remainingTotalTime <= 0) {
                    _gameState.value = GameState.DRAW
                    _navigationEvent.value = "DRAW"
                    stopTimers()
                }
            }
        }

        viewModelScope.launch {
            while (isTimerRunning) {
                delay(1000L)
                if (remainingActionTime > 0) remainingActionTime--

                if (remainingActionTime <= 0) {
                    resetActionTimer()

                    // Cambiamos de turno...
                    _currentTurn.value = when (_currentTurn.value) {
                        PlayerTurn.PLAYER -> PlayerTurn.OPPONENT
                        PlayerTurn.OPPONENT -> PlayerTurn.PLAYER
                        else -> PlayerTurn.PLAYER
                    }

                    // Si ahora es el turno del oponente, ejecutamos su jugada
                    if (_currentTurn.value == PlayerTurn.PLAYER) {
                        _currentTurn.value = PlayerTurn.OPPONENT

                        // ───────────────────────────────────────
                        // Aquí metemos el “breve delay” para que
                        // el cambio de turno se note en UI
                        // ───────────────────────────────────────
                        viewModelScope.launch {
                            delay(2000L)                        // <— espera 2 s
                            placeEnemyCardForTurn()            // el enemigo coloca carta
                            _currentTurn.value = PlayerTurn.COMBAT

                            delay(1000L)                        // <— espera 1 s antes de combate
                            resolveCombat()                    // se ejecuta el combate

                            // Una vez acabado, volvemos al jugador
                            _currentTurn.value = PlayerTurn.PLAYER
                        }
                    }
                }
            }
        }
    }

    private fun resetActionTimer() {
        remainingActionTime = actionTimeSeconds
    }

    private fun stopTimers() {
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

            // Lanzamos la resolución con un breve delay
            viewModelScope.launch {
                _currentTurn.value = PlayerTurn.COMBAT
                delay(400L)                 // <— 0.4 s para que veas la carta en el tablero
                resolveCombat()
            }
        }
    }

    private fun resolveCombat() {
        playerSlots.zip(enemySlots).forEach { (p, e) ->
            val pc = p.card; val ec = e.card
            when {
                pc != null && ec != null -> {
                    pc.health -= ec.damage
                    ec.health -= pc.damage
                    if (pc.health <= 0) p.clear()
                    if (ec.health <= 0) e.clear()
                }
                pc != null && ec == null -> _enemy.value = _enemy.value.copy(currentHits = _enemy.value.currentHits - 1)
                pc == null && ec != null -> _player.value = _player.value.copy(currentHits = _player.value.currentHits - 1)
            }
        }

        checkGameState()

        // Roban 2 cartas
        repeat(2) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }

        // Juega enemigo
        placeEnemyCardForTurn()
        _currentTurn.value = PlayerTurn.PLAYER
    }

    private fun checkGameState() {
        val ph = _player.value.currentHits
        val eh = _enemy.value.currentHits
        val phCards = _player.value.deck.isNotEmpty() || _playerHand.isNotEmpty()
        val ehCards = _enemy.value.deck.isNotEmpty() || _enemyHand.isNotEmpty()

        when {
            ph <= 0 && eh <= 0 -> { _gameState.value = GameState.DRAW;   _navigationEvent.value = "DRAW"   }
            ph <= 0            -> { _gameState.value = GameState.DEFEAT; _navigationEvent.value = "DEFEAT" }
            eh <= 0            -> { _gameState.value = GameState.VICTORY;_navigationEvent.value = "VICTORY"}
            !phCards && !ehCards -> {
                when {
                    ph > eh -> { _gameState.value = GameState.VICTORY; _navigationEvent.value = "VICTORY" }
                    ph < eh -> { _gameState.value = GameState.DEFEAT;  _navigationEvent.value = "DEFEAT"  }
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
