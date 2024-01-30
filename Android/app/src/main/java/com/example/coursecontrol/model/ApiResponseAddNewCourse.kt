package com.example.coursecontrol.model

data class ApiResponseAddNewCourse(
    val status: String,
    val programs: List<Program>,
    val message: String?
)
