package com.example.coursecontrol

import AdminChecker
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.addNewCourse.CategoriesAdapter
import com.example.coursecontrol.addNewCourse.NewCoursesActivity
import com.example.coursecontrol.databinding.ActivityAdminCreateNewCourseBinding
import com.example.coursecontrol.model.Program
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.NewCourseTempSaver
import com.example.coursecontrol.util.SessionManager
import com.example.coursecontrol.viewmodel.AdminNewCourseViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminCreateNewCourseActivity : AppCompatActivity() {
    private val adminNewCourseViewModel: AdminNewCourseViewModel by viewModels()
    private lateinit var binding: ActivityAdminCreateNewCourseBinding
    private lateinit var sessionManager: SessionManager
    val adminChecker = AdminChecker()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCreateNewCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }

        val program: Program? = intent.getSerializableExtra("program") as? Program
        Log.d("info", "$program")

        sessionManager = SessionManager(this)

        inflateLayout()
        onCancelCreatingNewCourseSelected()
        onCreateNewCategorySelected()
        onCreateCourseSelected(program)

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                adminChecker.checkAdmin(sessionToken)
                if (sessionToken != null) {

                } else {
                }
                val isAdmin = adminChecker.isAdmin()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun inflateLayout(){
        val categoriesRecyclerView: RecyclerView = findViewById(R.id.categoriesRecyclerView)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)

        val categories = NewCourseTempSaver.getCategories()
        val categoriesAdapter = CategoriesAdapter(categories)
        categoriesRecyclerView.adapter = categoriesAdapter
    }

    private fun onCreateCourseSelected(program: Program?) {
        val btnCreateCourse = findViewById<Button>(R.id.btnCreateCourse)
        btnCreateCourse.setOnClickListener {
            val name = findViewById<EditText>(R.id.newCourseName).text.toString()
            val ects = findViewById<EditText>(R.id.newCourseEcts).text.toString()
            val semester = findViewById<EditText>(R.id.newCourseSemester).text.toString()

            if(name.isNotBlank() && ects.isNotBlank() && semester.isNotBlank()){
                if(NewCourseTempSaver.getCategories().isNotEmpty()){
                    lifecycleScope.launch(Dispatchers.Main) {
                        val newCourse = NewCourseTempSaver.createCourse(name, semester.toInt(), ects.toInt())
                        adminNewCourseViewModel.createNewCourse(
                            context = this@AdminCreateNewCourseActivity,
                            sessionToken = sessionManager.getSessionToken()!!,
                            course = newCourse,
                            programId = program!!.id
                        )
                    }
                    val intent = Intent(this, NewCoursesActivity::class.java)
                    intent.putExtra("program", program)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, "Course should have atleast one category!", Toast.LENGTH_SHORT).show()
                }
            }else {
                Toast.makeText(this, "Insert data into all fields before adding a Course", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onCreateNewCategorySelected() {
        val btnCreateNewCategory = findViewById<Button>(R.id.btnCreateNewCategory)
        btnCreateNewCategory.setOnClickListener {
            val intent = Intent(this, AdminCreateNewCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onCancelCreatingNewCourseSelected() {
        val btnCancel = findViewById<Button>(R.id.btnCancelCreatingNewCourse)
        btnCancel.setOnClickListener {
            NewCourseTempSaver.clearCategories()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        inflateLayout()
    }
}