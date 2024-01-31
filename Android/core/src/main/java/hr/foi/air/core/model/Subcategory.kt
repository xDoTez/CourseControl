package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Subcategory(
    @SerializedName("id")val id: Int,
    @SerializedName("category_id")val categoryId: Int,
    @SerializedName("name")val name: String,
    @SerializedName("points")val points: Int,
    @SerializedName("requirements")val requirements: Int
): Serializable
