package com.example.coursecontrol

data class LoggedInUser(
    val error_message: Any,
    val session_token: SessionToken,
    val status: String
)