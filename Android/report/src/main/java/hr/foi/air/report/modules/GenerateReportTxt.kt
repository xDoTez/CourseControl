package hr.foi.air.report.modules

import android.content.Context
import android.os.Environment
import hr.foi.air.core.GenerateReport
import hr.foi.air.core.model.CategoryData
import hr.foi.air.core.model.CourseData
import java.io.File
import java.io.FileWriter


class GenerateReportTxt: GenerateReport {

    private var courseDataList = mutableListOf<CourseData>()

    override fun generateReport() {
        val reportFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!reportFolder.exists())
            reportFolder.mkdir()
        var file = File(reportFolder, "User_Courses_Report.txt")
        val writer = FileWriter(file)

        for(courseData in courseDataList){

            var courseDataText = "Course: " + courseData.course.name +
                    "\nSemester: " + courseData.course.semester +
                    " ECTS: " + courseData.course.ects + "\n\n"
            var categoriesText = buildCategoriesText(courseData.catagories)
            var courseText = courseDataText + categoriesText + "\n\n"

            writer.append(courseText)
            writer.flush()
        }
        writer.close()
    }

    private fun buildCategoriesText(categories: List<CategoryData>?): String {
        val stringBuilder = StringBuilder()
        var totalUserPoints = 0
        var totalCoursePoints = 0
        if (categories != null) {
            for (categoryData in categories) {
                val category = categoryData.category
                stringBuilder.append("${category.name}\n")

                val requirement = category.requirements

                stringBuilder.append("   - Requirement: $requirement points\n")


                for (subcategoryData in categoryData.subcategories!!) {
                    val subcategory = subcategoryData.subcategory
                    stringBuilder.append("   - ${subcategory.name}: Points - ${subcategory.points}: Requirement - ${subcategory.requirements}\n")

                }

                stringBuilder.append("   - User Points: ${categoryData.categoryUserData.points}\n")
                totalUserPoints += categoryData.categoryUserData.points



                val totalCategoryPoints = categoryData.category.points
                stringBuilder.append("   - Total Points for ${category.name}: $totalCategoryPoints\n")
                totalCoursePoints += totalCategoryPoints
                stringBuilder.append("\n")
                stringBuilder.append("\n")
                stringBuilder.append("\n")
                stringBuilder.append("\n")

            }
        }
        stringBuilder.append("   Total Points achieved: $totalUserPoints / $totalCoursePoints \n")
        return stringBuilder.toString()
    }


    override fun getName(context: Context): String {
        return "Generate Txt"
    }

    override fun setData(courseDataList: List<CourseData>) {
        for(course in courseDataList){
            this.courseDataList.add(course)
        }
    }
}