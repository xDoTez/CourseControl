package com.example.coursecontrol

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.coursecontrol.addNewCourse.ProgramDisplayActivity
import com.example.coursecontrol.util.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView


class CoursesNavActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.courses_page)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }

        val myCoursesImageView: ImageView = findViewById(R.id.imgMyCourses)
        val courseHistoryImageView: ImageView = findViewById(R.id.imgMyHistory)
        val addCoursesImageView: ImageView = findViewById(R.id.imgAddCourse)

        myCoursesImageView.setOnClickListener {
            // Start MyCoursesActivity on click
            val intent = Intent(this, CourseDisplayActivity::class.java)
            startActivity(intent)
        }

        courseHistoryImageView.setOnClickListener {
            // Start CourseHistoryActivity on click
            val intent = Intent(this, CourseDisplayHistoryActivity::class.java)
            startActivity(intent)
        }

        addCoursesImageView.setOnClickListener {
            // Start AddCoursesActivity on click
            val intent = Intent(this, ProgramDisplayActivity::class.java)
            startActivity(intent)
        }
    }
}