package com.example.coursecontrol

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
    @Headers("Content-Type:application/json")
    @POST("users/login")
    fun signin(@Body info: LoginBody): Call<ResponseBody>

    @Headers("Content-Type:application/json")
    @POST("users/registration")
    fun registerUser(
        @Body info: UserBody
    ): retrofit2.Call<ResponseBody>
}
class RetrofitInstance {
    companion object {
        val BASE_URL: String = "http://165.232.76.112:8000/"

        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client: OkHttpClient = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
        }.build()
        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}