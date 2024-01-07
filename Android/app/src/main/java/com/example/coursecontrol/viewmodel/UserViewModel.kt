package com.example.coursecontrol.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.ApiResponseAdminPrivileges
import com.example.coursecontrol.model.User
import com.example.coursecontrol.network.UserModel
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class UserViewModel : ViewModel(){
    private val _userDataLiveData = MutableLiveData<List<User>>()
    val userDataLiveData: MutableLiveData<List<User>>
        get() = _userDataLiveData
    private val _selectedUserLiveData = MutableLiveData<User>()
    val selectedUserLiveData: MutableLiveData<User>
        get() = _selectedUserLiveData

    suspend fun makeApiCall(sessionToken: SessionToken) {
        val requestModel = YourRequestModel(
            user = sessionToken.user,
            session_token = sessionToken.session_token,
            expiration = sessionToken.expiration
        )
        try {
            Log.d("UserViewModel", "Making API call with request: $requestModel")
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.getAllNonAdmins(requestModel)
            }
            Log.d("UserViewModel", "API call response: $response")
            handleApiResponse(response)
        } catch (e: HttpException) {
            Log.e("UserViewModel", "API call failed with HTTP exception", e)
            handleApiError(e)
        } catch (e: Exception) {
            Log.e("UserViewModel", "API call failed with unexpected exception", e)
            handleApiError(e)
        }
    }
    private fun handleApiResponse(response: ApiResponseAdminPrivileges) {
        if (response.status == "Success") {
            val newData = response.users
            _userDataLiveData.value = newData
        } else if (response.status == "DatabaseError") {
            Log.d("DatabaseError", "DatabaseError")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("UserViewModel", "API call failed", exception)
    }

    fun selectUser(user: User) {
        _selectedUserLiveData.value = user
    }
}
