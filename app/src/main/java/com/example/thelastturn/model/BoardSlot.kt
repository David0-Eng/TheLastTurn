package com.example.thelastturn.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BoardSlot(
    val id: Int,
    initialCard: Card? = null
) {
    var card by mutableStateOf(initialCard) // <-- ¡Clave!
        private set

    val isEmpty: Boolean
        get() = card == null

    fun clear() {
        card = null
    }

    fun placeCard(newCard: Card) {
        card = newCard
    }
}