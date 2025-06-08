package com.example.thelastturn.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thelastturn.data.GameRepository

/**
 * Factory para crear instancias de [GameViewModel] con dependencias.
 * Necesario para pasar [Application] y [GameRepository] al ViewModel.
 */
class GameViewModelFactory(
    private val application: Application,
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica que el ViewModel solicitado sea GameViewModel
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") // Suprime la advertencia de casting inseguro
            return GameViewModel(application, repository) as T
        }
        // Lanza una excepción si se intenta crear un ViewModel de una clase no soportada
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}