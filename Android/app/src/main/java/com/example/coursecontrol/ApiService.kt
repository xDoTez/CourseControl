package com.example.coursecontrol
import com.example.coursecontrol.RegistrationBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("http://165.232.76.112:8000/users/registration")
    fun registerUser(@Body request: RegistrationBody): Call<ResponseBody>
}