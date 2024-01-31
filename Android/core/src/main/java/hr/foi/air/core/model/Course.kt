package hr.foi.air.core.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Course(
    @SerializedName("id")val id: Int,
    @SerializedName("name")val name: String,
    @SerializedName("semester")val semester: Int,
    @SerializedName("ects")val ects: Int
): Serializable
