package com.example.coursecontrol

import AdminChecker
import UserDataAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.addNewCourse.ProgramDisplayActivity
import com.example.coursecontrol.adminPrivileges.UserDisplayActivity
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CourseDisplayActivity : AppCompatActivity() {
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    val adminChecker = AdminChecker()
    private lateinit var btnAlphAsc: Button
    private lateinit var btnAlphDesc: Button
    private lateinit var btnSemAsc: Button
    private lateinit var btnSemDesc: Button
    private lateinit var btnAddNewCourse: Button
    private lateinit var btnUsers : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_display_activity)
        sessionManager = SessionManager(this)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val historyButton: Button = findViewById(R.id.historyButton)
        historyButton.setOnClickListener {
            val historyIntent = Intent(this, CourseDisplayHistoryActivity::class.java)
            startActivity(historyIntent)
        }

        val usersButton: Button = findViewById(R.id.btnUsers)
        usersButton.setOnClickListener {
            val usersIntent = Intent(this, UserDisplayActivity::class.java)
            startActivity(usersIntent)
        }

        btnAddNewCourse = findViewById(R.id.btnAddNewCourse)
        btnAddNewCourse.setOnClickListener {
            val addNewCourseIntent = Intent(this, ProgramDisplayActivity::class.java)
            startActivity(addNewCourseIntent)
        }

        viewModel.courseDataLiveData.observe(this, Observer { courseDataList ->
            val userDataAdapter = UserDataAdapter(courseDataList) { selectedCourseData ->
                onCourseItemSelected(selectedCourseData)
            }
            recyclerView.adapter = userDataAdapter
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
                adminChecker.checkAdmin(sessionToken)
                if (sessionToken != null) {
                    viewModel.makeApiCall(sessionToken)
                } else {
                }
                val usersButton: Button = findViewById(R.id.btnUsers)
                val isAdmin = adminChecker.isAdmin()
                if (isAdmin) {
                    usersButton.visibility = View.VISIBLE
                }
                else{
                    usersButton.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btnAlphAsc = findViewById(R.id.alphabeticAsc)
        btnAlphAsc.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val sessionToken = sessionManager.getSessionToken()
                    if (sessionToken != null) {
                        viewModel.clearCourseData()
                        viewModel.makeApiCallForSortingAlphAsc(sessionToken)
                    } else {
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnAlphDesc = findViewById(R.id.alphabeticDesc)
        btnAlphDesc.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val sessionToken = sessionManager.getSessionToken()
                    if (sessionToken != null) {
                        viewModel.clearCourseData()
                        viewModel.makeApiCallForSortingAlphDesc(sessionToken)
                    } else {
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnSemAsc = findViewById(R.id.semesterAsc)
        btnSemAsc.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val sessionToken = sessionManager.getSessionToken()
                    if (sessionToken != null) {
                        viewModel.clearCourseData()
                        viewModel.makeApiCallForSortingSemAsc(sessionToken)
                    } else {
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnSemDesc = findViewById(R.id.semesterDesc)
        btnSemDesc.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val sessionToken = sessionManager.getSessionToken()
                    if (sessionToken != null) {
                        viewModel.clearCourseData()
                        viewModel.makeApiCallForSortingSemDesc(sessionToken)
                    } else {
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onCourseItemSelected(courseData: CourseData) {
        val intent = Intent(this, CourseDetailsActivity::class.java)
        intent.putExtra("course_data", courseData)
        startActivity(intent)
    }
}
