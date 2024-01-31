package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CategoryUserData(
    @SerializedName("id")val id: Int,
    @SerializedName("user_course_id")val userCourseId: Int,
    @SerializedName("category_id")val categoryId: Int,
    @SerializedName("points")val points: Int
): Serializable
