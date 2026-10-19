package com.example.evaluacion2.data.local

import android.content.Context
import com.example.evaluacion2.domain.model.User

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveSession(user: User) {
        preferences.edit().putInt(KEY_USER_ID, user.id).putString(KEY_NAME, user.name).putString(KEY_ALIAS, user.alias)
            .putString(KEY_EMAIL, user.email).putString(KEY_ROLE, user.role).putString(KEY_PHONE, user.phone).putBoolean(KEY_LOGGED_IN, true).apply()
    }
    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_LOGGED_IN, false)
    fun getUserId(): Int = preferences.getInt(KEY_USER_ID, NO_USER_ID)
    fun getUserName(): String? = preferences.getString(KEY_NAME, null)
    fun getUserAlias(): String? = preferences.getString(KEY_ALIAS, null)
    fun getUserEmail(): String? = preferences.getString(KEY_EMAIL, null)
    fun getUserRole(): String? = preferences.getString(KEY_ROLE, null)

    fun updateSessionProfile(user: User) {
        preferences.edit().putString(KEY_NAME, user.name).putString(KEY_ALIAS, user.alias).putString(KEY_EMAIL, user.email).putString(KEY_PHONE, user.phone).apply()
    }

    fun clearSession() { preferences.edit().clear().apply() }

    companion object {
        private const val PREFERENCES_NAME = "lou_araya_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_ALIAS = "alias"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_ROLE = "role"
        private const val KEY_LOGGED_IN = "logged_in"
        const val NO_USER_ID = -1
    }
}
