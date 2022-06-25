package com.connect.android.database

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserDatastore @Inject constructor(@ApplicationContext val context: Context) {

    // Keys
    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val TOKEN = stringPreferencesKey("token")
        val ID = intPreferencesKey("id")
        val EMAIL = stringPreferencesKey("email")
        val CODE = intPreferencesKey("code")
        val IS_VERIFIED = booleanPreferencesKey("is_verified")
    }

    // Read Theme
    val theme: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.THEME_MODE] ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

    // Read token
    val token: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.TOKEN]
        }

    // Read id
    val id: Flow<Int?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.ID]
        }

    // Read email
    val email: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.EMAIL]
        }

    // Read code
    val code: Flow<Int?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.CODE]
        }

    // Read isVerified
    val isVerified: Flow<Boolean?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[Keys.IS_VERIFIED]
        }

    // Update Theme
    suspend fun updateTheme(themeMode: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = themeMode
        }
    }

    // Update Token
    suspend fun updateToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TOKEN] = token
        }
    }

    // Update Id
    suspend fun updateId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ID] = id
        }
    }

    // Update Email
    suspend fun updateEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.EMAIL] = email
        }
    }

    // Update Code
    suspend fun updateCode(code: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CODE] = code
        }
    }

    // Update Is_Verified
    suspend fun updateIsVerified(isVerified: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.IS_VERIFIED] = isVerified
        }
    }

    // Delete Token
    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            if (preferences.contains(Keys.TOKEN)) preferences.remove(Keys.TOKEN)
        }
    }

    // Delete Id
    suspend fun deleteId() {
        context.dataStore.edit { preferences ->
            if (preferences.contains(Keys.ID)) preferences.remove(Keys.ID)
        }
    }

    // Delete Email
    suspend fun deleteEmail() {
        context.dataStore.edit { preferences ->
            if (preferences.contains(Keys.EMAIL)) preferences.remove(Keys.EMAIL)
        }
    }

    // Delete Code
    suspend fun deleteCode() {
        context.dataStore.edit { preferences ->
            if (preferences.contains(Keys.CODE)) preferences.remove(Keys.CODE)
        }
    }

    // Delete IsVerified
    suspend fun deleteIsVerified() {
        context.dataStore.edit { preferences ->
            if (preferences.contains(Keys.IS_VERIFIED)) preferences.remove(Keys.IS_VERIFIED)
        }
    }

}