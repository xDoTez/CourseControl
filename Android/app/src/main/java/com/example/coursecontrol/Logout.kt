package com.example.coursecontrol

import android.content.Context
import android.content.Intent
import android.widget.Toast

object Logout {
    public fun logoutUser(context: Context, intent: Intent){
        Toast.makeText(context, "Logout successful!", Toast.LENGTH_SHORT).show()
        context.startActivity(intent);
    }
}