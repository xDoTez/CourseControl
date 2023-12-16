package com.example.coursecontrol.model

data class CourseData(
    val course: Course,
    val courseUserData: CourseUserData,
    val categories: List<CategoryData>
)
