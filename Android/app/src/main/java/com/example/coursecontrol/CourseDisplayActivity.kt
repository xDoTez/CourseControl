package com.example.coursecontrol

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.model.UserDataAdapter
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CourseDisplayActivity : AppCompatActivity() {
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_display_activity)
        Log.d("tu", "tu")

        sessionManager = SessionManager(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.courseDataLiveData.observe(this, Observer { courseDataList ->
            val userDataAdapter = UserDataAdapter(courseDataList)
            recyclerView.adapter = userDataAdapter
        })

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout -> { Logout.logoutUser(this, Intent(this, MainActivity::class.java))
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, CourseDisplayActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    viewModel.makeApiCall(sessionToken)
                } else {
                    // Handle the case when the session token is null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


