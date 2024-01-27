package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName

data class NewSubcategory(
    @SerializedName("name")val name: String,
    @SerializedName("points")val points: Int,
    @SerializedName("requirements")val requirements: Int
)
