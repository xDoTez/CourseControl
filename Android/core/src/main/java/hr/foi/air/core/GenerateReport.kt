package hr.foi.air.core

import android.content.Context
import android.graphics.drawable.Drawable
import hr.foi.air.core.model.CourseData

interface GenerateReport {
    fun generateReport()
    fun getIcon(context: Context) : Drawable
    fun getName(context: Context) : String
    fun setData(courseDataList: List<CourseData>)
}