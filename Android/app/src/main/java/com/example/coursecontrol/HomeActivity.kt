package com.example.coursecontrol

import AdminChecker
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.coursecontrol.adminPrivileges.UserDisplayActivity
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    private lateinit var adminChecker: AdminChecker
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)

        sessionManager = SessionManager(this)
        adminChecker = AdminChecker()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)
        val username = intent.getStringExtra("USERNAME_EXTRA")

        val textUserName = findViewById<TextView>(R.id.textUserName)
        textUserName.text = "Welcome, $username!"

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }

        val coursesImageView: ImageView = findViewById(R.id.courses)
        val reportsImageView: ImageView = findViewById(R.id.report)
        val usersImageView: ImageView = findViewById(R.id.users)

        coursesImageView.setOnClickListener {
            val intent = Intent(this, CoursesNavActivity::class.java)
            startActivity(intent)
        }

        reportsImageView.setOnClickListener {
            val intent = Intent(this, GenerateReportManagerActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    adminChecker.checkAdmin(sessionToken)
                    isAdmin = adminChecker.isAdmin()

                    Log.d("AdminChecker", "API call successful. Status: $isAdmin")

                    if (!isAdmin) {
                        usersImageView.visibility = View.INVISIBLE
                    } else {
                        usersImageView.visibility = View.VISIBLE
                    }

                } else {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        usersImageView.setOnClickListener {
            val intent = Intent(this, UserDisplayActivity::class.java)
            startActivity(intent)
        }
    }
}
