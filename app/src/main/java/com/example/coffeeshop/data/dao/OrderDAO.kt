package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import com.example.coffeeshop.data.model.*
import java.io.IOException

object OrderDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // 🟢 CREATE - Tạo order mới
    fun createOrder(
        userId: String,
        items: List<OrderItem>,
        deliveryAddress: DeliveryAddress,
        subtotal: Double,
        discountAmount: Double,
        shippingFee: Double,
        paymentMethod: String,
        note: String,
        usedPointAmount: Double,
        discountByPointAmount: Double,
        callback: (success: Boolean, message: String, orderId: String?) -> Unit
    ) {
        val json = JSONObject().apply {
            put("userId", userId)
            put("orderDate", System.currentTimeMillis())
            put("status", "Pending")
            put("paymentMethod", paymentMethod)
            put("note", note)
            put("subtotal", subtotal)
            put("discountAmount", discountAmount)
            put("shippingFee", shippingFee)
            put("taxes", 0.0)
            put("usedPointAmount", usedPointAmount)
            put("discountByPointAmount", discountByPointAmount)
            put("totalAmount", subtotal + shippingFee - discountAmount - discountByPointAmount)

            // Địa chỉ giao hàng
            val addressObj = JSONObject().apply {
                put("fullName", deliveryAddress.fullName)
                put("phone", deliveryAddress.phone)
                put("street", deliveryAddress.street)
                put("ward", deliveryAddress.ward)
                put("district", deliveryAddress.district)
                put("city", deliveryAddress.city)
            }
            put("deliveryAddress", addressObj)

            // Items
            val itemsArray = JSONArray()
            items.forEach { item ->
                val itemObj = JSONObject().apply {
                    put("productId", item.productId)
                    put("productName", item.productName)
                    put("quantity", item.quantity)
                    put("finalUnitPrice", item.finalUnitPrice)
                    put("sizeChosen", item.sizeChosen)
                    put("tempChosen", item.tempChosen)
                    put("iceLevel", item.iceLevel)
                    put("sugarLevel", item.sugarLevel)

                    val toppingsArray = JSONArray()
                    item.chosenToppings.forEach { topping ->
                        toppingsArray.put(JSONObject().apply {
                            put("name", topping.name)
                            put("price", topping.price)
                        })
                    }
                    put("chosenToppings", toppingsArray)
                    put("itemNote", item.itemNote)
                }
                itemsArray.put(itemObj)
            }
            put("items", itemsArray)
        }

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/create")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val jsonRes = JSONObject(body)
                        val orderId = jsonRes.optString("_id", null)
                        callback(true, "Tạo đơn hàng thành công", orderId)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse: ${e.message}", null)
                    }
                } else {
                    callback(false, "Lỗi server: ${response.code}", null)
                }
            }
        })
    }

    // 🔵 READ - Lấy đơn hàng theo ID
    fun getOrderById(
        orderId: String,
        callback: (success: Boolean, message: String, order: Order?) -> Unit
    ) {
        val request = Request.Builder()
            .url("$BASE_URL/orders/$orderId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val order = parseOrder(JSONObject(body))
                        callback(true, "Tải thành công", order)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse: ${e.message}", null)
                    }
                } else {
                    callback(false, "Không tìm thấy đơn hàng", null)
                }
            }
        })
    }

    // 🔵 READ ALL - Lấy tất cả đơn hàng của user
    fun getOrdersByUserId(
        userId: String,
        callback: (success: Boolean, message: String, orders: List<Order>?) -> Unit
    ) {
        val url = "$BASE_URL/orders/filter?userId=$userId"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val jsonArray = JSONArray(body)
                        val orders = mutableListOf<Order>()
                        for (i in 0 until jsonArray.length()) {
                            orders.add(parseOrder(jsonArray.getJSONObject(i)))
                        }
                        // Sắp xếp theo ngày mới nhất trước
                        orders.sortByDescending { it.orderDate }
                        callback(true, "Tải ${orders.size} đơn hàng", orders)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse: ${e.message}", null)
                    }
                } else {
                    callback(false, "Lỗi server: ${response.code}", null)
                }
            }
        })
    }

    // 🟡 UPDATE - Cập nhật status order
    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("status", newStatus)
        }

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/orders/$orderId")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, "Cập nhật thành công")
                } else {
                    callback(false, "Lỗi server: ${response.code}")
                }
            }
        })
    }

    // 🟡 UPDATE - Cập nhật thông tin order đầy đủ (Address + ShippingFee + PaymentMethod)
    fun updateOrderFull(
        orderId: String,
        status: String,
        deliveryAddress: DeliveryAddress,
        paymentMethod: String,
        shippingFee: Double,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("status", status)
            put("paymentMethod", paymentMethod)
            put("shippingFee", shippingFee)

            val addressObj = JSONObject().apply {
                put("fullName", deliveryAddress.fullName)
                put("phone", deliveryAddress.phone)
                put("street", deliveryAddress.street)
                put("ward", deliveryAddress.ward)
                put("district", deliveryAddress.district)
                put("city", deliveryAddress.city)
            }
            put("deliveryAddress", addressObj)
        }

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/orders/$orderId")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, "Cập nhật thành công")
                } else {
                    callback(false, "Lỗi server: ${response.code}")
                }
            }
        })
    }

    // 🔴 DELETE - Hủy đơn hàng
    fun cancelOrder(
        orderId: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val url = "$BASE_URL/orders/usercancell/$orderId"

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, "Hủy đơn hàng thành công")
                } else {
                    callback(false, "Lỗi server: ${response.code}")
                }
            }
        })
    }

    // Helper: Parse Order từ JSON - Khớp với backend schema
    private fun parseOrder(obj: JSONObject): Order {
        val deliveryAddressJson = obj.getJSONObject("deliveryAddress")
        val deliveryAddress = DeliveryAddress(
            fullName = deliveryAddressJson.optString("fullName"),
            phone = deliveryAddressJson.optString("phone"),
            street = deliveryAddressJson.optString("street"),
            ward = deliveryAddressJson.optString("ward"),
            district = deliveryAddressJson.optString("district"),
            city = deliveryAddressJson.optString("city")
        )

        val itemsArray = obj.getJSONArray("items")
        val items = mutableListOf<OrderItem>()
        for (i in 0 until itemsArray.length()) {
            val itemJson = itemsArray.getJSONObject(i)
            val toppingsArray = itemJson.optJSONArray("chosenToppings") ?: JSONArray()
            val toppings = mutableListOf<Topping>()
            for (j in 0 until toppingsArray.length()) {
                val toppingJson = toppingsArray.getJSONObject(j)
                toppings.add(Topping(
                    name = toppingJson.optString("name"),
                    price = toppingJson.optDouble("price")
                ))
            }

            items.add(OrderItem(
                productId = itemJson.optString("productId"),
                productName = itemJson.optString("productName"),
                quantity = itemJson.optInt("quantity", 1),
                finalUnitPrice = itemJson.optDouble("finalUnitPrice"),
                sizeChosen = itemJson.optString("sizeChosen"),
                tempChosen = itemJson.optString("tempChosen"),
                iceLevel = itemJson.optString("iceLevel"),
                sugarLevel = itemJson.optString("sugarLevel"),
                chosenToppings = toppings,
                itemNote = itemJson.optString("itemNote")
            ))
        }

        return Order(
            _id = obj.optString("_id"),
            userId = obj.optString("userId"),
            orderDate = obj.optString("orderDate"),
            status = obj.optString("status"),
            paymentMethod = obj.optString("paymentMethod"),
            note = obj.optString("note"),
            subtotal = obj.optDouble("subtotal"),
            discountAmount = obj.optDouble("discountAmount"),
            shippingFee = obj.optDouble("shippingFee"),
            taxes = obj.optDouble("taxes", 0.0),
            totalAmount = obj.optDouble("totalAmount"),
            deliveryAddress = deliveryAddress,
            items = items,
            usedPointAmount = obj.optDouble("usedPointAmount", 0.0),
            discountByPointAmount = obj.optDouble("discountByPointAmount", 0.0)
        )
    }

    // Sample Orders for Testing
    fun getSampleOrders(): List<Order> {
        return listOf(
            Order(
                _id = "65b0e50f55e3a3c9e6d0a001",
                userId = "65b0e50f55e3a3c9e6d0a7a2",
                orderDate = "1705662300000",
                status = "Delivered",
                paymentMethod = "COD",
                note = "Giao hàng cẩn thận, không làm đổ.",
                subtotal = 108000.0,
                discountAmount = 0.0,
                shippingFee = 15000.0,
                taxes = 0.0,
                totalAmount = 123000.0,
                deliveryAddress = DeliveryAddress(
                    fullName = "Trần Thị B",
                    phone = "0987654321",
                    street = "250 Đường Sư Vạn Hạnh",
                    ward = "Phường 13",
                    district = "Quận 10",
                    city = "TP. Hồ Chí Minh"
                ),
                items = listOf(
                    OrderItem(
                        productId = "65b0e50f55e3a3c9e6d0a7b1",
                        productName = "Cà phê Sữa Đá",
                        quantity = 1,
                        finalUnitPrice = 35000.0,
                        sizeChosen = "L",
                        tempChosen = "Iced",
                        iceLevel = "50%",
                        sugarLevel = "70%",
                        chosenToppings = emptyList(),
                        itemNote = "Ít ngọt thôi."
                    ),
                    OrderItem(
                        productId = "65b0e50f55e3a3c9e6d0a7b2",
                        productName = "Latte Caramel Đá Xay",
                        quantity = 1,
                        finalUnitPrice = 73000.0,
                        sizeChosen = "M",
                        tempChosen = "Blended",
                        iceLevel = "100%",
                        sugarLevel = "100%",
                        chosenToppings = listOf(Topping("Shot Espresso Thêm", 12000.0)),
                        itemNote = "Nhiều kem tươi."
                    )
                ),
                usedPointAmount = 0.0,
                discountByPointAmount = 0.0
            ),
            Order(
                _id = "65b0e50f55e3a3c9e6d0a002",
                userId = "65b0e50f55e3a3c9e6d0a7a3",
                orderDate = "1709277000000",
                status = "Confirmed",
                paymentMethod = "COD",
                note = "Gọi điện trước khi giao hàng.",
                subtotal = 44000.0,
                discountAmount = 5000.0,
                shippingFee = 15000.0,
                taxes = 0.0,
                totalAmount = 54000.0,
                deliveryAddress = DeliveryAddress(
                    fullName = "Lê Văn C",
                    phone = "0912345678",
                    street = "123 Đường Điện Biên Phủ",
                    ward = "Phường 25",
                    district = "Quận Bình Thạnh",
                    city = "TP. Hồ Chí Minh"
                ),
                items = listOf(
                    OrderItem(
                        productId = "65b0e50f55e3a3c9e6d0a7b4",
                        productName = "Trà Đào Cam Sả",
                        quantity = 1,
                        finalUnitPrice = 44000.0,
                        sizeChosen = "L",
                        tempChosen = "Iced",
                        iceLevel = "50%",
                        sugarLevel = "50%",
                        chosenToppings = emptyList(),
                        itemNote = "Ít ngọt."
                    )
                ),
                usedPointAmount = 0.0,
                discountByPointAmount = 0.0
            ),
            Order(
                _id = "65b0e50f55e3a3c9e6d0a003",
                userId = "65b0e50f55e3a3c9e6d0a7a4",
                orderDate = "1712800800000",
                status = "Delivering",
                paymentMethod = "COD",
                note = "Không cần ghi chú gì thêm.",
                subtotal = 70000.0,
                discountAmount = 0.0,
                shippingFee = 15000.0,
                taxes = 0.0,
                totalAmount = 85000.0,
                deliveryAddress = DeliveryAddress(
                    fullName = "Phạm Thị D",
                    phone = "0909998887",
                    street = "700 Đường Lý Thường Kiệt",
                    ward = "Phường 1",
                    district = "Quận Tân Bình",
                    city = "TP. Hồ Chí Minh"
                ),
                items = listOf(
                    OrderItem(
                        productId = "65b0e50f55e3a3c9e6d0a7b7",
                        productName = "Cà phê Đen Đá",
                        quantity = 1,
                        finalUnitPrice = 25000.0,
                        sizeChosen = "M",
                        tempChosen = "Hot",
                        iceLevel = "N/A",
                        sugarLevel = "100%",
                        chosenToppings = emptyList(),
                        itemNote = "Rất nóng."
                    ),
                    OrderItem(
                        productId = "65b0e50f55e3a3c9e6d0a7b3",
                        productName = "Bánh Tiramisu",
                        quantity = 1,
                        finalUnitPrice = 45000.0,
                        sizeChosen = "Slice",
                        tempChosen = "N/A",
                        iceLevel = "N/A",
                        sugarLevel = "N/A",
                        chosenToppings = emptyList(),
                        itemNote = ""
                    )
                ),
                usedPointAmount = 0.0,
                discountByPointAmount = 0.0
            )
        )
    }
}
