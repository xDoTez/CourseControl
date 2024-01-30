package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CategoryData(
    @SerializedName("category") val category: Category,
    @SerializedName("category_user_data") val categoryUserData: CategoryUserData,
    @SerializedName("subcategories") val subcategories: List<SubcategoryData>?
): Serializable
