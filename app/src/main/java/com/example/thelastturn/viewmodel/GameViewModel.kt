package com.example.thelastturn.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.GameState
import com.example.thelastturn.model.Player
import com.example.thelastturn.model.PlayerTurn

class GameViewModel : ViewModel() {
    // Estados observables
    private val _gameState = mutableStateOf(GameState.ONGOING)
    val gameState get() = _gameState

    val initialDeck = Card.sampleDeck().toMutableList()

    private val _player = mutableStateOf(Player("Jugador", 3, 3, initialDeck))
    val player: Player get() = _player.value

    private val _enemy = mutableStateOf(Player("Enemigo", 3, 3, initialDeck.shuffled().toMutableList()))
    val enemy: Player get() = _enemy.value

    private val _currentTurn = mutableStateOf(PlayerTurn.PLAYER)
    val currentTurn get() = _currentTurn

    // Slots y manos
    val playerSlots = mutableStateListOf<BoardSlot>()
    val enemySlots = mutableStateListOf<BoardSlot>()

    private val _playerHand = mutableStateListOf<Card>()
    val playerHand get() = _playerHand

    private val _enemyHand = mutableStateListOf<Card>()
    val enemyHand get() = _enemyHand

    private val _selectedCard = mutableStateOf<Card?>(null)
    val selectedCard get() = _selectedCard

    // Navegación
    private val _navigationEvent = mutableStateOf<String?>(null)
    val navigationEvent get() = _navigationEvent

    init {
        val initialDeck = Card.sampleDeck().toMutableList()
        _player.value = _player.value.copy(deck = initialDeck)
        _enemy.value = _enemy.value.copy(deck = initialDeck.shuffled().toMutableList())

        // Inicializar los slots observables
        repeat(4) { playerSlots.add(BoardSlot(it + 1)) }
        repeat(4) { enemySlots.add(BoardSlot(it + 5)) }

        drawInitialCards()

        placeInitialEnemyCard()

        _currentTurn.value = PlayerTurn.PLAYER
    }


    private fun drawInitialCards() {
        repeat(3) {
            val playerCard = drawCard(_player.value.deck)
            if (playerCard != null) {
                _playerHand.add(playerCard)
            } else {
                println("ADVERTENCIA: Mazo del jugador vacío al reiniciar")
            }
            val enemyCard = drawCard(_enemy.value.deck)
            if (enemyCard != null) {
                _enemyHand.add(enemyCard)
            } else {
                println("ADVERTENCIA: Mazo del enemigo vacío al reiniciar")
            }
        }
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
    }

    fun placeCard(slotId: Int) {
        val card = _selectedCard.value ?: return
        val slot = playerSlots.firstOrNull { it.id == slotId } ?: return

        if (slot.isEmpty && _currentTurn.value == PlayerTurn.PLAYER) {
            // Jugador coloca carta
            slot.placeCard(card)
            _playerHand.remove(card)
            _selectedCard.value = null

            // Cambia el turno a combate y resuelve
            _currentTurn.value = PlayerTurn.COMBAT
            resolveCombat()
        }
    }

    private fun placeEnemyCardForTurn() {
        val availableCards = _enemyHand.toMutableList()
        val emptySlot = enemySlots.firstOrNull { it.isEmpty }
        if (emptySlot != null && availableCards.isNotEmpty()) {
            val card = availableCards.random()
            emptySlot.placeCard(card)
            _enemyHand.remove(card)
            println("Enemigo colocó: ${card.name}")
            logState()
        }
    }

    private fun placeInitialEnemyCard() {
        val card = _enemyHand.firstOrNull() ?: return
        val slot = enemySlots.firstOrNull { it.isEmpty } ?: return
        slot.placeCard(card)
        _enemyHand.remove(card)
        println("El enemigo colocó una carta inicial: ${card.name}")
    }

    private fun resolveCombat() {
        playerSlots.zip(enemySlots).forEach { (playerSlot, enemySlot) ->
            val playerCard = playerSlot.card
            val enemyCard = enemySlot.card

            when {
                playerCard != null && enemyCard != null -> {
                    playerCard.health -= enemyCard.damage
                    enemyCard.health -= playerCard.damage
                    if (playerCard.health <= 0) playerSlot.clear()
                    if (enemyCard.health <= 0) enemySlot.clear()
                }
                playerCard != null && enemyCard == null -> {
                    _enemy.value = _enemy.value.copy(currentHits = _enemy.value.currentHits - 1)
                }
                playerCard == null && enemyCard != null -> {
                    _player.value = _player.value.copy(currentHits = _player.value.currentHits - 1)
                }
            }
        }

        checkGameState()

        // Ambos roban 2 cartas
        repeat(2) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }

        // El enemigo juega ahora su carta del nuevo turno
        placeEnemyCardForTurn()

        // Se pasa el turno al jugador
        _currentTurn.value = PlayerTurn.PLAYER
    }

    private fun checkGameState() {
        when {
            _player.value.currentHits <= 0 -> {
                _gameState.value = GameState.DEFEAT
                _navigationEvent.value = "DEFEAT"
            }
            _enemy.value.currentHits <= 0 -> {
                _gameState.value = GameState.VICTORY
                _navigationEvent.value = "VICTORY"
            }
        }
    }


    private fun drawCard(deck: MutableList<Card>): Card? {
        return if (deck.isNotEmpty()) deck.removeAt(0) else null
    }

    fun resetNavigation() {
        _navigationEvent.value = null
    }

    private fun logState() {
        println("Turno actual: ${_currentTurn.value}")
        println("Mano enemiga: ${_enemyHand.size} cartas")
        println("Slots enemigos: ${enemySlots.map { it.card?.name }}")
    }
}