package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubcategoryUserData(
    @SerializedName("user_course_category_id")val userCourseCategoryId: Int,
    @SerializedName("subcategory_id")val subcategoryId: Int,
    @SerializedName("points")val points: Int
): Serializable
