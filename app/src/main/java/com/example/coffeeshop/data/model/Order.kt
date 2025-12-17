package com.example.coffeeshop.data.model

import java.io.Serializable

// Địa chỉ giao hàng
data class DeliveryAddress(
    val fullName: String,
    val phone: String,
    val street: String,
    val ward: String,
    val district: String,
    val city: String
) : Serializable

// Chi tiết từng item trong order
data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val finalUnitPrice: Double,
    val sizeChosen: String,
    val tempChosen: String,
    val iceLevel: String,
    val sugarLevel: String,
    val chosenToppings: List<Topping>,
    val itemNote: String
) : Serializable

// Order chính - Khớp với backend schema
data class Order(
    val _id: String? = null,
    val userId: String,
    val orderDate: String,// Timestamp (ms)
    val status: String, // "Pending", "Confirmed", "Delivering", "Delivered", "Cancelled"
    val paymentMethod: String, // "COD", "Transfer", "Card"
    val note: String,
    val subtotal: Double,
    val discountAmount: Double,
    val shippingFee: Double,
    val taxes: Double,
    val totalAmount: Double,
    val deliveryAddress: DeliveryAddress,
    val items: List<OrderItem>,
    val usedPointAmount: Number,
    val discountByPointAmount: Number,

    ) : Serializable {
    // Helper function để tính giá trị của từng item
    fun getItemTotal(index: Int): Double {
        return if (index in items.indices) items[index].finalUnitPrice * items[index].quantity else 0.0
    }

    // Helper function để lấy danh sách tùy chọn của item
    fun getItemOptions(index: Int): String {
        if (index !in items.indices) return ""
        val item = items[index]
        val options = mutableListOf<String>()

        if (item.sizeChosen.isNotEmpty()) options.add("Size: ${item.sizeChosen}")
        if (item.tempChosen.isNotEmpty()) options.add(item.tempChosen)
        if (item.iceLevel.isNotEmpty() && item.iceLevel != "N/A") options.add("Đá: ${item.iceLevel}")
        if (item.sugarLevel.isNotEmpty() && item.sugarLevel != "N/A") options.add("Đường: ${item.sugarLevel}")
        if (item.chosenToppings.isNotEmpty()) {
            options.add("Topping: ${item.chosenToppings.joinToString(", ") { it.name }}")
        }
        if (item.itemNote.isNotEmpty()) options.add("Ghi chú: ${item.itemNote}")

        return options.joinToString(" | ")
    }
}
