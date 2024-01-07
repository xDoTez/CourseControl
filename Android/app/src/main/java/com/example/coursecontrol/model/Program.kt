package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Program(
    @SerializedName("id")val id: Int,
    @SerializedName("name")val name: String
): Serializable
