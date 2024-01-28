package com.example.coursecontrol.model

import com.example.coursecontrol.network.YourRequestModel

data class SetInactive(
    val session_token: YourRequestModel,
    val user_course: CourseUserData
)
