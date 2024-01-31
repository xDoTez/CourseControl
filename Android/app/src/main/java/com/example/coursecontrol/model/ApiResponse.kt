package com.example.coursecontrol.model

import hr.foi.air.core.model.CourseData

data class ApiResponse(
    val status: String,
    val message: String?,
    val data: List<CourseData>
)
