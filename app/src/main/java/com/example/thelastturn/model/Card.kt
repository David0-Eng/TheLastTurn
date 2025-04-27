package com.example.thelastturn.model

import androidx.annotation.DrawableRes
import com.example.thelastturn.R

data class Card(
    val id: Int,
    val name: String,
    val damage: Int,
    val health: Int,
    @DrawableRes val imageRes: Int,
    val maxHealth: Int
) {
    companion object {
        fun sampleDeck(): MutableList<Card> = mutableListOf(
            Card(1, "Guerrero", 3, 5, R.drawable.warrior, 5),
            Card(2, "Mago", 5, 3, R.drawable.mage, 3),
            Card(3, "Arquero", 4, 4, R.drawable.arquero, 4),
            Card(4, "Golem", 2, 8, R.drawable.golem, 8),
            Card(7, "Curandera", 1, 6, R.drawable.curandera, 6),
            Card(8, "Asesino", 6, 2, R.drawable.asesino, 2),
            Card(9, "Caballero", 4, 6, R.drawable.caballero, 6),
            Card(10,"Dragón", 7, 7, R.drawable.dragon, 7)
        )
    }

    fun withHealth(newHealth: Int): Card {
        return copy(health = newHealth.coerceAtLeast(0))
    }
}