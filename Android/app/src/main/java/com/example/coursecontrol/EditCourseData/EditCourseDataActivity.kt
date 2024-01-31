package com.example.coursecontrol.EditCourseData

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.coursecontrol.CourseDetailsActivity
import com.example.coursecontrol.R
import com.example.coursecontrol.SessionToken
import com.example.coursecontrol.model.AddNewCourse
import com.example.coursecontrol.model.EditCourseData
import com.example.coursecontrol.network.YourRequestModel
import com.example.coursecontrol.util.RetrofitInstance
import com.example.coursecontrol.util.SessionManager
import hr.foi.air.core.model.Category
import hr.foi.air.core.model.CategoryData
import hr.foi.air.core.model.CategoryUserData
import hr.foi.air.core.model.CourseData
import hr.foi.air.core.model.Subcategory
import hr.foi.air.core.model.SubcategoryData
import hr.foi.air.core.model.SubcategoryUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditCourseDataActivity : AppCompatActivity() {
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private lateinit var categoryContainer: LinearLayout
    private val editTextCategoryList = mutableListOf<EditText>()
    private val editTextSubcategoryList = mutableListOf<EditText>()
    private lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_course_data)

        sessionManager = SessionManager(this)

        val courseData: CourseData? = intent.getSerializableExtra("course_data") as? CourseData

        if (courseData != null) {
            val courseName: TextView = findViewById(R.id.txtCourseName)
            courseName.text = courseData.course.name

            categoryContainer = findViewById(R.id.categoryContainer)

            for (categoryData in courseData.catagories) {
                addEditText(categoryData, null)

                for (subcategoryData in categoryData.subcategories!!) {
                    addEditText(null, subcategoryData)
                }
            }
        }

        btnCancel = findViewById(R.id.btnCancelEdit)
        btnCancel.setOnClickListener {
            finish()
        }


        btnSave = findViewById(R.id.btnSaveEdit)
        btnSave.setOnClickListener {

            if (courseData != null) {
                for (item in courseData.catagories) {
                    for (category in editTextCategoryList) {
                        if (item.category.id == category.id && item.subcategories?.isEmpty() == false) {
                            var points = 0
                            for (subcategory in item.subcategories!!) {
                                for (sub in editTextSubcategoryList) {
                                    if (subcategory.subcategory.id == sub.id) {
                                        points += sub.text.toString().toInt()
                                    }
                                }
                            }
                            if (points != category.text.toString().toInt()) {
                                pointsNotMatching(item.category.name)
                                return@setOnClickListener
                            }
                        }
                    }
                }
            }
            val sessionToken = sessionManager.getSessionToken()
            lateinit var request: EditCourseData
            if (sessionToken != null) {
                if (courseData != null) {
                    request = getRequestModel(sessionToken, courseData, editTextCategoryList, editTextSubcategoryList)
                    Log.d("request", request.toString())

                    val coroutineScope = CoroutineScope(Dispatchers.Default)
                    coroutineScope.launch {
                        makeApiCall(request)
                    }
                }
            }
            val intent = Intent(this, CourseDetailsActivity::class.java)
            intent.putExtra("course_data", request.course_data)
            startActivity(intent)
            finish()
        }
    }


    private fun addEditText(categoryData: CategoryData?, subcategoryData: SubcategoryData?) {
        val textView = TextView(this)
        if (categoryData != null) {
            textView.text = categoryData.category.name
        } else if (subcategoryData != null) {
            textView.text = subcategoryData.subcategory.name
        }

        val editText = EditText(this)
        if (categoryData != null) {
            editText.setText((categoryData.categoryUserData.points).toString())
            editText.id = categoryData.category.id
            editTextCategoryList.add(editText)
        } else if (subcategoryData != null) {
            editText.setText((subcategoryData.subcategoryUserData.points).toString())
            editText.id = subcategoryData.subcategory.id
            editTextSubcategoryList.add(editText)
        }

        categoryContainer.addView(textView)
        categoryContainer.addView(editText)
    }

    private fun getRequestModel(sessionToken: SessionToken, courseData: CourseData, editTextCategoryList: List<EditText>, editTextSubcategoryList: List<EditText>): EditCourseData {

        val requestModel = EditCourseData(
            session_token = YourRequestModel(
                user = sessionToken.user,
                session_token = sessionToken.session_token,
                expiration = sessionToken.expiration),
            course_data = CourseData(
                course = courseData.course,
                courseUserData = courseData.courseUserData,
                catagories = getCategories(courseData, editTextCategoryList, editTextSubcategoryList)
                )
            )
        return requestModel
    }

    private fun getCategories(courseData: CourseData, editTextCategoryList: List<EditText>, editTextSubcategoryList: List<EditText>): List<CategoryData> {

        var categoryList = mutableListOf<CategoryData>()
        var categoryItem: CategoryData

        for (category in courseData.catagories) {
            for (item in editTextCategoryList) {
                if (category.category.id == item.id) {
                    categoryItem = CategoryData(
                        category = Category(
                            id = category.category.id,
                            courseId = category.category.courseId,
                            name = category.category.name,
                            points = category.category.points,
                            requirements = category.category.requirements
                        ),
                        categoryUserData = CategoryUserData(
                            id = category.categoryUserData.id,
                            userCourseId = category.categoryUserData.userCourseId,
                            categoryId = category.categoryUserData.categoryId,
                            points = item.text.toString().toInt()
                        ),
                        subcategories = getSubcategories(category, editTextSubcategoryList)
                    )
                    categoryList.add(categoryItem)
                }
            }
        }
        return categoryList
    }

    private fun getSubcategories(category: CategoryData, editTextSubcategoryList: List<EditText>): List<SubcategoryData> {
        var subcategoryList = mutableListOf<SubcategoryData>()
        var subcategoryItem: SubcategoryData

        for (item in category.subcategories!!) {
            for (sub in editTextSubcategoryList) {
                if (item.subcategory.id == sub.id) {
                    subcategoryItem = SubcategoryData(
                        subcategory = Subcategory(
                            id = item.subcategory.id,
                            categoryId = item.subcategory.categoryId,
                            name = item.subcategory.name,
                            points = item.subcategory.points,
                            requirements = item.subcategory.requirements
                        ),
                        subcategoryUserData = SubcategoryUserData(
                            userCourseCategoryId = item.subcategoryUserData.userCourseCategoryId,
                            subcategoryId = item.subcategoryUserData.subcategoryId,
                            points = sub.text.toString().toInt()
                        )
                    )
                    subcategoryList.add(subcategoryItem)
                }
            }
        }
        return subcategoryList
    }
    
    suspend fun makeApiCall(request: EditCourseData) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.editCourseData(request)
            }

            handleApiResponse(response)

        } catch (e: Exception) {
            handleApiError(e)
        }
    }

    private fun handleApiError(e: Exception) {
        Log.e("EditCourseData", "API call failed", e)
    }

    private fun handleApiResponse(response: AddNewCourse) {
        if (response.status == "Success") {
            val newData = response.message
            Log.d("EditCourseData", "Successfully edited course data!")

        } else {
            Log.e("EditCourseData", "API call unsuccessful. Status: ${response.status}")
        }
    }

    private fun pointsNotMatching(category: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Points for category $category and its subcategory points doesn't match.")
        builder.setPositiveButton("OK")
        { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}