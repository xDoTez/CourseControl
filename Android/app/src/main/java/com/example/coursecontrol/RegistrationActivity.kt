package com.example.coursecontrol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class RegistrationActivity : MainActivity(){
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurnname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            registerUser()
        }
    }
    private fun registerUser() {
        val name = etName.text.toString()
        val surname = etSurname.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        // API poziv...

        val message = "Name: $name\nSurname: $surname\nEmail: $email\nPassword: $password"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}