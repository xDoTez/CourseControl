package com.example.coursecontrol.network

import com.example.coursecontrol.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

data class YourRequestModel(
    val user: Int,
    val session_token: String,
    val expiration: String
)

interface ApiService {
    @POST("http://165.232.76.112:8000/something/course_data")
    suspend fun postUserData(@Body request: YourRequestModel): ApiResponse
}