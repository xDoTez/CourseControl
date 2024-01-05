package com.example.coursecontrol.network

import com.example.coursecontrol.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

data class YourRequestModelHistory(
    val user: Int,
    val session_token: String,
    val expiration: String
)

interface ApiServiceHistory {
    @POST("http://165.232.76.112:8000/something/course_data_old")
    suspend fun postUserDataHistory(@Body request: YourRequestModelHistory): ApiResponse
}