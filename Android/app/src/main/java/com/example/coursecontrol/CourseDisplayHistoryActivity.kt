package com.example.coursecontrol

import UserDataAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModelHistory  // Updated import
import com.google.android.material.bottomnavigation.BottomNavigationView
import hr.foi.air.core.model.CourseData
import kotlinx.coroutines.launch

class CourseDisplayHistoryActivity : AppCompatActivity() {
    private val viewModel: CourseViewModelHistory by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_display_history_activity)
        Log.d("tu", "tu")

        sessionManager = SessionManager(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        viewModel.courseDataLiveData.observe(this, Observer { courseDataList ->
            val userDataAdapter = UserDataAdapter(courseDataList) { selectedCourseData ->
                onCourseItemSelected(selectedCourseData)
            }
            recyclerView.adapter = userDataAdapter
        })
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }


        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    viewModel.makeApiCall(sessionToken)
                } else {
                    // Handle the case when sessionToken is null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onCourseItemSelected(courseData: CourseData) {
        val intent = Intent(this, CourseDetailsHistoryActivity::class.java)
        intent.putExtra("course_data", courseData)
        startActivity(intent)
    }
}
