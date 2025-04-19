package com.example.thelastturn.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.thelastturn.R
import com.example.thelastturn.model.BoardSlot
import com.example.thelastturn.model.Card
import com.example.thelastturn.model.GameState

class GameViewModel : ViewModel() {
    val gameState = mutableStateOf(GameState.ONGOING)
    val playerHand = mutableStateListOf<Card>()
    val enemyHand = mutableStateListOf<Card>()

    val playerSlots = mutableStateListOf(
        BoardSlot(1), BoardSlot(2), BoardSlot(3), BoardSlot(4)
    )

    val enemySlots = mutableStateListOf(
        BoardSlot(5), BoardSlot(6), BoardSlot(7), BoardSlot(8)
    )

    val selectedCard = mutableStateOf<Card?>(null)

    init {
        val initialDeck = Card.sampleDeck()
        playerHand.addAll(initialDeck.take(4))
        enemyHand.addAll(listOf(
            Card(5, "Enemigo", 2, 6, R.drawable.default_card),
            Card(6, "Esqueleto", 3, 4, R.drawable.default_card)
        ))
    }

    fun selectCard(card: Card) {
        selectedCard.value = card
    }

    fun resetSelection() {
        selectedCard.value = null
    }

    fun placeCard(slotId: Int) {
        val card = selectedCard.value ?: return
        val targetSlot = playerSlots.firstOrNull { it.id == slotId }

        targetSlot?.let { slot ->
            if (slot.card == null) {
                slot.card = card // Asignar la Card al slot
                playerHand.remove(card)
                resetSelection()
            }
        }
    }
}