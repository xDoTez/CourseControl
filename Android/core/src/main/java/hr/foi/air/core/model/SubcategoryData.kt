package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubcategoryData(
    @SerializedName("subcategory")val subcategory: Subcategory,
    @SerializedName("subcategory_user_data")val subcategoryUserData: SubcategoryUserData
): Serializable
