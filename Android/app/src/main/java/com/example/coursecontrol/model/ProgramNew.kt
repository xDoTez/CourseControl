package com.example.coursecontrol.model
import com.example.coursecontrol.network.YourRequestModel

data class ProgramNew(
    val session_token: YourRequestModel,
    val name: String
)
