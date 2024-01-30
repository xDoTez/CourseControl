package com.example.coursecontrol.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.Course
import com.example.coursecontrol.model.NewCourses
import com.example.coursecontrol.model.ProgramNew
import com.example.coursecontrol.network.NewCoursesModel
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewCourseViewModel: ViewModel() {
    private val _courseDataLiveData = MutableLiveData<List<Course>>()
    val courseDataLiveData: LiveData<List<Course>>
        get() = _courseDataLiveData
    private val _selectedCourseLiveData = MutableLiveData<Course>()
    val selectedCourseLiveData: LiveData<Course>
        get() = _selectedCourseLiveData
    suspend fun makeApiCall(sessionToken: SessionToken, programId: Int) {
        Log.d("SessionToken", "Expiration: ${sessionToken.expiration}")
        Log.d("SessionToken", "Token: ${sessionToken.session_token}")
        Log.d("SessionToken", "User: ${sessionToken.user}")
        Log.d("Id", "Id: ${programId}")
        if (sessionToken.session_token != null && sessionToken.expiration != null) {
            val requestModel = NewCoursesModel(
                session_token = YourRequestModel(
                    user = sessionToken.user,
                    session_token = sessionToken.session_token,
                    expiration = sessionToken.expiration),
                program_id = programId
            )

            Log.d("NewCourseViewModel", "Request Body: $requestModel")

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.getNewCourses(requestModel)
                }

                handleApiResponse(response)

            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("CourseViewModel", "Invalid SessionToken: $sessionToken")
        }
    }

    private fun handleApiResponse(response: NewCourses) {
        if (response.status == "Success") {
            val newData = response.programs
            _courseDataLiveData.value = _courseDataLiveData.value.orEmpty() + newData
            Log.d("CourseViewModel", "${response.programs}")

        } else {
            Log.e("CourseViewModel", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("CourseViewModel", "API call failed", exception)
    }
/*
    fun selectCourse(courseData: CourseData) {
        _selectedCourseLiveData.value = courseData
    }*/

    fun clearCourseData() {
        _courseDataLiveData.value = emptyList()
    }
}