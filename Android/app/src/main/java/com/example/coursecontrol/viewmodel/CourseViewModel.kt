package com.example.coursecontrol.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.ApiResponse
import com.example.coursecontrol.model.CourseData
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CourseViewModel : ViewModel() {
    private val _courseDataLiveData = MutableLiveData<List<CourseData>>()
    val courseDataLiveData: LiveData<List<CourseData>>
        get() = _courseDataLiveData

    suspend fun makeApiCall(sessionToken: SessionToken) {
        Log.d("SessionToken", "Expiration: ${sessionToken.expiration}")
        Log.d("SessionToken", "Token: ${sessionToken.session_token}")
        Log.d("SessionToken", "User: ${sessionToken.user}")
        if (sessionToken.session_token != null && sessionToken.expiration != null) {
            val requestModel = YourRequestModel(
                user = sessionToken.user,
                session_token = sessionToken.session_token,
                expiration = sessionToken.expiration
            )

            Log.d("CourseViewModel", "Request Body: $requestModel")

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.postUserData(requestModel)
                }

                handleApiResponse(response)

            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("CourseViewModel", "Invalid SessionToken: $sessionToken")
        }
    }


    private fun handleApiResponse(response: ApiResponse) {
        if (response.status == "Success") {
            val newData = response.data
            _courseDataLiveData.value = _courseDataLiveData.value.orEmpty() + newData

        } else {
            Log.e("CourseViewModel", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("CourseViewModel", "API call failed", exception)
    }
}



