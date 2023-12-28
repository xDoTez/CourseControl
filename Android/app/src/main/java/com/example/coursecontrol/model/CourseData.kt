package com.example.coursecontrol.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CourseData(
    @SerializedName("course")val course: Course,
    @SerializedName("course_user_data")val courseUserData: CourseUserData,
    @SerializedName("categories")val catagories: List<CategoryData>
): Serializable