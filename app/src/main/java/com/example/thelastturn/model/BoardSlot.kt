package com.example.thelastturn.model

data class BoardSlot(
    val id: Int,
    var card: Card? = null
) {
    val isEmpty: Boolean
        get() = card == null

    fun clear() {
        card = null
    }
}