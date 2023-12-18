package com.example.coursecontrol.model

data class CategoryData(
    val category: Category,
    val categoryUserData: CategoryUserData,
    val subcategories: List<SubcategoryData>,
    val requirements: List<Requirement>
)
