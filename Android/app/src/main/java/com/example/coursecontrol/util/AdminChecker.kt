import android.util.Log
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.Admin
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdminChecker {

    private var admin: Boolean = false
    suspend fun checkAdmin(sessionToken: SessionToken?) {
        if (sessionToken?.session_token != null && sessionToken.expiration != null) {
            val requestModel = YourRequestModel(
                user = sessionToken.user,
                session_token = sessionToken.session_token,
                expiration = sessionToken.expiration
            )

            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.apiService.checkIfAdmin(requestModel)
                }

                handleApiResponse(response)

            } catch (e: Exception) {
                handleApiError(e)
            }
        } else {
            Log.e("AdminChecker", "Invalid SessionToken: $sessionToken")
        }
    }


    private fun handleApiError(e: Exception) {
        Log.e("AdminChecker", "API call failed", e)
    }

    private fun handleApiResponse(response: Admin) {
        if (response.status == "Success") {
            admin = response.is_admin
            Log.d("IsAdmin", "$admin")
        } else {
            Log.e("AdminChecker", "API call unsuccessful. Status: ${response.status}")
        }
    }

    fun isAdmin(): Boolean {
        return admin
    }
}
