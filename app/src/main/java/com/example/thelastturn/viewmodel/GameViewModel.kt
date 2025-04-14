package com.example.thelastturn.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    // Aquí irá el ganador
    val winner = mutableStateOf("Jugador 1") // Modificar más adelante

    // Enviar email
    fun sendEmail() {
        // Lógica enviar correo
    }
}