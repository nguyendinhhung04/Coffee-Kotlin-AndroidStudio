package com.example.coffeeshop.data.model

import java.io.Serializable

data class Item(
    val _id: String? = null,
    val name: String,
    val image_url: String,
    val basePrice: Double,
    val description: String,
    val sizes: List<Size> = emptyList(),
    val tempOptions: List<TempOption> = emptyList(),
    val toppings: List<Topping> = emptyList(),
    val iceLevels: List<String> = emptyList(),
    val sugarLevels: List<String> = emptyList(),
    val isActive: Boolean = true,
    val categories: List<String> = emptyList()
) : Serializable

