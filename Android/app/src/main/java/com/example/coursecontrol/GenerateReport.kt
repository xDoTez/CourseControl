package com.example.coursecontrol

import android.content.Context
import android.graphics.drawable.Drawable
import com.example.coursecontrol.model.CourseData

interface GenerateReport {
    fun generateReport()
    fun getIcon(context: Context) : Drawable
    fun getName(context: Context) : String
    fun setData(courseDataList: List<CourseData>)
}