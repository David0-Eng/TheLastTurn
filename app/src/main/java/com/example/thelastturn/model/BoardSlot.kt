package com.example.thelastturn.model

data class BoardSlot(
    val id: Int,
    var card: Card? = null // Almacena el objeto Card directamente
)