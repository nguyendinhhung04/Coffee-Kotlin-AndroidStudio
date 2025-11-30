package com.example.coffeeshop.data.model

data class CartItem(
    val item: Item,
    var quantity: Int = 1,
    val customizations: Map<String, String> = emptyMap(),
    val price: Double // Calculated based on base price + customizations
) {
    fun getTotalPrice(): Double = price * quantity
}
