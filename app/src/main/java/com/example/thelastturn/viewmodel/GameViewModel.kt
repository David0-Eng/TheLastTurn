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

    private val _player = mutableStateOf(Player("Jugador", 30, mutableListOf()))
    val player get() = _player

    private val _enemy = mutableStateOf(Player("Enemigo", 30, mutableListOf()))
    val enemy get() = _enemy

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
    }

    fun resetSelection() {
        _selectedCard.value = null
    }

    private fun drawInitialCards() {
        repeat(3) {
            _playerHand.add(drawCard(_player.value.deck)!!)
            _enemyHand.add(drawCard(_enemy.value.deck)!!)
        }
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
    }

    fun placeCard(slotId: Int) {
        val card = _selectedCard.value ?: return
        val slot = playerSlots.firstOrNull { it.id == slotId } ?: return

        if (slot.isEmpty && _currentTurn.value == PlayerTurn.PLAYER) {
            slot.placeCard(card) // Usar método en lugar de asignación directa
            _playerHand.remove(card)
            _selectedCard.value = null
            _currentTurn.value = PlayerTurn.OPPONENT
            placeEnemyCards()
        }
    }

    private fun placeEnemyCards() {
        val availableCards = _enemyHand.toMutableList()
        // Buscar el primer slot vacío disponible
        val emptySlot = enemySlots.firstOrNull { it.isEmpty }
        if (emptySlot != null && availableCards.isNotEmpty()) {
            val card = availableCards.random()
            emptySlot.placeCard(card)
            _enemyHand.remove(card)
            availableCards.remove(card)
            println("Enemigo colocó: ${card.name}")
            logState()
            // Cambiar al turno de combate después de cada colocación
            _currentTurn.value = PlayerTurn.COMBAT
            resolveCombat()
        } else {
            // Si no hay slots o cartas, pasar directamente al combate
            _currentTurn.value = PlayerTurn.COMBAT
            resolveCombat()
        }
    }

    private fun resolveCombat() {
        // Resolver enfrentamientos
        playerSlots.zip(enemySlots).forEach { (playerSlot, enemySlot) ->
            playerSlot.card?.let { playerCard ->
                enemySlot.card?.let { enemyCard ->
                    // Combate entre cartas
                    playerCard.health -= enemyCard.damage
                    enemyCard.health -= playerCard.damage

                    if (playerCard.health <= 0) playerSlot.clear()
                    if (enemyCard.health <= 0) enemySlot.clear()
                } ?: run {
                    // Daño directo al enemigo
                    _enemy.value = _enemy.value.copy(health = _enemy.value.health - playerCard.damage)
                }
            }

            enemySlot.card?.let { enemyCard ->
                playerSlot.card ?: run {
                    // Daño directo al jugador
                    _player.value = _player.value.copy(health = _player.value.health - enemyCard.damage)
                }
            }
        }

        checkGameState()
        prepareNextTurn()
    }

    private fun checkGameState() {
        when {
            _player.value.health <= 0 -> {
                _gameState.value = GameState.DEFEAT
                _navigationEvent.value = "DEFEAT"
            }
            _enemy.value.health <= 0 -> {
                _gameState.value = GameState.VICTORY
                _navigationEvent.value = "VICTORY"
            }
            playerSlots.none { it.card != null } && enemySlots.none { it.card != null } -> {
                _gameState.value = GameState.DRAW
                _navigationEvent.value = "DRAW"
            }
        }
    }

    private fun prepareNextTurn() {


        repeat(2) {
            drawCard(_player.value.deck)?.let { _playerHand.add(it) }
            drawCard(_enemy.value.deck)?.let { _enemyHand.add(it) }
        }

        _currentTurn.value = PlayerTurn.PLAYER
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