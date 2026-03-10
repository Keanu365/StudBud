package io.github.keanu365.studbud

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

expect fun producePath(): String
fun createDataStore(): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

internal const val dataStoreFileName = "studbud.preferences_pb"

class AppPreferences(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val KEY_SIGNED_IN = booleanPreferencesKey("signed_in")
        private val KEY_FIRST_TIME_USER = booleanPreferencesKey("first_time_user")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    val signedIn: Flow<Boolean> = dataStore.data.map { it[KEY_SIGNED_IN] ?: false }
    val firstTimeUser: Flow<Boolean> = dataStore.data.map { it[KEY_FIRST_TIME_USER] ?: true }
    val userEmail: Flow<String> = dataStore.data.map { it[KEY_USER_EMAIL] ?: "" }
    val username: Flow<String> = dataStore.data.map { it[KEY_USERNAME] ?: "" }

    suspend fun saveSignIn(email: String, username: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USERNAME] = username
            preferences[KEY_SIGNED_IN] = true
        }
    }
    suspend fun signOut() {
        supabase.auth.signOut(scope = SignOutScope.LOCAL)
        dataStore.edit { preferences ->
            preferences[KEY_USER_EMAIL] = ""
            preferences[KEY_USERNAME] = ""
            preferences[KEY_SIGNED_IN] = false
        }
    }

    suspend fun setFirstTimeUser(isFirstTime: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_FIRST_TIME_USER] = isFirstTime
        }
    }

    suspend fun clearData() {
        dataStore.edit { it.clear() }
    }
}
