package com.example.coffeeshop.data.model

data class Promotion(
    val _id: String,
    val name: String,
    val description: String?,
    val type: String?,
    val scope: String?,
    val value: Double
)
