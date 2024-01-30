package com.example.coursecontrol


import AdminChecker
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.coursecontrol.R.*
import com.example.coursecontrol.model.Admin
import com.example.coursecontrol.network.NewCoursesModel
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var sessionManager: SessionManager
    lateinit var apiResponse: LoggedInUser
    private lateinit var tvForgottenPassword: TextView

    var admin: Boolean = false
    val adminChecker = AdminChecker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        sessionManager = SessionManager(this)

        btnLogin.setOnClickListener {
            login(etUsername.text.toString(), etPassword.text.toString())
        }

        tvForgottenPassword = findViewById(R.id.tvForgottenPassword)
        tvForgottenPassword.setOnClickListener {
            val intent = Intent(this, ForgottenPasswordActivity::class.java)
            startActivity(intent)
        }

        tvRegister.setOnClickListener {
            val registerActivity = Intent(this, RegistrationActivity::class.java)
            startActivity(registerActivity)
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
                        sessionManager.saveSessionToken(odgovor.session_token)
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
        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccessfulLogin() {
        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

        val username = etUsername.text.toString()

        val homeActivityIntent = Intent(this, HomeActivity::class.java)
        homeActivityIntent.putExtra("USERNAME_EXTRA", username)
        startActivity(homeActivityIntent)

        lifecycleScope.launch {
            try {
                val sessionToken = sessionManager.getSessionToken()
                if (sessionToken != null) {
                    adminChecker.checkAdmin(sessionToken)
                    val isAdmin = adminChecker.isAdmin()
                    Log.d("AdminChecker", "API call successful. Status: $isAdmin")
                } else {
                    // Handle the case when sessionToken is null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



}