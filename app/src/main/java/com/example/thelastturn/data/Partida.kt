package com.example.thelastturn.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "partidas")
data class Partida(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val alias: String,
    val fecha: String,
    val resultado: String,
    val tamanoTablero: Int,
    val danioInfligido: Int,
    val danioRecibido: Int,
    val cartasEliminadas: Int
) : Parcelable