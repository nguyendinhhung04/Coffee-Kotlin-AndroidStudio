package com.example.coffeeshop.data.model

import java.io.Serializable

data class DeliveryAddress(
    val fullName: String,
    val phone: String,
    val street: String,
    val ward: String,
    val district: String,
    val city: String
) : Serializable

