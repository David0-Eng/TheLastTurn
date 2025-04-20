package com.example.thelastturn.model

data class Player(
    val name: String,
    val maxHits: Int,
    var currentHits: Int,
    val deck: MutableList<Card>
)