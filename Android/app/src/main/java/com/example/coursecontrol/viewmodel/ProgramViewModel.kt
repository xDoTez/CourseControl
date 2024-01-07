package com.example.coursecontrol.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.ApiResponseAddNewCourse
import com.example.coursecontrol.model.Program
import com.example.coursecontrol.network.YourRequestModelHistory
import com.example.coursecontrol.util.RetrofitInstance
import com.example.coursecontrol.util.RetrofitInstanceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProgramViewModel : ViewModel() {
    private val _programDataLiveData = MutableLiveData<List<Program>>()
    val programDataLiveData: MutableLiveData<List<Program>>
        get() = _programDataLiveData
    private val _selectedProgramLiveData = MutableLiveData<Program>()
    val selectedProgramLiveData: MutableLiveData<Program>
        get() = _selectedProgramLiveData

    suspend fun makeApiCall() {

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.getAllPrograms()
            }

            handleApiResponse(response)
            Log.d("", "$response")

        } catch (e: Exception) {
            handleApiError(e)
        }
    }

    private fun handleApiResponse(response: ApiResponseAddNewCourse) {
        if (response.status == "Sucess") {
            val newData = response.programs
            _programDataLiveData.value = _programDataLiveData.value.orEmpty() + newData

        } else if (response.status == "DatabaseError"){
            Log.d("DatabaseError", "DatabaseError")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("ProgramViewModel", "API call failed", exception)
    }

    fun selectProgram(program: Program) {
        _selectedProgramLiveData.value = program
    }
}