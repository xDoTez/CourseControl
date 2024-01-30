package com.example.coursecontrol

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.coursecontrol.util.SessionManager

object Logout {
    fun logoutUser(context: Context, intent: Intent){
        Toast.makeText(context, "Logout successful!", Toast.LENGTH_SHORT).show()
        val sessionManager = SessionManager(context)
        sessionManager.clearSession()
        context.startActivity(intent)
    }
}