package com.example.coursecontrol

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coursecontrol.adminPrivileges.UserDisplayActivity
import com.example.coursecontrol.util.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity(): AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)
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
        val statsImageView: ImageView = findViewById(R.id.user16)
        val usersImageView: ImageView = findViewById(R.id.users)

        coursesImageView.setOnClickListener {

            val intent = Intent(this, CoursesNavActivity::class.java)
            startActivity(intent)
        }

        reportsImageView.setOnClickListener {

            val intent = Intent(this, GenerateReportManagerActivity::class.java)
            startActivity(intent)
        }

        statsImageView.setOnClickListener {
        }

        usersImageView.setOnClickListener {
            val intent = Intent(this, UserDisplayActivity::class.java)
            startActivity(intent)
        }

    }
}


