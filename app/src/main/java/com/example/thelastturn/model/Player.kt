package com.example.thelastturn.model

data class Player(
    val name: String,
    var health: Int,
    val deck: List<Card>
)