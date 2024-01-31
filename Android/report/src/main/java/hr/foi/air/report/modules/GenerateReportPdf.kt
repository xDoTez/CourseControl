package hr.foi.air.report.modules

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import com.example.coursecontrol.R
import hr.foi.air.core.model.CategoryData
import hr.foi.air.core.model.CourseData
import java.io.File


class GenerateReportPdf: hr.foi.air.core.GenerateReport {

    private var courseDataList = mutableListOf<CourseData>()

    override fun generateReport(){
        val doc = PdfDocument()

        for(courseData in courseDataList){
            var iterator = 1
            var pageInfo = PdfDocument.PageInfo.Builder(595, 842, iterator).create()
            var currentPage = doc.startPage(pageInfo)
            var pageCanvas = currentPage.canvas
            var paint = Paint()
            paint.setColor(Color.BLACK)

            var courseDataText = "Course: " + courseData.course.name +
                    "\nSemester: " + courseData.course.semester +
                    " ECTS: " + courseData.course.ects + "\n\n"
            var categoriesText = buildCategoriesText(courseData.catagories)
            var courseText = courseDataText + categoriesText

            Log.d("pdf_zapis", courseText)

            val lines = courseText.split('\n')
            lines.forEachIndexed { i, line ->
                pageCanvas.drawText(line, 30F, 30F + (15F * i), paint)
            }

            //pageCanvas.drawText(courseText, 30F, 30F, paint)
            doc.finishPage(currentPage)
            iterator++
        }
        createPdfFile(doc)
        doc.close()
    }

    private fun createPdfFile(pdfDocument: PdfDocument){
        val reportFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!reportFolder.exists())
            reportFolder.mkdir()

        var file = File(reportFolder, "User_Courses_Report.pdf")
        pdfDocument.writeTo(file.outputStream())
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



    override fun getIcon(context: Context): Drawable {
        return AppCompatResources.getDrawable(context, R.drawable.icon_pdf_report_white)!!
    }

    override fun getName(context: Context): String {
        return context.getString(R.string.generate_pdf)
    }

    override fun setData(courseDataList: List<CourseData>) {
        for(course in courseDataList){
            this.courseDataList.add(course)
        }
    }

}

