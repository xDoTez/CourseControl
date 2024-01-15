package com.example.coursecontrol.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.ApiResponse
import com.example.coursecontrol.model.Program
import com.example.coursecontrol.model.ProgramName
import com.example.coursecontrol.model.ProgramNew
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseAddNewProgram : ViewModel() {

    suspend fun addNewProgram(context: Context, sessionToken: SessionToken, program: String) {
        if (sessionToken.isValid()) {
            val requestModel = ProgramNew(
                session_token = YourRequestModel(
                    user = sessionToken.user,
                    session_token = sessionToken.session_token,
                    expiration = sessionToken.expiration
                ),
                program = ProgramName(
                    name = program
                )
            )

            try {
                Log.i("CourseAddNewProgram", "Sending request to add a new program. Request: $requestModel")

                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.addNewProgram(requestModel)
                }

                handleApiResponse(context, response)

            } catch (e: Exception) {
                handleApiError(context, e)
            }
        } else {
            Log.e("CourseAddNewProgram", "Invalid SessionToken: $sessionToken")
        }
    }

    private fun handleApiResponse(context: Context, response: ApiResponse) {
        if (response.status == "Success") {
            val message = response.message
            if (message != null) {
                showToast(context, "New program added: $message")
            } else {
                showToast(context, "New program added successfully.")
            }
        } else {
            Log.e("CourseAddNewProgram", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(context: Context, exception: Exception) {
        Log.e("CourseAddNewProgram", "API call failed", exception)
        showToast(context, "Failed to add a new program. Please try again.")
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun SessionToken.isValid(): Boolean {
        return session_token != null && expiration != null
    }
}
