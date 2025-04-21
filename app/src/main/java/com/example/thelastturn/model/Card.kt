package com.example.thelastturn.model

import androidx.annotation.DrawableRes
import com.example.thelastturn.R

data class Card(
    val id: Int,
    val name: String,
    val damage: Int,
    var health: Int,
    @DrawableRes val imageRes: Int,
    val maxHealth: Int
) {
    companion object {
        fun sampleDeck(): MutableList<Card> = mutableListOf(
            Card(1, "Guerrero", 3, 5, R.drawable.warrior, 5),
            Card(2, "Mago", 5, 3, R.drawable.mage, 3),
            Card(3, "Arquero", 4, 4, R.drawable.default_card, 4),
            Card(4, "Golem", 2, 8, R.drawable.default_card, 8),
            Card(5,"Tralalero", 3, 6, R.drawable.default_card, 6),
            Card(6,"Bombardilo", 4, 4, R.drawable.default_card, 4)
        )
    }
}