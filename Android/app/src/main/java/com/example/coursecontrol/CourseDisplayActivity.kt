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
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.CourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class CourseDisplayActivity : AppCompatActivity() {
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    val adminChecker = AdminChecker()

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
                adminChecker.checkAdmin(sessionToken)
                if (sessionToken != null) {
                    viewModel.makeApiCall(sessionToken)
                } else {
                }
                val isAdmin = adminChecker.isAdmin()
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
