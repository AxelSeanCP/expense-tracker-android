package com.example.expensetracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreferenceManager(private val context: Context) {
    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")

    val isDarkTheme: Flow<Boolean> = context.themeDataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME] ?: false
        }

    suspend fun toggleTheme() {
        context.themeDataStore.edit { preferences ->
            val currentTheme = preferences[IS_DARK_THEME] ?: false
            preferences[IS_DARK_THEME] = !currentTheme
        }
    }
}