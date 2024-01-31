package com.example.coursecontrol.model

import com.example.coursecontrol.network.YourRequestModel
import hr.foi.air.core.model.CourseData

data class EditCourseData(
    val session_token: YourRequestModel,
    val course_data: CourseData
)
