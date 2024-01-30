package com.example.coursecontrol.util

import com.example.coursecontrol.model.NewCategory
import com.example.coursecontrol.model.NewCourse
import com.example.coursecontrol.model.NewSubcategory

object NewCourseTempSaver {
    private var categoriesList = mutableListOf<NewCategory>()
    private var subcategoriesList = mutableListOf <NewSubcategory>()

    fun addCategory(name: String, points: Int, requirements: Int){
        var newSubcategoriesList = mutableListOf<NewSubcategory>()
        for(subcategory in subcategoriesList){
            newSubcategoriesList.add(subcategory)
        }
        var category = NewCategory(
            name = name,
            points = points,
            requirements = requirements,
            subcategories = newSubcategoriesList
        )
        categoriesList.add(category)

        subcategoriesList.clear()
    }

    fun addSubcategory(name: String, points: Int, requirements: Int){
        var subcategory = NewSubcategory(
            name = name,
            points = points,
            requirements = requirements
        )
        subcategoriesList.add(subcategory)
    }

    fun createCourse(name: String, semester: Int, ects: Int): NewCourse{
        var newCategoriesList = mutableListOf<NewCategory>()
        for(category in categoriesList){
            newCategoriesList.add(category)
        }

        val newCourse = NewCourse(
            name = name,
            semester = semester,
            ects = ects,
            catagories = newCategoriesList
        )

        categoriesList.clear()

        return newCourse
    }

    fun getCategories(): MutableList<NewCategory>{
        return categoriesList
    }

    fun getSubcategories(): MutableList<NewSubcategory>{
        return subcategoriesList
    }

    fun clearCategories(){
        categoriesList.clear()
    }

    fun clearSubCategories(){
        subcategoriesList.clear()
    }
}