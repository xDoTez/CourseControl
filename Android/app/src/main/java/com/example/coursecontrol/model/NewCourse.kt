package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName

data class NewCourse(
    @SerializedName("name")val name: String,
    @SerializedName("semester")val semester: Int,
    @SerializedName("ects")val ects: Int,
    @SerializedName("categories")val catagories: List<NewCategory>
)
