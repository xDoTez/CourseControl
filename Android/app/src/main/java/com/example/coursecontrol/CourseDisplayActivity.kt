package com.example.coursecontrol

import AdminChecker
import UserDataAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.addNewCourse.ProgramDisplayActivity
import com.example.coursecontrol.adminPrivileges.UserDisplayActivity
import com.example.coursecontrol.databinding.ActivityMainBinding
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CourseDisplayActivity : AppCompatActivity() {
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    val adminChecker = AdminChecker()
    private lateinit var btnAddNewCourse: Button
    private lateinit var btnUsers : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_display_activity)
        sessionManager = SessionManager(this)


        val spinner = findViewById<Spinner>(R.id.spinner)
        val sortOptions = listOf("Name ascending", "Name descending", "Semester ascending", "Semester descending")
        val adapter = ArrayAdapter<String>(this, R.layout.sort_options, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedItem = sortOptions[position]

                if (selectedItem == "Name ascending") {
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
                } else if (selectedItem == "Name descending") {
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
                } else if (selectedItem == "Semester ascending") {
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
                } else if (selectedItem == "Semester descending") {
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

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

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
    }

    private fun onCourseItemSelected(courseData: CourseData) {
        val intent = Intent(this, CourseDetailsActivity::class.java)
        intent.putExtra("course_data", courseData)
        startActivity(intent)
    }
}
