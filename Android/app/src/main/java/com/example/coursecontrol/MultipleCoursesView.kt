package com.example.coursecontrol

import android.os.Bundle
import android.widget.ListView
import androidx.activity.ComponentActivity

class MultipleCoursesView : ComponentActivity() {

    private lateinit var lvCourses: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_courses_view)

        lvCourses = findViewById(R.id.lvCourses)
    }
}
