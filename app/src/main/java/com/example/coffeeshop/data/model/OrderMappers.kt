package com.example.coffeeshop.data.model

import com.example.coffeeshop.utils.CartManager
import java.time.Instant

data class OrderItemDTO(
    val productId: String?,
    val productName: String,
    val quantity: Int,
    val finalUnitPrice: Double,
    val sizeChosen: String?,
    val tempChosen: String?,
    val iceLevel: String?,
    val sugarLevel: String?,
    val chosenToppings: List<Map<String, Any>>,
    val itemNote: String?
)

data class OrderDTO(
    val id: String? = null,
    val userId: String,
    val orderDate: String,
    val status: String,
    val paymentMethod: String,
    val note: String?,
    val subtotal: Double,
    val discountAmount: Double,
    val shippingFee: Double,
    val taxes: Double,
    val totalAmount: Double,
    val deliveryAddress: Map<String, String>,
    val items: List<OrderItemDTO>
)

fun buildOrderDTOFromCart(
    userId: String,
    status: String = "Unpaid",
    paymentMethod: String = "COD",
    note: String? = null,
    deliveryAddress: Map<String, String>
): OrderDTO {
    val cartItems = CartManager.getAllItems()
    val subtotal = CartManager.getSubtotal()
    val discount = 0.0
    val shipping = 0.0
    val taxes = 0.0
    val total = subtotal - discount + shipping + taxes

    val orderItems = cartItems.map { cartItem ->
        val c = cartItem.customizations
        val sizeChosen = c["size"]
        val tempChosen = c["temp"]
        val iceLevel = c["ice"] ?: "N/A"
        val sugarLevel = c["sugar"] ?: "N/A"

        val toppingsNames = c["toppings"]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val chosenToppings = toppingsNames.mapNotNull { topName ->
            cartItem.item.toppings.firstOrNull { it.name == topName }?.let {
                mapOf("name" to it.name, "price" to it.price)
            }
        }

        OrderItemDTO(
            productId = cartItem.item._id,
            productName = cartItem.item.name,
            quantity = cartItem.quantity,
            finalUnitPrice = cartItem.price,
            sizeChosen = sizeChosen,
            tempChosen = tempChosen,
            iceLevel = iceLevel,
            sugarLevel = sugarLevel,
            chosenToppings = chosenToppings,
            itemNote = c["note"]
        )
    }

    return OrderDTO(
        userId = userId,
        orderDate = Instant.now().toString(),
        status = status,
        paymentMethod = paymentMethod,
        note = note,
        subtotal = subtotal,
        discountAmount = discount,
        shippingFee = shipping,
        taxes = taxes,
        totalAmount = total,
        deliveryAddress = deliveryAddress,
        items = orderItems
    )
}
