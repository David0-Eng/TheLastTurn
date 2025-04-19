package com.example.thelastturn.model

data class Game(
    var currentPlayer: Player,
    var opponent: Player,
    var turnCount: Int = 0,
    var gameState: GameState = GameState.ONGOING
)