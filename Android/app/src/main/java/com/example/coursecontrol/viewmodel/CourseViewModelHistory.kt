package com.example.coursecontrol.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.ApiResponse
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstanceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CourseViewModelHistory : ViewModel() {
    private val _courseDataLiveData = MutableLiveData<List<CourseData>>()
    val courseDataLiveData: LiveData<List<CourseData>>
        get() = _courseDataLiveData
    private val _selectedCourseLiveData = MutableLiveData<CourseData>()
    val selectedCourseLiveData: LiveData<CourseData>
        get() = _selectedCourseLiveData

    suspend fun makeApiCall(sessionToken: SessionToken) {
        Log.d("SessionToken", "Expiration: ${sessionToken.expiration}")
        Log.d("SessionToken", "Token: ${sessionToken.session_token}")
        Log.d("SessionToken", "User: ${sessionToken.user}")
        if (sessionToken.session_token != null && sessionToken.expiration != null) {
            val requestModel = YourRequestModel( // Use YourRequestModelHistory for history API service
                user = sessionToken.user,
                session_token = sessionToken.session_token,
                expiration = sessionToken.expiration
            )

            Log.d("CourseViewModelHistory", "Request Body: $requestModel")

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstanceHistory.apiServiceHistory.postUserDataHistory(requestModel)
                }

                handleApiResponse(response)

            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("CourseViewModelHistory", "Invalid SessionToken: $sessionToken")
        }
    }

    private fun handleApiResponse(response: ApiResponse) {
        if (response.status == "Success") {
            val newData = response.data
            _courseDataLiveData.value = _courseDataLiveData.value.orEmpty() + newData

        } else {
            Log.e("CourseViewModelHistory", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("CourseViewModelHistory", "API call failed", exception)
    }

    fun selectCourse(courseData: CourseData) {
        _selectedCourseLiveData.value = courseData
    }
}
