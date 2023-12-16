package com.example.coursecontrol.model

data class Category(
    val id: Int,
    val courseId: Int,
    val name: String,
    val points: Int,
    val requirements: Int
)
