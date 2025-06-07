package com.example.thelastturn.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val partidaDao: PartidaDao) {
    val allPartidas: Flow<List<Partida>> = partidaDao.getAllPartidas()

    suspend fun insert(partida: Partida) {
        partidaDao.insert(partida)
    }
}