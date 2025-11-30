package com.example.coffeeshop.data.model

import java.io.Serializable

data class Item(
    val _id: String? = null,
    val name: String,
    val category: String,
    val image_url: String, // trùng backend
    val basePrice: Double,
    val description: String,
    val sizes: List<Size> = emptyList(),
    val tempOptions: List<TempOption> = emptyList(),
    val iceLevels: List<String> = emptyList(),
    val sugarLevels: List<String> = emptyList(),
    val toppings: List<Topping> = emptyList(),
    val isActive: Boolean = true
) : Serializable

data class Size(
    val name: String,
    val modifier: Double,
    val label: String
) : Serializable

data class TempOption(
    val name: String,
    val modifier: Double,
    val label: String
) : Serializable

data class Topping(
    val name: String,
    val price: Double
) : Serializable
