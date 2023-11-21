package com.example.coursecontrol
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object NetworkService {

    private var instance: Retrofit = Retrofit.Builder()
        .baseUrl("http://165.232.76.112:8000/users/registration")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

}
