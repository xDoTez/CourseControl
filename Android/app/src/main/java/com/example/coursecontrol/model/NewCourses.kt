package com.example.coursecontrol.model

import hr.foi.air.core.model.Course

data class NewCourses(
    val status: String,
    val programs: List<Course>,
    val message: String
)
