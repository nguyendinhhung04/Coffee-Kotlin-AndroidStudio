package com.example.coffeeshop.data.model

data class Item(
    val _id: String? = null,
    val name: String,
    val category: String,
    val image: String,
    val price: Double,
    val description: String
)
