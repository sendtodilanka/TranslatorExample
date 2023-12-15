package com.codebxlk.compose.translator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codebxlk.compose.translator.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreManager(context: Context) {

    companion object {
        val defaultThemeKey = intPreferencesKey(name = "default_theme_key")
        val materialYouKey = booleanPreferencesKey(name = "material_you_key")

        val sourceKey = stringPreferencesKey("source_language_key")
        val targetKey = stringPreferencesKey("target_language_key")
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        context.getString(R.string.app_name)
    )

    val dataStore = context.dataStore

    fun <T> readValue(key: Preferences.Key<T>): Flow<T?> {
        return dataStore.data.catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }.map {
            it[key]
        }
    }

    suspend fun <T> storeValue(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }
}