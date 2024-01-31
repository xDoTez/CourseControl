package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Category(
    @SerializedName("id")val id: Int,
    @SerializedName("course_id")val courseId: Int,
    @SerializedName("name")val name: String,
    @SerializedName("points")val points: Int,
    @SerializedName("requirements")val requirements: Int
): Serializable
