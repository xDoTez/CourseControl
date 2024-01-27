package com.example.coursecontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.coursecontrol.util.NavigationHandler
import com.example.coursecontrol.util.NewCourseTempSaver
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminCreateNewSubcategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_create_new_subcategory)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navigationHandler = NavigationHandler(this)

        bottomNavigationView.setOnItemSelectedListener { item ->
            navigationHandler.handleItemSelected(item)
        }

        onCreateSubcategorySelected()
        onCancelCreatingNewSubcategorySelected()
    }


    private fun onCreateSubcategorySelected() {
        val btnCreateSubcategory = findViewById<Button>(R.id.btnCreateSubcategory)
        btnCreateSubcategory.setOnClickListener {
            val name = findViewById<EditText>(R.id.newSubCategoryName).text.toString()
            val points = findViewById<EditText>(R.id.newSubCategoryPoints).text.toString()
            val requirements = findViewById<EditText>(R.id.newSubCategoryRequirements).text.toString()

            if(name.isNotBlank() && points.isNotBlank() && requirements.isNotBlank()){
                NewCourseTempSaver.addSubcategory(name, points.toInt(), requirements.toInt())
                Log.d("name", name)
                Log.d("points", points)
                Log.d("requirements", requirements)
                finish()
            } else {
                Toast.makeText(this, "Insert data into all fields before adding a subcategory", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onCancelCreatingNewSubcategorySelected() {
        val btnCancel = findViewById<Button>(R.id.btnCancelCreatingNewSubcategory)
        btnCancel.setOnClickListener {
            finish()
        }
    }
}