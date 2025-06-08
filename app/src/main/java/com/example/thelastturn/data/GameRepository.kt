package com.example.thelastturn.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val partidaDao: PartidaDao) {
    val allPartidas: Flow<List<Partida>> = partidaDao.getAllPartidas()

    suspend fun insertPartida(partida: Partida) {
        partidaDao.insert(partida)
    }

    suspend fun deletePartida(partida: Partida) {
        partidaDao.delete(partida)
    }

    fun getPartidaById(id: String): Flow<Partida?> {
        return partidaDao.getPartidaById(id)
    }
}