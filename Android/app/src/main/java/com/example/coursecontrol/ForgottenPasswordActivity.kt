package com.example.coursecontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.coursecontrol.model.Email
import com.example.coursecontrol.model.ResetCode
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgottenPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnGetResetCode: Button
    private lateinit var etResetCode: EditText
    private lateinit var tvPassReset: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)

        etEmail = findViewById(R.id.etEmail)
        btnGetResetCode = findViewById(R.id.btnGetResetCode)
        etResetCode = findViewById(R.id.etResetCode)

        btnGetResetCode.setOnClickListener {
            val email = etEmail.text.toString()

            val coroutineScope = CoroutineScope(Dispatchers.Default)
            coroutineScope.launch {
                makeAPIcall(email)
            }
        }

        tvPassReset = findViewById(R.id.tvPassReset)
        tvPassReset.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private suspend fun makeAPIcall(email: String) {
        val requestModel = Email(
            email = email
        )

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.sendEmail(requestModel)
            }
            handleApiResponse(response)

        } catch (e: Exception) {
            handleApiError(e)
        }
    }

    private fun handleApiError(e: Exception) {
        Log.e("SendEmail", "API call failed", e)
    }

    private fun handleApiResponse(response: ResetCode) {
        if (response.status == "Success") {
            Looper.prepare()
            val code = response.code
            etResetCode.setText(code)
            Looper.loop()
        } else {
            Log.e("SendEmail", "API call unsuccessful. Status: ${response.status}")
        }
    }
}