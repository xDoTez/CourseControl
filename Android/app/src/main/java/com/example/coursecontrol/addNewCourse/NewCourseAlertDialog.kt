package com.example.coursecontrol.addNewCourse

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.AddNewCourse
import com.example.coursecontrol.network.AddNewCourseModel
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import hr.foi.air.core.model.Course
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewCourseAlertDialog (context: Context, course: Course, sessionToken: SessionToken) {
    private val alertDialog: AlertDialog = AlertDialog.Builder(context).create()

    init {
        alertDialog.setTitle("Confirm selection")
        alertDialog.setMessage("Are you sure you want to enroll " + course.name)

        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "YES"
        ) {_, _ ->

            val coroutineScope = CoroutineScope(Dispatchers.Default)
            coroutineScope.launch {
                makeApiCall(sessionToken, course.id)
            }

            alertDialog.dismiss()
        }

        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "NO"
        ) {_, _ ->
            alertDialog.dismiss()
        }
    }

    fun show() {
        alertDialog.show()
    }

    suspend fun makeApiCall(sessionToken: SessionToken, courseId: Int) {
        if (sessionToken.session_token != null && sessionToken.expiration != null) {
            val requestModel = AddNewCourseModel(
                session_token = YourRequestModel(
                    user = sessionToken.user,
                    session_token = sessionToken.session_token,
                    expiration = sessionToken.expiration),
                course_id = courseId
            )

            Log.d("AddNewCourseViewModel", "Request Body: $requestModel")

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.addNewCourse(requestModel)
                }

                handleApiResponse(response)

            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("AddNewCourseViewModel", "Invalid SessionToken: $sessionToken")
        }
    }

    private fun handleApiResponse(response: AddNewCourse) {
        if (response.status == "Success") {
            val newData = response.message
            Log.d("AddNewCourse", "Successfully added new course!")

        } else {
            Log.e("AddNewCourse", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("CourseViewModel", "API call failed", exception)
    }
}