package com.example.thelastturn.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para obtener la instancia de DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesManager(private val context: Context) {

    // Claves para las preferencias
    private object PreferencesKeys {
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val NUM_SLOTS = intPreferencesKey("num_slots")
        val TOTAL_TIME = intPreferencesKey("total_time")
        val ACTION_TIME = intPreferencesKey("action_time")
    }

    // Flujos para observar los valores de las preferencias
    val playerName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PLAYER_NAME] ?: "" // Valor por defecto
        }

    val numSlots: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NUM_SLOTS] ?: 4 // Valor por defecto: 4 (para 4x4)
        }

    val totalTime: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TOTAL_TIME] ?: 180 // Valor por defecto: 180 segundos
        }

    val actionTime: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACTION_TIME] ?: 30 // Valor por defecto: 30 segundos
        }


    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_NAME] = name
        }
    }

    suspend fun saveGameSettings(numSlots: Int, totalTime: Int, actionTime: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NUM_SLOTS] = numSlots
            preferences[PreferencesKeys.TOTAL_TIME] = totalTime
            preferences[PreferencesKeys.ACTION_TIME] = actionTime
        }
    }
}