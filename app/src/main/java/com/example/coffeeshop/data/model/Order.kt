package com.example.coffeeshop.data.model

import java.io.Serializable

data class Order(
    val _id: String? = null,
    val userId: String,
    val orderDate: Long,
    val status: String,
    val items: List<OrderItem>,
    val deliveryAddress: DeliveryAddress,
    val subtotal: Double,
    val discountAmount: Double,
    val shippingFee: Double,
    val taxes: Double,
    val totalAmount: Double,
    val paymentMethod: String? = null,
    val note: String? = null
) : Serializable

