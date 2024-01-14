package com.example.coursecontrol.adminPrivileges

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.AddNewAdmin
import com.example.coursecontrol.network.NewAdminModel
import com.example.coursecontrol.model.User
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewAdminAlertDialog(context: Context, user: User, sessionToken: SessionToken) {
    private val alertDialog: AlertDialog = AlertDialog.Builder(context).create()

    init {
        alertDialog.setTitle("Confirm selection")
        alertDialog.setMessage("Are you sure you want to enroll " + user.username)

        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "YES"
        ) {_, _ ->

            val coroutineScope = CoroutineScope(Dispatchers.Default)
            coroutineScope.launch {
                makeApiCall(sessionToken, user.id)
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

    suspend fun makeApiCall(sessionToken: SessionToken, userID: Int) {
        if (sessionToken.session_token != null && sessionToken.expiration != null) {
            val requestModel = NewAdminModel(
                session_token = YourRequestModel(
                    user = sessionToken.user,
                    session_token = sessionToken.session_token,
                    expiration = sessionToken.expiration),
                user_id = userID
            )

            Log.d("AddNewAdminViewModel", "Request Body: $requestModel")

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.addNewAdmin(requestModel)
                }

                handleApiResponse(response)

            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("AddNewAdminViewModel", "Invalid SessionToken: $sessionToken")
        }
    }

    private fun handleApiResponse(response: AddNewAdmin) {
        if (response.status == "Success") {
            val newData = response.message
            Log.d("AddNewAdmin", "Successfully added new admin!")

        } else {
            Log.e("AddNewAdmin", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun handleApiError(exception: Exception) {
        Log.e("AdminViewModel", "API call failed", exception)
    }
}