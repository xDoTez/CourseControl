package hr.foi.air.core

import android.content.Context
import hr.foi.air.core.model.CourseData

interface GenerateReport {
    fun generateReport()
    fun getName(context: Context) : String
    fun setData(courseDataList: List<CourseData>)
}