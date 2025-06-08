package com.example.thelastturn.model

enum class GameState {
    LOADING,   // Nuevo estado: el juego se está inicializando o cargando datos.
    ONGOING,
    VICTORY,
    DEFEAT,
    DRAW,
    TIME_OVER,
    ERROR      // Nuevo estado: ocurrió un error crítico que impidió que el juego continuara.
}