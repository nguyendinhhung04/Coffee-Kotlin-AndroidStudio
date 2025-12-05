package com.example.coffeeshop.data.model

import java.io.Serializable

data class Size(
    val name: String,
    val modifier: Double,
    val label: String
) : Serializable

