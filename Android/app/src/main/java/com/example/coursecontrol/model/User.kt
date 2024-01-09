package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName
import com.google.gson.internal.bind.DateTypeAdapter
import java.io.Serializable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
data class User (
    @SerializedName("id")val id: Int,
    @SerializedName("username")val username: String,
    @SerializedName("password")val password: String,
    @SerializedName("email")val email: String,
    @SerializedName("datetime_of_creation")val datetime_of_creation: String,
    ): Serializable