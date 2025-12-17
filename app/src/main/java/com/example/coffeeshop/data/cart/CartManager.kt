package com.example.coffeeshop.data.cart

import com.example.coffeeshop.data.model.CartItem

object CartManager {
    private val _items = mutableListOf<CartItem>()
    val items: List<CartItem> get() = _items

    fun clearCart() {
        _items.clear()
    }

    fun addItem(cartItem: CartItem) {
        _items.add(cartItem)
    }
}
