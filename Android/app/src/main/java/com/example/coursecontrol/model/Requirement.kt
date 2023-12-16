package com.example.coursecontrol.model

data class Requirement(
    val id: Int,
    val categoryId: Int,
    val subcategoryId: Int,
    val name: String,
    val points: Int
)
