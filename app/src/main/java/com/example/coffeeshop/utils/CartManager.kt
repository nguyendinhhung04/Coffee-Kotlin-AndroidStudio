package com.example.coffeeshop.utils

import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.data.model.Item

object CartManager {
    private val cartItems: MutableList<CartItem> = mutableListOf()
    private var totalAmount: Double = 0.0

    // Add or update item in cart
    fun addItem(cartItem: CartItem) {
        synchronized(this) {
            val existingIndex = cartItems.indexOfFirst {
                // use _id instead of id
                it.item._id == cartItem.item._id && it.customizations == cartItem.customizations
            }
            if (existingIndex != -1) {
                cartItems[existingIndex].quantity += cartItem.quantity
            } else {
                cartItems.add(cartItem)
            }
            updateTotal()
        }
    }

    // Remove item by id + customizations
    fun removeItem(itemId: String?, customizations: Map<String, String> = emptyMap()) {
        synchronized(this) {
            val index = cartItems.indexOfFirst {
                it.item._id == itemId && it.customizations == customizations
            }
            if (index != -1) {
                cartItems.removeAt(index)
                updateTotal()
            }
        }
    }

    // Update quantity for specific item
    fun updateQuantity(itemId: String?, customizations: Map<String, String>, newQuantity: Int) {
        synchronized(this) {
            val index = cartItems.indexOfFirst {
                it.item._id == itemId && it.customizations == customizations
            }
            if (index != -1 && newQuantity > 0) {
                cartItems[index].quantity = newQuantity
                updateTotal()
            } else if (newQuantity <= 0) {
                removeItem(itemId, customizations)
            }
        }
    }

    // Get total number of items (considering quantities)
    fun getItemCount(): Int {
        synchronized(this) {
            return cartItems.sumOf { it.quantity }
        }
    }

    fun isEmpty(): Boolean = getItemCount() == 0

    fun getAllItems(): List<CartItem> {
        synchronized(this) {
            return cartItems.toList()
        }
    }

    fun getTotalAmount(): Double {
        synchronized(this) {
            return totalAmount
        }
    }

    fun clearCart() {
        synchronized(this) {
            cartItems.clear()
            totalAmount = 0.0
        }
    }

    private fun updateTotal() {
        totalAmount = cartItems.sumOf { cartItem ->
            val item = cartItem.item
            val customizationCost = cartItem.price - item.basePrice
            val pricePerItem = if (item.discountedPrice > 0) {
                item.discountedPrice
            } else {
                item.basePrice
            }
            (pricePerItem + customizationCost) * cartItem.quantity
        }
    }

    fun getSubtotal(): Double = getTotalAmount()
}
