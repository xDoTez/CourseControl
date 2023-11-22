package com.example.coursecontrol
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("registration")
    fun registerUser(@Body request: RegistrationBody): Call<ResponseBody>
}