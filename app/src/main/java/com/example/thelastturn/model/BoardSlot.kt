package com.example.thelastturn.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BoardSlot(
    val id: Int,
    initialCard: Card? = null
) {
    private val _card = mutableStateOf(initialCard)
    var card: Card? by _card
        private set

    val isEmpty: Boolean
        get() = card == null

    fun clear() {
        card = null
    }

    // Metodos para gestionar las cartas

    fun placeCard(newCard: Card) {
        card = newCard
    }

    fun updateCard(updatedCard: Card) {
        card = updatedCard
    }
}