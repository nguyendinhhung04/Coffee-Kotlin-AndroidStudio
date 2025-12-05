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
    private const val BASE_URL = "https://c76lgf-3000.csb.app"
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
            put("totalAmount", subtotal + shippingFee - discountAmount)

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
            .url("$BASE_URL/orders/create")
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

    // 🔵 READ ALL - Lấy tất cả đơn hàng
    fun getAllOrders(callback: (Boolean, String, List<Order>?) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/orders")
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

    // 🔵 READ ALL - Lấy tất cả đơn hàng của user
    fun getOrdersByUserId(
        userId: String,
        callback: (success: Boolean, message: String, orders: List<Order>?) -> Unit
    ) {
        val url = "$BASE_URL/orders?userId=$userId"
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

    // 🔴 DELETE - Hủy đơn hàng
    fun cancelOrder(
        orderId: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$BASE_URL/orders/$orderId")
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
            orderDate = obj.optLong("orderDate"),
            status = obj.optString("status"),
            paymentMethod = obj.optString("paymentMethod"),
            note = obj.optString("note"),
            subtotal = obj.optDouble("subtotal"),
            discountAmount = obj.optDouble("discountAmount"),
            shippingFee = obj.optDouble("shippingFee"),
            taxes = obj.optDouble("taxes", 0.0),
            totalAmount = obj.optDouble("totalAmount"),
            deliveryAddress = deliveryAddress,
            items = items
        )
    }
}