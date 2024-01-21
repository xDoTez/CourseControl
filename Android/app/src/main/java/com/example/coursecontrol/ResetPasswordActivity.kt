package com.example.coursecontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.coursecontrol.model.AddNewCourse
import com.example.coursecontrol.model.ResetPassword
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var enterResetCode: EditText
    private lateinit var enterPassword: EditText
    private lateinit var enterPassword2: EditText
    private lateinit var btnResetPassword: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        enterResetCode = findViewById(R.id.enterResetCode)
        enterPassword = findViewById(R.id.enterPassword)
        enterPassword2 = findViewById(R.id.enterPassword2)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val resetCode = enterResetCode.text.toString()
            val pass = enterPassword.text.toString()
            val pass2 = enterPassword2.text.toString()

            if (pass == pass2) {
                val coroutineScope = CoroutineScope(Dispatchers.Default)
                coroutineScope.launch {
                    makeAPIcall(resetCode, pass)
                }
                Toast.makeText(this@ResetPasswordActivity, "Successfully changed password!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@ResetPasswordActivity, "Passwords doesn't match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun makeAPIcall(code: String, pass: String) {
        val requestModel = ResetPassword(
            code = code,
            password = pass
        )

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.resetPassword(requestModel)
            }
            handleApiResponse(response)

        } catch (e: Exception) {
            handleApiError(e)
        }
    }

    private fun handleApiResponse(response: AddNewCourse) {
        if (response.status == "Success") {
            Log.d("PasswordReset", "Successfully changed password!")
        } else {
            Log.e("PasswordReset", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(e: Exception) {
        Log.e("PasswordReset", "API call failed", e)
    }
}