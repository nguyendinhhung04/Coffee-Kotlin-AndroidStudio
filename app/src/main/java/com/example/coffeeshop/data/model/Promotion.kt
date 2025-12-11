package com.example.coffeeshop.data.model

data class Promotion(
    val _id: String,
    val name: String,
    val description: String,
    val type: String,
    val scope: String,
    val value: Double,
    val startDate: String = "",
    val endDate: String = "",
    val minOrderTotal: Double? = null,
    val isActive: Boolean = true,
    val productIds: List<String>? = null,
    val categories: List<String>? = null,
    val comboItems: List<ComboItem>? = null
)

data class ComboItem(
    val productId: String,
    val requiredQty: Int
)
