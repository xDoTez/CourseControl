package com.example.coursecontrol.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem

import com.example.coursecontrol.HomeActivity
import com.example.coursecontrol.Logout
import com.example.coursecontrol.MainActivity
import com.example.coursecontrol.R

class NavigationHandler(private val context: Context) {

    fun handleItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                Logout.logoutUser(context, Intent(context, MainActivity::class.java))
                return true
            }
            R.id.home -> {
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
                return true
            }
            R.id.back -> {
                if (context is Activity) {
                    context.onBackPressed()
                }
                return true
            }
            else -> return false
        }
    }
}