package com.example.coffeeshop.data.dao

import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Size
import com.example.coffeeshop.data.model.TempOption
import com.example.coffeeshop.data.model.Topping
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.coffeeshop.data.model.Combo
import com.example.coffeeshop.data.model.OrderItem

object ItemDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://c76lgf-3000.csb.app"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // ----------------------------
    // Tạo combo mới
    // ----------------------------
    fun createCombo(combo: Combo, callback: (Boolean, String, Combo?) -> Unit) {
        val json = JSONObject().apply {
            put("name", combo.name)
            put("description", combo.description)
            put("image_url", combo.image_url)
            put("basePrice", combo.basePrice)
            put("isActive", combo.isActive)

            val itemsArray = JSONArray()
            combo.items.forEach { orderItem ->
                val itemObj = JSONObject().apply {
                    put("productId", orderItem.productId)
                    put("productName", orderItem.productName)
                    put("quantity", orderItem.quantity)
                    put("finalUnitPrice", orderItem.finalUnitPrice)
                    put("sizeChosen", orderItem.sizeChosen)
                    put("tempChosen", orderItem.tempChosen)
                    put("iceLevel", orderItem.iceLevel)
                    put("sugarLevel", orderItem.sugarLevel)
                    val toppingsArray = JSONArray()
                    orderItem.chosenToppings.forEach { topping ->
                        toppingsArray.put(JSONObject().apply {
                            put("name", topping.name)
                            put("price", topping.price)
                        })
                    }
                    put("chosenToppings", toppingsArray)
                    put("itemNote", orderItem.itemNote)
                }
                itemsArray.put(itemObj)
            }
            put("items", itemsArray)
        }

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/combos") // Assuming a /combos endpoint
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val createdCombo = parseCombo(jsonResponse)
                        callback(true, "Thêm combo thành công", createdCombo)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse JSON: ${e.message}", null)
                    }
                } else {
                    callback(false, "Lỗi server: ${response.code}", null)
                }
            }
        })
    }

    private fun parseCombo(obj: JSONObject): Combo {
        val itemsArray = obj.optJSONArray("items")
        val orderItems = mutableListOf<OrderItem>()
        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val itemJson = itemsArray.getJSONObject(i)
                val toppingsArray = itemJson.optJSONArray("chosenToppings")
                val chosenToppings = mutableListOf<Topping>()
                if (toppingsArray != null) {
                    for (j in 0 until toppingsArray.length()) {
                        val toppingJson = toppingsArray.getJSONObject(j)
                        chosenToppings.add(Topping(
                            name = toppingJson.optString("name"),
                            price = toppingJson.optDouble("price")
                        ))
                    }
                }
                orderItems.add(OrderItem(
                    productId = itemJson.optString("productId"),
                    productName = itemJson.optString("productName"),
                    quantity = itemJson.optInt("quantity", 1),
                    finalUnitPrice = itemJson.optDouble("finalUnitPrice"),
                    sizeChosen = itemJson.optString("sizeChosen"),
                    tempChosen = itemJson.optString("tempChosen"),
                    iceLevel = itemJson.optString("iceLevel"),
                    sugarLevel = itemJson.optString("sugarLevel"),
                    chosenToppings = chosenToppings,
                    itemNote = itemJson.optString("itemNote")
                ))
            }
        }
        return Combo(
            _id = obj.optString("_id"),
            name = obj.optString("name"),
            description = obj.optString("description"),
            image_url = obj.optString("image_url"),
            basePrice = obj.optDouble("basePrice"),
            items = orderItems,
            isActive = obj.optBoolean("isActive", true)
        )
    }

    // ----------------------------
    // Tạo món mới
    // ----------------------------
    fun createItem(item: Item, callback: (Boolean, String, Item?) -> Unit) {
        val json = JSONObject().apply {
            put("name", item.name)
            put("category", item.category)
            put("image_url", item.image_url)
            put("basePrice", item.basePrice)
            put("description", item.description)

            val sizesArray = JSONArray()
            item.sizes.forEach { size ->
                sizesArray.put(JSONObject().apply {
                    put("name", size.name)
                    put("modifier", size.modifier)
                    put("label", size.label)
                })
            }
            put("sizes", sizesArray)

            val tempOptionsArray = JSONArray()
            item.tempOptions.forEach { temp ->
                tempOptionsArray.put(JSONObject().apply {
                    put("name", temp.name)
                    put("modifier", temp.modifier)
                    put("label", temp.label)
                })
            }
            put("tempOptions", tempOptionsArray)

            val toppingsArray = JSONArray()
            item.toppings.forEach { topping ->
                toppingsArray.put(JSONObject().apply {
                    put("name", topping.name)
                    put("price", topping.price)
                })
            }
            put("toppings", toppingsArray)

            put("iceLevels", JSONArray(item.iceLevels))
            put("sugarLevels", JSONArray(item.sugarLevels))
            put("isActive", item.isActive)
        }

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/items")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val createdItem = Item(
                            _id = jsonResponse.optString("_id"),
                            name = jsonResponse.optString("name"),
                            category = jsonResponse.optString("category"),
                            image_url = jsonResponse.optString("image_url"),
                            basePrice = jsonResponse.optDouble("basePrice"),
                            description = jsonResponse.optString("description"),
                            sizes = parseSizes(jsonResponse.optJSONArray("sizes")),
                            tempOptions = parseTempOptions(jsonResponse.optJSONArray("tempOptions")),
                            iceLevels = jsonArrayToStringList(jsonResponse.optJSONArray("iceLevels")),
                            sugarLevels = jsonArrayToStringList(jsonResponse.optJSONArray("sugarLevels")),
                            toppings = parseToppings(jsonResponse.optJSONArray("toppings")),
                            isActive = jsonResponse.optBoolean("isActive", true)
                        )
                        callback(true, "Thêm món thành công", createdItem)
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
                            val iceLevels = jsonArrayToStringList(obj.optJSONArray("iceLevels"))
                            val sugarLevels = jsonArrayToStringList(obj.optJSONArray("sugarLevels"))

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
                                isActive = obj.optBoolean("isActive", true)
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
                            val iceLevels = jsonArrayToStringList(obj.optJSONArray("iceLevels"))
                            val sugarLevels = jsonArrayToStringList(obj.optJSONArray("sugarLevels"))

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
                                isActive = obj.optBoolean("isActive", true)
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
                            val iceLevels = jsonArrayToStringList(obj.optJSONArray("iceLevels"))
                            val sugarLevels = jsonArrayToStringList(obj.optJSONArray("sugarLevels"))

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
                                isActive = obj.optBoolean("isActive", true)
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

    private fun jsonArrayToStringList(jsonArray: JSONArray?): List<String> {
        val list = mutableListOf<String>()
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        }
        return list
    }

    // ----------------------------
    // Lấy món gợi ý theo mùa
    // ----------------------------
    fun getRecommendedItemsBySeason(seasonKeywords: List<String>, callback: (Boolean, String, List<Item>?) -> Unit) {
        // Lấy tất cả món và filter theo từ khóa mùa
        getAllItems { success, message, allItems ->
            if (success && allItems != null) {
                val recommendedItems = allItems.filter { item ->
                    val itemNameLower = item.name.lowercase()
                    val itemDescLower = item.description.lowercase()
                    val itemCategoryLower = item.category.lowercase()
                    
                    // Kiểm tra xem món có chứa từ khóa mùa không
                    seasonKeywords.any { keyword ->
                        itemNameLower.contains(keyword.lowercase()) ||
                        itemDescLower.contains(keyword.lowercase()) ||
                        itemCategoryLower.contains(keyword.lowercase())
                    }
                }.take(4) // Lấy tối đa 4 món gợi ý
                
                callback(true, "Tìm thấy ${recommendedItems.size} món gợi ý", recommendedItems)
            } else {
                callback(false, message, null)
            }
        }
    }

    // ----------------------------
    // Lấy món gợi ý theo thời tiết
    // ----------------------------
    fun getRecommendedItemsByWeather(weatherKeywords: List<String>, callback: (Boolean, String, List<Item>?) -> Unit) {
        // Lấy tất cả món và filter theo từ khóa thời tiết
        getAllItems { success, message, allItems ->
            if (success && allItems != null) {
                val recommendedItems = allItems.filter { item ->
                    val itemNameLower = item.name.lowercase()
                    val itemDescLower = item.description.lowercase()
                    val itemCategoryLower = item.category.lowercase()
                    
                    // Kiểm tra xem món có chứa từ khóa thời tiết không
                    weatherKeywords.any { keyword ->
                        itemNameLower.contains(keyword.lowercase()) ||
                        itemDescLower.contains(keyword.lowercase()) ||
                        itemCategoryLower.contains(keyword.lowercase())
                    }
                }.take(4) // Lấy tối đa 4 món gợi ý
                
                callback(true, "Tìm thấy ${recommendedItems.size} món gợi ý", recommendedItems)
            } else {
                callback(false, message, null)
            }
        }
    }
}