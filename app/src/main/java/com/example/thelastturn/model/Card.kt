package com.example.thelastturn.model

import com.example.thelastturn.R

data class Card(
    val id: Int,
    val name: String,
    val damage: Int,
    var health: Int,
    var imageRes: Int
)
{
    companion object {
        fun sampleCards(): List<Card> = listOf(
            Card(1, "Guerrero", 3, 5, R.drawable.warrior),
            Card(2, "Mago", 2, 3, R.drawable.mage)
            // Agrega más cartas aquí
        )
    }
}