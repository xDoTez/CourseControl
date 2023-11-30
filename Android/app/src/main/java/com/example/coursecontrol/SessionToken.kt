package com.example.coursecontrol

data class SessionToken(
    val expiration: String,
    val session_token: String,
    val user: Int
)