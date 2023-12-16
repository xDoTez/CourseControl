package com.example.coursecontrol.util
import android.content.Context
import android.content.SharedPreferences
import com.example.coursecontrol.SessionToken

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("session_preferences", Context.MODE_PRIVATE)

    fun saveSessionToken(sessionToken: SessionToken) {
        with(sharedPreferences.edit()) {
            putString("expiration", sessionToken.expiration)
            putString("session_token", sessionToken.session_token)
            putInt("user", sessionToken.user)
            apply()
        }
    }

    fun getSessionToken(): SessionToken? {
        val expiration = sharedPreferences.getString("expiration", null)
        val sessionToken = sharedPreferences.getString("session_token", null)
        val user = sharedPreferences.getInt("user", -1)

        return if (expiration != null && sessionToken != null && user != -1) {
            SessionToken(expiration, sessionToken, user)
        } else {
            null
        }
    }

    fun clearSession() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}
