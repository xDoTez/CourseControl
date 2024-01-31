package com.example.coursecontrol.model

import com.example.coursecontrol.network.YourRequestModel
import hr.foi.air.core.model.CourseUserData

data class SetInactive(
    val session_token: YourRequestModel,
    val user_course: CourseUserData
)
