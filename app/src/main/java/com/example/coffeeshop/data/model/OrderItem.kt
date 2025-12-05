package com.example.coffeeshop.data.model

import java.io.Serializable

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val finalUnitPrice: Double,
    val sizeChosen: String? = null,
    val tempChosen: String? = null,
    val iceLevel: String? = null,
    val sugarLevel: String? = null,
    val chosenToppings: List<Topping> = emptyList(),
    val itemNote: String? = null
) : Serializable

