package com.example.coursecontrol.util

import com.example.coursecontrol.network.ApiServiceHistory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceHistory {
    private const val BASE_URL = "http://165.232.76.112:8000/something/course_data_old/"
    val apiServiceHistory: ApiServiceHistory by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceHistory::class.java)
    }
}