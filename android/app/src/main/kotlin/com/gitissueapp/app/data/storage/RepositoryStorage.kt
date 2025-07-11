package com.gitissueapp.app.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "repository_prefs")

@Singleton
class RepositoryStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    suspend fun saveRepository(owner: String, repo: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OWNER] = owner
            preferences[KEY_REPO] = repo
        }
    }

    fun getOwner(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_OWNER]
    }

    fun getRepo(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_REPO]
    }

    fun getRepository(): Flow<Pair<String?, String?>> = dataStore.data.map { preferences ->
        Pair(preferences[KEY_OWNER], preferences[KEY_REPO])
    }

    suspend fun clearRepository() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_OWNER)
            preferences.remove(KEY_REPO)
        }
    }

    companion object {
        private val KEY_OWNER = stringPreferencesKey("repository_owner")
        private val KEY_REPO = stringPreferencesKey("repository_name")
    }
}