package com.example.coursecontrol

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class RegistrationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)
        val btnRegister: Button = findViewById(R.id.btnRegister)
        etName = findViewById(R.id.etName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnRegister.setOnClickListener {
            Log.d("password", etPassword.toString())
            signup(etUsername.text.toString(), etPassword.text.toString(), etEmail.text.toString())
        }

    }
    private fun signup(username: String, password: String, email: String, ){
        val name = etName.text.toString()
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (password != confirmPassword) {
            Toast.makeText(this@RegistrationActivity, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
            return
        }

        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val registerInfo = UserBody(username, password,email)

        Log.d("Request", "Username: $username, Password: $password, Email: $email")

        retIn.registerUser(registerInfo).enqueue(object :
            Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    handleNetworkError()
                } else {
                    handleRegistrationError()
                }
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val responseBody = response.body()?.string() ?: ""
                if (responseBody.contains("SuccessfulRegistration")) {
                    handleSuccessfulRegistration()
                } else {
                    handleFailedRegistration()
                }
            }
        })
    }
    private fun handleRegistrationError() {
        Toast.makeText(this@RegistrationActivity, "Greška prilikom registracije", Toast.LENGTH_SHORT).show()
    }
    private fun handleNetworkError() {
        Toast.makeText(this@RegistrationActivity, "Nema pristupa internetu", Toast.LENGTH_SHORT).show()
    }
    private fun handleSuccessfulRegistration() {
        Toast.makeText(this@RegistrationActivity, "Registracija uspješna", Toast.LENGTH_SHORT).show()
        //treba napraviti prijelaz na drugi ekran
    }
    private fun handleFailedRegistration() {
        Toast.makeText(this@RegistrationActivity, "Neuspješna registracija", Toast.LENGTH_SHORT).show()
    }
}