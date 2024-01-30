package com.example.coursecontrol.model

data class ApiResponseAdminPrivileges(
    val status: String,
    val message: String?,
    val users: List<User>
)

