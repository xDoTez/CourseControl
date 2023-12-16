package com.example.coursecontrol.model

data class Subcategory(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val points: Int,
    val requirements: Int
)
