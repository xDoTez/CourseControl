package com.example.coursecontrol.model

import com.example.coursecontrol.network.YourRequestModel

data class EditCourseData(
    val session_token: YourRequestModel,
    val course_data: CourseData
)
