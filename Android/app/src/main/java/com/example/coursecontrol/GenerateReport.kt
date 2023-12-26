package com.example.coursecontrol

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import com.example.coursecontrol.model.CourseData

interface GenerateReport {
    fun getIcon(context: Context) : Drawable
    fun getName(context: Context) : String
    fun getActivity() : Activity
    fun setData(courseDataList: List<CourseData>)
}