package com.example.coursecontrol.addNewCourse

import UserDataAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.CourseDetailsActivity
import com.example.coursecontrol.GenerateReportManagerActivity
import com.example.coursecontrol.Logout
import com.example.coursecontrol.MainActivity
import com.example.coursecontrol.R
import com.example.coursecontrol.model.Course
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.model.Program
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.NewCourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class NewCoursesActivity : AppCompatActivity() {
    private val viewModel: NewCourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_courses)

        sessionManager = SessionManager(this)

        val programData: Program? = intent.getSerializableExtra("program") as? Program
        Log.d("info", "$programData")

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.courseDataLiveData.observe(this, Observer { courseDataList ->
            val newCourseAdapter = NewCourseAdapter(courseDataList) { selectedCourseData ->
                onCourseItemSelected(selectedCourseData)
            }
            recyclerView.adapter = newCourseAdapter
        })

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    Logout.logoutUser(this, Intent(this, MainActivity::class.java))
                    true
                }
                R.id.report -> {
                    val intent = Intent(this, GenerateReportManagerActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    true
                }
                else -> false
            }
        }


        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    if (programData != null) {
                        viewModel.makeApiCall(sessionToken, programData.id)
                    }
                } else {
                    // Handle the case when sessionToken is null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onCourseItemSelected(courseData: Course) {
        val sessionToken = sessionManager.getSessionToken()
        if (sessionToken != null) {
            val newCourseAlertDialog = NewCourseAlertDialog(
                this,
                courseData,
                sessionToken
            )
            newCourseAlertDialog.show()
        }
    }
}