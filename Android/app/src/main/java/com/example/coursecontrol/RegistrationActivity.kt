package com.example.coursecontrol

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class RegistrationActivity : MainActivity(){
    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword : EditText

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/") //
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        val btnRegister: Button = findViewById(R.id.btnRegister)
        etName = findViewById(R.id.etName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)


        btnRegister.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View) {
                Log.d("RegistrationActivity", "Button clicked")
                registerUser()
            }
        })
    }
    private fun registerUser() {
        val name = etName.text.toString()
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (password != confirmPassword) {
            Toast.makeText(this@RegistrationActivity, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
            return
        }
        val registrationBody = RegistrationBody(username, email, password)

        val call = apiService.registerUser(registrationBody)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    handleSuccessfulRegistration()
                } else {
                    handleFailedRegistration()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Log.e("RegistrationActivity", "Network error: ${t.message}")
                    handleNetworkError()
                } else {
                    handleRegistrationError()
                }
            }
        })
    }
    private fun handleSuccessfulRegistration() {
        Toast.makeText(this@RegistrationActivity, "Registracija uspješna", Toast.LENGTH_SHORT).show()
        //treba napraviti prijelaz na drugi ekran
    }
    private fun handleFailedRegistration() {
        Toast.makeText(this@RegistrationActivity, "Neuspješna registracija", Toast.LENGTH_SHORT).show()
    }
    private fun handleRegistrationError() {
        Toast.makeText(this@RegistrationActivity, "Greška prilikom registracije", Toast.LENGTH_SHORT).show()
    }
    private fun handleNetworkError() {
        Toast.makeText(this@RegistrationActivity, "Nema pristupa internetu", Toast.LENGTH_SHORT).show()
    }
}
