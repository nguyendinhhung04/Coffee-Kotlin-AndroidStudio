package com.example.coffeeshop.utils

import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.OrderItem
import com.example.coffeeshop.data.model.Topping

// ============================================================================
// CartItem - Đại diện cho sản phẩm trong giỏ hàng
// ============================================================================
data class CartItem(
    val item: Item,
    val quantity: Int = 1,
    val sizeChosen: String = "",
    val tempChosen: String = "",
    val iceLevel: String = "",
    val sugarLevel: String = "",
    val chosenToppings: List<Topping> = emptyList(),
    val itemNote: String = ""
) {
    /**
     * Tính giá của 1 item (không nhân với quantity)
     */
    fun getUnitPrice(): Double {
        var price = item.basePrice

        // Thêm giá size
        item.sizes.find { it.name == sizeChosen }?.let { size ->
            price += size.modifier
        }

        // Thêm giá temp option
        item.tempOptions.find { it.name == tempChosen }?.let { temp ->
            price += temp.modifier
        }

        // Thêm giá toppings
        chosenToppings.forEach { topping ->
            price += topping.price
        }

        return price
    }

    /**
     * Tính tổng giá (unit price × quantity)
     */
    fun getTotalPrice(): Double = getUnitPrice() * quantity

    /**
     * Chuyển CartItem thành OrderItem
     */
    fun toOrderItem(): OrderItem {
        return OrderItem(
            productId = item._id ?: "",
            productName = item.name,
            quantity = quantity,
            finalUnitPrice = getUnitPrice(),
            sizeChosen = sizeChosen,
            tempChosen = tempChosen,
            iceLevel = iceLevel,
            sugarLevel = sugarLevel,
            chosenToppings = chosenToppings,
            itemNote = itemNote
        )
    }
}

// ============================================================================
// CartManager - Quản lý giỏ hàng (Singleton Object)
// ============================================================================
object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    /**
     * Thêm item vào giỏ hàng
     * Nếu item có tùy chọn giống nhau, chỉ tăng quantity
     */
    fun addItem(cartItem: CartItem) {
        // Kiểm tra xem item có tùy chọn giống nhau không
        val existingIndex = cartItems.indexOfFirst { existing ->
            existing.item._id == cartItem.item._id &&
                    existing.sizeChosen == cartItem.sizeChosen &&
                    existing.tempChosen == cartItem.tempChosen &&
                    existing.iceLevel == cartItem.iceLevel &&
                    existing.sugarLevel == cartItem.sugarLevel &&
                    existing.chosenToppings == cartItem.chosenToppings &&
                    existing.itemNote == cartItem.itemNote
        }

        if (existingIndex >= 0) {
            // Cập nhật quantity của item hiện tại
            val existing = cartItems[existingIndex]
            cartItems[existingIndex] = existing.copy(quantity = existing.quantity + cartItem.quantity)
        } else {
            // Thêm item mới
            cartItems.add(cartItem)
        }
    }

    /**
     * Xóa item khỏi giỏ (theo index)
     */
    fun removeItem(index: Int) {
        if (index in cartItems.indices) {
            cartItems.removeAt(index)
        }
    }

    /**
     * Cập nhật số lượng của item
     */
    fun updateQuantity(index: Int, quantity: Int) {
        if (index in cartItems.indices) {
            if (quantity <= 0) {
                removeItem(index)
            } else {
                cartItems[index] = cartItems[index].copy(quantity = quantity)
            }
        }
    }

    /**
     * Lấy tất cả items trong giỏ
     */
    fun getItems(): List<CartItem> = cartItems.toList()

    /**
     * Tính tổng tiền (subtotal)
     */
    fun getSubtotal(): Double = cartItems.sumOf { it.getTotalPrice() }

    /**
     * Lấy số lượng loại sản phẩm
     */
    fun getItemCount(): Int = cartItems.size

    /**
     * Lấy tổng số lượng sản phẩm (tính cả quantity)
     */
    fun getTotalQuantity(): Int = cartItems.sumOf { it.quantity }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    fun clearCart() {
        cartItems.clear()
    }

    /**
     * Kiểm tra giỏ hàng có trống không
     */
    fun isEmpty(): Boolean = cartItems.isEmpty()

    /**
     * Lấy item tại vị trí index
     */
    fun getItem(index: Int): CartItem? {
        return if (index in cartItems.indices) cartItems[index] else null
    }

    /**
     * Convert tất cả CartItem thành OrderItem list
     */
    fun toOrderItems(): List<OrderItem> {
        return cartItems.map { it.toOrderItem() }
    }
}