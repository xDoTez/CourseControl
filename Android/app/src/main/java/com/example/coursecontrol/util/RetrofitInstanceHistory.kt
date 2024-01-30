package com.example.coursecontrol.util

import com.example.coursecontrol.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceHistory {
    private const val BASE_URL = "http://165.232.76.112:8000/something/course_data_old/"
    val apiServiceHistory: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}