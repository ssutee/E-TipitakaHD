package com.watnapp.etipitaka.plus.account

import android.content.Context

/** Persists the logged-in user's username + auth token in SharedPreferences. */
class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val token: String?
        get() = prefs.getString(KEY_TOKEN, null)

    val username: String?
        get() = prefs.getString(KEY_USERNAME, null)

    val isLoggedIn: Boolean
        get() = token != null && username != null

    fun save(username: String, token: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "account_preferences"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
    }
}
