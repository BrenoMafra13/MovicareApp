package ca.gbc.comp3074.movicareapp.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {
    companion object {
        private val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        private val KEY_USER_ID   = intPreferencesKey("user_id")
        private val KEY_USERNAME  = stringPreferencesKey("username")
        private val KEY_ROLE      = stringPreferencesKey("role")
    }

    val session = context.dataStore.data.map { pref ->
        SessionState(
            loggedIn = pref[KEY_LOGGED_IN] ?: false,
            userId   = pref[KEY_USER_ID],
            username = pref[KEY_USERNAME],
            role     = pref[KEY_ROLE]
        )
    }

    suspend fun save(userId: Int, username: String, role: String) {
        context.dataStore.edit { p ->
            p[KEY_LOGGED_IN] = true
            p[KEY_USER_ID]   = userId
            p[KEY_USERNAME]  = username
            p[KEY_ROLE]      = role
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

data class SessionState(
    val loggedIn: Boolean,
    val userId: Int?,
    val username: String?,
    val role: String?
)
