package com.example.thelastturn.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val PLAYER_NAME = stringPreferencesKey("player_name")
    }

    suspend fun savePlayerName(name: String) {
        dataStore.edit { settings ->
            settings[PLAYER_NAME] = name
        }
    }

    val playerName: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PLAYER_NAME] ?: ""
        }
}