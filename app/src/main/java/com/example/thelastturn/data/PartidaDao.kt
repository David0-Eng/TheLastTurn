package com.example.thelastturn.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface PartidaDao {
    @Query("SELECT * FROM partidas ORDER BY fecha DESC")
    fun getAllPartidas(): Flow<List<Partida>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(partida: Partida)

    @Delete
    suspend fun delete(partida: Partida)

    @Query("DELETE FROM partidas")
    suspend fun deleteAllPartidas()

    @Query("SELECT * FROM partidas WHERE id = :id")
    fun getPartidaById(id: String): Flow<Partida?> // Devuelve un Flow de una Partida o null
}