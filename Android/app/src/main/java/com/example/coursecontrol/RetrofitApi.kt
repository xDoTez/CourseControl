package com.example.coursecontrol

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitApi {
    @POST("login")
    fun loginUser(@Body loginBody: LoginBody): Call<ResponseBody>
}