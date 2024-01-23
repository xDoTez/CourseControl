package com.example.coursecontrol

import UserDataAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModelHistory  // Updated import
import com.google.android.material.bottomnavigation.BottomNavigationView
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
        val activeButton: Button = findViewById(R.id.activeButton)

        activeButton.setOnClickListener {
            val historyIntent = Intent(this, CourseDisplayActivity::class.java)
            startActivity(historyIntent)
        }
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
        val intent = Intent(this, CourseDetailsActivity::class.java)
        intent.putExtra("course_data", courseData)
        startActivity(intent)
    }
}
