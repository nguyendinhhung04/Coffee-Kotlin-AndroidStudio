package com.example.coffeeshop.data.dao

import com.example.coffeeshop.data.model.ComboItem
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Promotion
import com.example.coffeeshop.data.model.Size
import com.example.coffeeshop.data.model.TempOption
import com.example.coffeeshop.data.model.Topping
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ItemDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // ----------------------------
    // Lấy tất cả món
    // ----------------------------
    fun getAllItems(callback: (Boolean, String, List<Item>?) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/items")
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
                        val items = mutableListOf<Item>()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)

                            val sizes = parseSizes(obj.optJSONArray("sizes"))
                            val temps = parseTempOptions(obj.optJSONArray("tempOptions"))
                            val toppings = parseToppings(obj.optJSONArray("toppings"))

                            // ❌ iceLevels & sugarLevels không còn trong backend → list rỗng
                            val iceLevels = emptyList<String>()
                            val sugarLevels = emptyList<String>()

                            val item = Item(
                                _id = obj.optString("_id"),
                                name = obj.optString("name"),
                                category = obj.optString("category"),
                                image_url = obj.optString("image_url"),
                                basePrice = obj.optDouble("basePrice"),
                                description = obj.optString("description"),
                                sizes = sizes,
                                tempOptions = temps,
                                iceLevels = iceLevels,
                                sugarLevels = sugarLevels,
                                toppings = toppings,
                                isActive = obj.optBoolean("isActive", true),
                                promotion = parsePromotion(obj.optJSONObject("promotion")),                 // NEW
                                discountedPrice = obj.optDouble("discountedPrice",          // NEW
                                    obj.optDouble("basePrice"))

                            )
                            items.add(item)
                        }

                        callback(true, "Tải thành công ${items.size} món", items)

                    } catch (e: Exception) {
                        callback(false, "Lỗi parse JSON: ${e.message}", null)
                    }
                } else {
                    callback(false, "Lỗi server: ${response.code}", null)
                }
            }
        })
    }


    fun getTopSellingItems(
        callback: (success: Boolean, message: String, items: List<Item>?) -> Unit
    ) {
        val url = "$BASE_URL/items/top-selling"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    callback(false, "Lỗi server: ${response.code}", null)
                    return
                }
                try {
                    // ✅ parse object gốc
                    val root = JSONObject(body)
                    val success = root.optBoolean("success", false)
                    val msg = root.optString("message", "")

                    if (!success) {
                        callback(false, msg, null)
                        return
                    }

                    // ✅ mảng data bên trong
                    val dataArray = root.getJSONArray("data")
                    val items = mutableListOf<Item>()

                    for (i in 0 until dataArray.length()) {
                        val obj = dataArray.getJSONObject(i)

                        // API top-selling trả ít field hơn: totalSold, productId, name, image_url
                        // nên map đơn giản, chỉ dùng các field cần trong home
                        val item = Item(
                            _id = obj.optString("productId"),   // dùng productId làm id
                            name = obj.optString("name"),
                            category = "",
                            image_url = obj.optString("image_url"),
                            basePrice = 0.0,
                            description = "",
                            sizes = emptyList(),
                            tempOptions = emptyList(),
                            iceLevels = emptyList(),
                            sugarLevels = emptyList(),
                            toppings = emptyList(),
                            isActive = true,
                            promotion = parsePromotion(obj.optJSONObject("Promotion")),                 // NEW
                            discountedPrice = obj.optDouble("discountedPrice",          // NEW
                                obj.optDouble("basePrice"))

                        )
                        items.add(item)
                    }

                    callback(true, "Tải top-selling: ${items.size} món", items)

                } catch (e: Exception) {
                    callback(false, "Lỗi parse JSON: ${e.message}", null)
                }
            }
        })
    }


    // ----------------------------
    // Lọc theo category
    // ----------------------------
    fun getItemsByCategory(category: String, callback: (Boolean, String, List<Item>?) -> Unit) {
        val url = "$BASE_URL/items?category=$category"
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
                        val items = mutableListOf<Item>()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)

                            val sizes = parseSizes(obj.optJSONArray("sizes"))
                            val temps = parseTempOptions(obj.optJSONArray("tempOptions"))
                            val toppings = parseToppings(obj.optJSONArray("toppings"))

                            val item = Item(
                                _id = obj.optString("_id"),
                                name = obj.optString("name"),
                                category = obj.optString("category"),
                                image_url = obj.optString("image_url"),
                                basePrice = obj.optDouble("basePrice"),
                                description = obj.optString("description"),
                                sizes = sizes,
                                tempOptions = temps,
                                iceLevels = emptyList(),
                                sugarLevels = emptyList(),
                                toppings = toppings,
                                isActive = obj.optBoolean("isActive", true),
                                promotion = parsePromotion(obj.optJSONObject("promotion")),                 // NEW
                                discountedPrice = obj.optDouble("discountedPrice",          // NEW
                                    obj.optDouble("basePrice"))

                            )
                            items.add(item)
                        }

                        callback(true, "Tải $category: ${items.size} món", items)

                    } catch (e: Exception) {
                        callback(false, "Lỗi parse JSON: ${e.message}", null)
                    }
                } else {
                    callback(false, "Lỗi server: ${response.code}", null)
                }
            }
        })
    }

    // ----------------------------
    // Tìm kiếm
    // ----------------------------
    fun searchItems(query: String, callback: (Boolean, String, List<Item>?) -> Unit) {
        val url = "$BASE_URL/items?search=$query"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val jsonArray = JSONArray(body)
                        val items = mutableListOf<Item>()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)

                            val sizes = parseSizes(obj.optJSONArray("sizes"))
                            val temps = parseTempOptions(obj.optJSONArray("tempOptions"))
                            val toppings = parseToppings(obj.optJSONArray("toppings"))

                            val item = Item(
                                _id = obj.optString("_id"),
                                name = obj.optString("name"),
                                category = obj.optString("category"),
                                image_url = obj.optString("image_url"),
                                basePrice = obj.optDouble("basePrice"),
                                description = obj.optString("description"),
                                sizes = sizes,
                                tempOptions = temps,
                                iceLevels = emptyList(),
                                sugarLevels = emptyList(),
                                toppings = toppings,
                                isActive = obj.optBoolean("isActive", true),
                                promotion = parsePromotion(obj.optJSONObject("promotion")),                 // NEW
                                discountedPrice = obj.optDouble("discountedPrice",          // NEW
                                    obj.optDouble("basePrice"))

                            )
                            items.add(item)
                        }

                        callback(true, "Tìm thấy ${items.size} kết quả", items)

                    } catch (e: Exception) {
                        callback(false, "Lỗi parse JSON: ${e.message}", null)
                    }
                } else {
                    callback(false, "Không tìm thấy", null)
                }
            }
        })
    }

    // ----------------------------
    // HÀM HỖ TRỢ PARSE
    // ----------------------------
    private fun parseSizes(jsonArray: JSONArray?): List<Size> {
        val list = mutableListOf<Size>()
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(Size(obj.optString("name"), obj.optDouble("modifier"), obj.optString("label")))
            }
        }
        return list
    }

    private fun parseTempOptions(jsonArray: JSONArray?): List<TempOption> {
        val list = mutableListOf<TempOption>()
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(TempOption(obj.optString("name"), obj.optDouble("modifier"), obj.optString("label")))
            }
        }
        return list
    }

    private fun parseToppings(jsonArray: JSONArray?): List<Topping> {
        val list = mutableListOf<Topping>()
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(Topping(obj.optString("name"), obj.optDouble("price")))
            }
        }
        return list
    }

    private fun parsePromotion(json: JSONObject?): Promotion? {
        if (json == null) return null

        return Promotion(
            _id = json.optString("_id"),
            name = json.optString("name"),
            description = json.optString("description"),
            type = json.optString("type"),
            scope = json.optString("scope"),
            value = json.optDouble("value"),
            startDate = json.optString("startDate"),
            endDate = json.optString("endDate"),
            minOrderTotal = if (json.has("minOrderTotal")) json.optDouble("minOrderTotal") else null,
            isActive = json.optBoolean("isActive", true),
            productIds = jsonArrayToStringList(json.optJSONArray("productIds")),
            categories = jsonArrayToStringList(json.optJSONArray("categories")),
            comboItems = parseComboItems(json.optJSONArray("comboItems"))
        )
    }

    private fun parseComboItems(jsonArray: JSONArray?): List<ComboItem>? {
        if (jsonArray == null) return null
        val list = mutableListOf<ComboItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                ComboItem(
                    productId = obj.optString("productId"),
                    requiredQty = obj.optInt("requiredQty")
                )
            )
        }
        return list
    }

    private fun jsonArrayToStringList(jsonArray: JSONArray?): List<String> {
        val list = mutableListOf<String>()
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        }
        return list
    }


}