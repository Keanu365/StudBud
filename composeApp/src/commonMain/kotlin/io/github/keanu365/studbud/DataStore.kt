package io.github.keanu365.studbud

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.keanu365.studbud.viewmodels.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
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
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        //Sorry that there was no better way to do this :(
        private val KEY_RAW_USER_DATA = stringPreferencesKey("raw_user_data")
        private val KEY_RAW_GROUPS_DATA = stringPreferencesKey("raw_groups_data")
        private val KEY_RAW_ASSIGNMENTS_DATA = stringPreferencesKey("raw_assignments_data")
        private val KEY_SETTINGS = stringPreferencesKey("settings")
    }

    val signedIn: Flow<Boolean> = dataStore.data.map { it[KEY_SIGNED_IN] ?: false }
    val firstTimeUser: Flow<Boolean> = dataStore.data.map { it[KEY_FIRST_TIME_USER] ?: true }
    val userId: Flow<String> = dataStore.data.map { it[KEY_USER_ID] ?: "" }
    val rawUserData: Flow<String> = dataStore.data.map { it[KEY_RAW_USER_DATA] ?: "" }
    val rawGroupsData: Flow<String> = dataStore.data.map { it[KEY_RAW_GROUPS_DATA] ?: "" }
    val rawAssignmentsData: Flow<String> = dataStore.data.map { it[KEY_RAW_ASSIGNMENTS_DATA] ?: "" }
    val settings: Flow<String> = dataStore.data.map { it[KEY_SETTINGS] ?: "" }


    suspend fun saveSignIn(id: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
            preferences[KEY_SIGNED_IN] = true
        }
    }
    suspend fun signOut() {
        supabase.auth.signOut(scope = SignOutScope.LOCAL)
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = ""
            preferences[KEY_SIGNED_IN] = false
            preferences[KEY_RAW_USER_DATA] = ""
            preferences[KEY_RAW_GROUPS_DATA] = ""
            preferences[KEY_RAW_ASSIGNMENTS_DATA] = ""
        }
    }

    suspend fun setFirstTimeUser(isFirstTime: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_FIRST_TIME_USER] = isFirstTime
        }
    }

    suspend fun saveRawUserData(
        user: User?,
        groups: List<Group>,
        assignments: List<Assignment>
    ){
        dataStore.edit { preferences ->
            preferences[KEY_RAW_USER_DATA] = Json.encodeToString(user).also { println(it) }
            preferences[KEY_RAW_GROUPS_DATA] = Json.encodeToString(groups).also{ println(it) }
            preferences[KEY_RAW_ASSIGNMENTS_DATA] = Json.encodeToString(assignments).also{ println(it) }
        }
    }

    suspend fun setSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[KEY_SETTINGS] = Json.encodeToString(settings)
        }
    }
}
