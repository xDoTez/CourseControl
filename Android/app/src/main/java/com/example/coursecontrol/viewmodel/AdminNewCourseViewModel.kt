package com.example.coursecontrol.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.AddNewCourse
import com.example.coursecontrol.model.CreateNewCourse
import com.example.coursecontrol.model.NewCourse
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminNewCourseViewModel: ViewModel() {

    suspend fun createNewCourse(context: Context, sessionToken: SessionToken, course: NewCourse, programId: Int){
        var program = listOf(programId)
        if(sessionToken.isValid()){
            val requestModel = CreateNewCourse(
                session_token = YourRequestModel(
                    user = sessionToken.user,
                    session_token = sessionToken.session_token,
                    expiration = sessionToken.expiration
                ),
                new_course = course,
                program_id = program
            )

            try {
                Log.i("CreateNewCourse", "Sending request to add a new course. Request: $requestModel")

                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.createNewCourse(requestModel)
                }

                handleApiResponse(context, response)

            } catch (e: Exception) {
                handleApiError(context, e)
            }
        }
    }

    private fun handleApiResponse(context: Context, response: AddNewCourse) {
        if (response.status == "Success") {
            val message = response.message
            if (message != null) {
                showToast(context, "New course added: $message")
            } else {
                showToast(context, "New course added successfully.")
            }
        } else {
            Log.e("CreateNewCourse", "API call unsuccessful. Status: ${response.status}" + " " + response.message)
        }
    }

    private fun handleApiError(context: Context, exception: Exception) {
        Log.e("CreateNewCourse", "API call failed", exception)
        showToast(context, "Failed to add a new course. Please try again.")
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun SessionToken.isValid(): Boolean {
        return session_token != null && expiration != null
    }

}