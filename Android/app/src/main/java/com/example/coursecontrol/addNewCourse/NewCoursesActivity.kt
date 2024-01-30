package com.example.coursecontrol.addNewCourse
import AdminChecker
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.AdminCreateNewCourseActivity
import com.example.coursecontrol.CourseDetailsActivity
import com.example.coursecontrol.GenerateReportManagerActivity
import com.example.coursecontrol.Logout
import com.example.coursecontrol.MainActivity
import com.example.coursecontrol.R
import com.example.coursecontrol.model.Course
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.model.Program
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.NewCourseTempSaver
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.NewCourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class NewCoursesActivity : AppCompatActivity() {
    private val viewModel: NewCourseViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    val adminChecker = AdminChecker()
    private lateinit var givenProgram: Program

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_courses)

        sessionManager = SessionManager(this)

        val programData: Program? = intent.getSerializableExtra("program") as? Program
        givenProgram = programData!!
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
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }

        val btnCreateNewCourse: Button = findViewById(R.id.btnCreateNewCurse)

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    if (programData != null) {
                        viewModel.makeApiCall(sessionToken, programData.id)
                    }
                    adminChecker.checkAdmin(sessionToken)
                    val isAdmin = adminChecker.isAdmin()
                    if (!isAdmin) {
                        btnCreateNewCourse.visibility = View.GONE
                    }
                    onCreateNewCourseSelected(programData!!)
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

    private fun onCreateNewCourseSelected(program: Program) {
        val btnCreateNewCourse = findViewById<Button>(R.id.btnCreateNewCurse)
        btnCreateNewCourse.setOnClickListener {
            NewCourseTempSaver.clearCategories()
            NewCourseTempSaver.clearSubCategories()
            val intent = Intent(this, AdminCreateNewCourseActivity::class.java)
            intent.putExtra("program", program)
            startActivity(intent)
        }
    }
}