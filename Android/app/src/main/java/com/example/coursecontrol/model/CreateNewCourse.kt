package com.example.coursecontrol.model

import com.example.coursecontrol.network.YourRequestModel

data class CreateNewCourse(
    val session_token: YourRequestModel,
    val new_course: NewCourse,
    val program_id: List<Int>
)
