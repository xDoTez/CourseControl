package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CourseUserData(
    @SerializedName("id")val id: Int,
    @SerializedName("user_id")val userId: Int,
    @SerializedName("course_id")val courseId: Int,
    @SerializedName("is_active")val isActive: Boolean
): Serializable
