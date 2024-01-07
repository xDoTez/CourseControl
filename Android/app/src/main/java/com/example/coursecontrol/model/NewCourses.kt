package com.example.coursecontrol.model

data class NewCourses(
    val status: String,
    val programs: List<Course>,
    val message: String
)
