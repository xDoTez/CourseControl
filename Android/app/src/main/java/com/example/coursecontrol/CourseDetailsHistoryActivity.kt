package com.example.coursecontrol

import CategoryAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.EditCourseData.EditCourseDataActivity

import com.example.coursecontrol.model.ApiResponse
import com.example.coursecontrol.model.CategoryData
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.model.CourseUserData
import com.example.coursecontrol.model.SetInactive
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.RetrofitInstance
import com.example.coursecontrol.util.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CourseDetailsHistoryActivity : AppCompatActivity() {
    private lateinit var btnEditCourseData: ImageButton
    private lateinit var btnSetCourseInactive: Button
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detailed_course_view_history)
        sessionManager = SessionManager(this)
        val courseData: CourseData? = intent.getSerializableExtra("course_data") as? CourseData
        if (courseData != null) {
            val courseNameTextView: TextView = findViewById(R.id.textViewCourseName)
            courseNameTextView.text = courseData.course.name

            recyclerViewCategories = findViewById(R.id.recyclerViewCategories)
            setupRecyclerView(courseData.catagories)
        } else {
            Toast.makeText(this, "Error: Course details not available", Toast.LENGTH_SHORT).show()
            finish()
        }
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }
        btnEditCourseData = findViewById(R.id.btnEditCourseData)
        btnEditCourseData.setOnClickListener {
            val editCourseData = Intent(this, EditCourseDataActivity::class.java)
            editCourseData.putExtra("course_data", courseData)
            startActivity(editCourseData)
            finish()
        }
        btnSetCourseInactive = findViewById(R.id.btnSetCourseInactive)
        btnSetCourseInactive.setOnClickListener {
            showConfirmationDialog { confirmed ->
                if (confirmed) {
                    lifecycleScope.launch {
                        try {
                            val sessionToken = sessionManager.getSessionToken()
                            if (sessionToken != null) {
                                if (courseData != null) {
                                    setCourseInactive(courseData, sessionToken)
                                }
                            } else {
                                // Handle sessionToken being null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }




    }

    private fun setupRecyclerView(categories: List<CategoryData>?) {
        val layoutManager = LinearLayoutManager(this)
        recyclerViewCategories.layoutManager = layoutManager

        val adapter = CategoryAdapter(categories.orEmpty())
        recyclerViewCategories.adapter = adapter
    }

    private suspend fun setCourseInactive(courseData: CourseData, sessionToken: SessionToken) {
        if (sessionToken.session_token != null && sessionToken.expiration != null) {
            val requestModel = SetInactive(
                session_token = YourRequestModel(
                    user = sessionToken.user,
                    session_token = sessionToken.session_token,
                    expiration = sessionToken.expiration
                ),
                user_course = CourseUserData(
                    id = courseData.courseUserData.id,
                    userId = courseData.courseUserData.userId,
                    courseId = courseData.courseUserData.courseId,
                    isActive = courseData.courseUserData.isActive
                )
            )
            Log.d("SetInactive", "Request Body: $requestModel")

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.setInactive(requestModel)
                }
                handleApiResponse(response)
            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("", "$sessionToken")
        }
    }



    private fun showConfirmationDialog(callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you want to designate this course as inactive?")

        builder.setPositiveButton("Yes") { _, _ ->
            callback(true)
        }

        builder.setNegativeButton("No") { _, _ ->
            callback(false)
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun handleApiResponse(response: ApiResponse) {
        Log.e("AddNewCourse", "API call unsuccessful. Status: ${response.status}")
    }

    private fun handleApiError(exception: Exception) {
        Log.e("API Error", "Error occurred: ${exception.message}")
    }



}
