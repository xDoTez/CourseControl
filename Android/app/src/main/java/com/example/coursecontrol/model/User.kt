package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date
public class User (
    @SerializedName("id")val id: Int,
    @SerializedName("username")val username: String,
    @SerializedName("password")val password: String,
    @SerializedName("email")val email: String,
    @SerializedName("datetime_of_creation")val datetime_of_creation: Date,
    )