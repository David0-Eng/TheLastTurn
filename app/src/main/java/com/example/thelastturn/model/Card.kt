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

            Card(1, "Guerrero", 3, 6, R.drawable.guerrero, 6),       // Tanque versátil
            Card(2, "Mago", 5, 3, R.drawable.mago, 3),              // Daño alto, vidafrágil
            Card(3, "Arquero", 4, 4, R.drawable.arquero, 4),        // Balance clásico
            Card(4, "Golem", 2, 9, R.drawable.golem, 9),            // Tanque definitivo
            Card(5, "Ninja", 6, 3, R.drawable.ninja, 3),            // Ataque rápido
            Card(6, "Berserker", 8, 1, R.drawable.berserker, 1),    // Vidafrágil, daño extremo
            Card(7, "Curandera", 1, 7, R.drawable.curandera, 7),    // Soporte curativo
            Card(8, "Asesino", 7, 2, R.drawable.asesino, 2),        // Eliminador de unidades
            Card(9, "Caballero", 4, 7, R.drawable.caballero, 7),    // Defensor equilibrado
            Card(10, "Dragón", 7, 6, R.drawable.dragon, 6),         // Poder legendario

            Card(11, "Escudero", 2, 8, R.drawable.escudero, 8),     // Defensa pura
            Card(13, "Lancero", 5, 5, R.drawable.lancero, 5),       // Balance ofensivo-defensivo
            Card(14, "Sabueso", 4, 2, R.drawable.sabueso, 2)        // Velocidad y emboscada
        )
    }

    fun withHealth(newHealth: Int): Card {
        return copy(health = newHealth.coerceAtLeast(0))
    }
}