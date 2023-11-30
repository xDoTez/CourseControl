package com.example.coursecontrol

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.coursecontrol.R.*
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    lateinit var apiResponse: LoggedInUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            login(etUsername.text.toString(), etPassword.text.toString())
        }
    }

    private fun login(username: String, password: String){
        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val signInInfo = LoginBody(username, password)
        retIn.signin(signInInfo).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    val responseBody = response.body()?.string() ?: ""

                    if (responseBody.contains("SuccessfullLogin")) {

                        val gson = Gson()
                        val odgovor: LoggedInUser = gson.fromJson(responseBody, LoggedInUser::class.java)
                        println(odgovor.session_token.user)

                        handleSuccessfulLogin()
                    } else {
                        handleFailedLogin()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun handleFailedLogin() {
        Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccessfulLogin() {
        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
    }
}