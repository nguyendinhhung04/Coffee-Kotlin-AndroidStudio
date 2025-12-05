package com.example.coffeeshop.data.model

import java.io.Serializable

data class Combo(
    val _id: String? = null,
    val name: String,
    val description: String,
    val image_url: String,
    val basePrice: Double,
    val items: List<OrderItem>,
    val isActive: Boolean = true
) : Serializable
