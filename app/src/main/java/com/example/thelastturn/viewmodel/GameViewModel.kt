package com.example.thelastturn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _currentPlayer = MutableStateFlow(Player("Jugador", 20, Card.sampleCards()))
    private val _opponent = MutableStateFlow(Player("Enemigo", 20, Card.sampleCards()))
    private val _currentTurn = MutableStateFlow(PlayerTurn.PLAYER)
    private val _gameState = MutableStateFlow(GameState.ONGOING)

    val currentPlayer: StateFlow<Player> = _currentPlayer
    val opponent: StateFlow<Player> = _opponent
    val currentTurn: StateFlow<PlayerTurn> = _currentTurn
    val gameState: StateFlow<GameState> = _gameState

    fun attack(attacker: Card) {
        if (_gameState.value != GameState.ONGOING) return
        if (_currentTurn.value != PlayerTurn.PLAYER) return

        viewModelScope.launch {
            // Buscar carta rival en la misma posición
            val opponentCard = _opponent.value.deck.find { it.id == attacker.id }

            if (opponentCard != null) {
                opponentCard.health -= attacker.damage
                if (opponentCard.health <= 0) {
                    _opponent.value = _opponent.value.copy(
                        deck = _opponent.value.deck.filter { it.id != opponentCard.id }
                    )
                }
            } else {
                _opponent.value = _opponent.value.copy(
                    health = _opponent.value.health - attacker.damage
                )
            }

            checkGameOver()
            if (_gameState.value == GameState.ONGOING) {
                switchTurn()
                opponentTurn()
            }
        }
    }

    private fun checkGameOver() {
        when {
            _opponent.value.health <= 0 -> _gameState.value = GameState.VICTORY
            _currentPlayer.value.health <= 0 -> _gameState.value = GameState.DEFEAT
            _currentPlayer.value.deck.isEmpty() && _opponent.value.deck.isEmpty() ->
                _gameState.value = GameState.DRAW
        }
    }

    private fun switchTurn() {
        _currentTurn.value = when (_currentTurn.value) {
            PlayerTurn.PLAYER -> PlayerTurn.OPPONENT
            PlayerTurn.OPPONENT -> PlayerTurn.PLAYER
        }
    }

    private suspend fun opponentTurn() {
        delay(1000) // Simula tiempo de espera del oponente
        // Lógica simple de IA (ataque aleatorio)
        val availableCards = _opponent.value.deck.filter { it.health > 0 }
        if (availableCards.isNotEmpty()) {
            val randomCard = availableCards.random()
            // Aquí implementar lógica de contraataque
        }
        switchTurn()
    }
}

enum class PlayerTurn { PLAYER, OPPONENT }
enum class GameState { ONGOING, VICTORY, DEFEAT, DRAW }