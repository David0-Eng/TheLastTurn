package com.example.thelastturn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDao {
    @Insert
    suspend fun insert(partida: Partida)

    @Query("SELECT * FROM partidas ORDER BY fecha DESC")
    fun getAllPartidas(): Flow<List<Partida>>
}