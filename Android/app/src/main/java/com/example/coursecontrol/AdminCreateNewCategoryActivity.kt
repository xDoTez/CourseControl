package com.example.coursecontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecontrol.addNewCourse.SubcategoriesAdapter
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.NewCourseTempSaver
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminCreateNewCategoryActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_create_new_category)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }


        inflateLayout()
        onCreateCategorySelected()
        onCreateNewSubcategorySelected()
        onCancelCreatingNewCategorySelected()
    }

    fun inflateLayout(){
        val categoriesRecyclerView: RecyclerView = findViewById(R.id.allSubcategoriesRecyclerView)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)

        val subcategories = NewCourseTempSaver.getSubcategories()
        val subcategoriesAdapter = SubcategoriesAdapter(subcategories)
        categoriesRecyclerView.adapter = subcategoriesAdapter

        var points = 0
        var requirements = 0
        if(!subcategories.isNullOrEmpty()){
            for(subcategory in subcategories){
                points += subcategory.points
                requirements += subcategory.requirements
            }

            val pointsText = findViewById<EditText>(R.id.newCategoryPoints)
            val requirementsText = findViewById<EditText>(R.id.newCategoryRequirements)

            pointsText.setText(points.toString())
            requirementsText.setText(requirements.toString())
        }
    }

    private fun onCreateCategorySelected() {
        val btnCreateCategory = findViewById<Button>(R.id.btnCreateCategory)
        btnCreateCategory.setOnClickListener {
            val name = findViewById<EditText>(R.id.newCategoryName).text.toString()
            val points = findViewById<EditText>(R.id.newCategoryPoints).text.toString()
            val requirements = findViewById<EditText>(R.id.newCategoryRequirements).text.toString()

            if(name.isNotBlank() && points.isNotBlank() && requirements.isNotBlank()){
                NewCourseTempSaver.addCategory(name, points.toInt(), requirements.toInt())
                Log.d("name", name)
                Log.d("points", points)
                Log.d("requirements", requirements)
                finish()
            } else {
                Toast.makeText(this, "Insert data into all fields before adding a category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onCreateNewSubcategorySelected() {
        val btnCreateNewSubcategory = findViewById<Button>(R.id.btnCreateNewSubcategory)
        btnCreateNewSubcategory.setOnClickListener {
            val intent = Intent(this, AdminCreateNewSubcategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onCancelCreatingNewCategorySelected() {
        val btnCancel = findViewById<Button>(R.id.btnCancelCreatingNewCategory)
        btnCancel.setOnClickListener {
            NewCourseTempSaver.clearSubCategories()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        inflateLayout()
    }
}