package com.example.coursecontrol

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coursecontrol.model.CategoryData
import com.example.coursecontrol.model.CourseData

class CourseDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detailed_course_view)

        val courseData: CourseData? = intent.getSerializableExtra("course_data") as? CourseData

        if (courseData != null) {
            val courseNameTextView: TextView = findViewById(R.id.textViewCourseName)
            courseNameTextView.text = courseData.course.name


            val categoriesTextView: TextView = findViewById(R.id.textViewCategories)
            categoriesTextView.text = buildCategoriesText(courseData.catagories)
        } else {
            Toast.makeText(this, "Error: Course details not available", Toast.LENGTH_SHORT).show()
            finish()
        }
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


                    for (subcategoryData in categoryData.subcategories) {
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

}