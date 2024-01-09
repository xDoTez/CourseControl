package com.example.coursecontrol.viewmodel

import android.annotation.SuppressLint
import android.icu.text.DisplayContext
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.ApiResponseAdminPrivileges
import com.example.coursecontrol.model.User
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class UserViewModel : ViewModel(){
    private val _userDataLiveData = MutableLiveData<List<User>>()
    val userDataLiveData: MutableLiveData<List<User>>
        get() = _userDataLiveData
    private val _selectedUserLiveData = MutableLiveData<User>()
    val selectedUserLiveData: MutableLiveData<User>
        get() = _selectedUserLiveData

    class MyDateTypeAdapter : JsonDeserializer<Date>, JsonSerializer<Date> {
        private val formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
            val dateString = json?.asString
            return try {
                formatter.parse(dateString)
            } catch (e: ParseException) {
                throw JsonParseException(e)
            }
        }

        override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            synchronized(formatter) {
                val dateFormatAsString: String = formatter.format(src)
                return JsonPrimitive(dateFormatAsString)
            }
        }
    }

    val gson = GsonBuilder().apply {
        registerTypeAdapter(Date::class.java, MyDateTypeAdapter())
    }.create()
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
            Log.e("UserViewModel", "Response body: ${e.response()?.errorBody()?.string()}")
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

        if (exception is HttpException) {
            val errorBody = exception.response()?.errorBody()?.string()
            Log.e("UserViewModel", "Response body: $errorBody")
        }
    }

    fun selectUser(user: User) {
        _selectedUserLiveData.value = user
    }
}
