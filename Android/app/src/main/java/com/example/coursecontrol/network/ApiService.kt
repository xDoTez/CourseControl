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

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=NameAlphabeticAsc")
    suspend fun sortNameAlphabeticAsc(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=NameAlphabeticDesc")
    suspend fun sortNameAlphabeticDesc(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=SemesterAsc")
    suspend fun sortSemesterAsc(@Body request: YourRequestModel): ApiResponse

    @POST("http://165.232.76.112:8000/something/course_data?sorting_option=SemesterDesc")
    suspend fun sortSemesterDesc(@Body request: YourRequestModel): ApiResponse
}