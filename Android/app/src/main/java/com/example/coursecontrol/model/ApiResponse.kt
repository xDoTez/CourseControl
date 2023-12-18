package com.example.coursecontrol.model

data class ApiResponse(
    val status: String,
    val message: String?,
    val data: List<CourseData>
)
