package com.example.coursecontrol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompletedCoursesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_courses)

        val completedCoursesList = listOf(
            CompletedCourse("Course 1",  "Grade A"),
            CompletedCourse("Course 2",  "Grade B"),
            CompletedCourse("Course 3",  "Grade B"),
            CompletedCourse("Course 4",  "Grade B"),
        )

        val recyclerView: RecyclerView = findViewById(R.id.completedCoursesRecyclerView)
        val adapter = CompletedCoursesAdapter(completedCoursesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}