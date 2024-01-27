package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName

data class NewCategory(
    @SerializedName("name")val name: String,
    @SerializedName("points")val points: Int,
    @SerializedName("requirements")val requirements: Int,
    @SerializedName("subcategories")val subcategories: List<NewSubcategory>?
)
