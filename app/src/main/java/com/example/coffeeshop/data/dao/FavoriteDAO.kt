package com.example.coffeeshop.data.dao

import android.util.Log
import com.example.coffeeshop.data.model.ComboItem
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Promotion
import com.example.coffeeshop.data.model.Size
import com.example.coffeeshop.data.model.TempOption
import com.example.coffeeshop.data.model.Topping
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

object FavoriteDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun getFavoritesByUser(
        userId: String,
        callback: (success: Boolean, message: String, items: List<Item>?) -> Unit
    ) {
        val url = "$BASE_URL/favorites/user/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr == null) {
                    callback(false, "Server error: ${response.code}", null)
                    return
                }
                try {
                    val arr = JSONArray(bodyStr)
                    val result = mutableListOf<Item>()
                    for (i in 0 until arr.length()) {
                        val favObj = arr.getJSONObject(i)
                        val productObj = favObj.getJSONObject("productId")  // lấy object sản phẩm
                        result.add(parseItem(productObj))
                    }
                    callback(true, "OK", result)
                } catch (e: Exception) {
                    callback(false, "Parse error: ${e.message}", null)
                }
            }
        })
    }

    fun addFavorite(
        userId: String,
        itemId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("userId", userId)
            put("itemId", itemId)
        }
        Log.d("Fav", "addFavorite body=$json")

        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url("$BASE_URL/favorites")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (response.isSuccessful) {
                    callback(true, "Added to favorites")
                } else {
                    val msg = try {
                        JSONObject(bodyStr ?: "").optString("message", "Add favorite failed")
                    } catch (e: Exception) {
                        "Add favorite failed: ${response.code}"
                    }
                    callback(false, msg)
                }
            }
        })
    }

    fun removeFavorite(
        userId: String,
        itemId: String,
        callback: (Boolean, String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("userId", userId)
            put("itemId", itemId)
        }
        val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url("$BASE_URL/favorites")
            .delete(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (response.isSuccessful) {
                    callback(true, "Removed from favorites")
                } else {
                    val msg = try {
                        JSONObject(bodyStr ?: "").optString("message", "Remove favorite failed")
                    } catch (e: Exception) {
                        "Remove favorite failed: ${response.code}"
                    }
                    callback(false, msg)
                }
            }
        })
    }
    private fun parseItem(obj: JSONObject): Item {
        // parse sizes
        val sizesJson = obj.optJSONArray("sizes")
        val sizes = mutableListOf<Size>()
        if (sizesJson != null) {
            for (i in 0 until sizesJson.length()) {
                val s = sizesJson.getJSONObject(i)
                sizes.add(
                    Size(
                        name = s.optString("name"),
                        modifier = s.optDouble("modifier", 0.0),
                        label = s.optString("label")
                    )
                )
            }
        }

        // parse tempOptions
        val tempsJson = obj.optJSONArray("tempOptions")
        val temps = mutableListOf<TempOption>()
        if (tempsJson != null) {
            for (i in 0 until tempsJson.length()) {
                val t = tempsJson.getJSONObject(i)
                temps.add(
                    TempOption(
                        name = t.optString("name"),
                        modifier = t.optDouble("modifier", 0.0),
                        label = t.optString("label")
                    )
                )
            }
        }

        // parse iceLevels
        val iceArr = obj.optJSONArray("iceLevels")
        val iceLevels = mutableListOf<String>()
        if (iceArr != null) {
            for (i in 0 until iceArr.length()) {
                iceLevels.add(iceArr.getString(i))
            }
        }

        // parse sugarLevels
        val sugarArr = obj.optJSONArray("sugarLevels")
        val sugarLevels = mutableListOf<String>()
        if (sugarArr != null) {
            for (i in 0 until sugarArr.length()) {
                sugarLevels.add(sugarArr.getString(i))
            }
        }

        // parse toppings
        val topsJson = obj.optJSONArray("toppings")
        val toppings = mutableListOf<Topping>()
        if (topsJson != null) {
            for (i in 0 until topsJson.length()) {
                val tp = topsJson.getJSONObject(i)
                toppings.add(
                    Topping(
                        name = tp.optString("name"),
                        price = tp.optDouble("price", 0.0)
                    )
                )
            }
        }

        val promotionObj = obj.optJSONObject("promotion")
        val promotion = if (promotionObj != null) parsePromotion(promotionObj) else null
        
        val basePrice = obj.optDouble("basePrice", 0.0)

        return Item(
            _id = obj.optString("_id"),
            name = obj.optString("name"),
            category = obj.optString("category"),
            image_url = obj.optString("image_url"),
            basePrice = basePrice,
            discountedPrice = obj.optDouble("discountedPrice", basePrice),
            description = obj.optString("description"),
            sizes = sizes,
            tempOptions = temps,
            iceLevels = iceLevels,
            sugarLevels = sugarLevels,
            toppings = toppings,
            promotion = promotion,
            isActive = obj.optBoolean("isActive", true)
        )
    }

    private fun parsePromotion(promoObj: JSONObject): Promotion {
        val productIdsJson = promoObj.optJSONArray("productIds")
        val productIds = mutableListOf<String>()
        if (productIdsJson != null) {
            for (j in 0 until productIdsJson.length()) {
                productIds.add(productIdsJson.getString(j))
            }
        }

        val categoriesJson = promoObj.optJSONArray("categories")
        val categories = mutableListOf<String>()
        if (categoriesJson != null) {
            for (j in 0 until categoriesJson.length()) {
                categories.add(categoriesJson.getString(j))
            }
        }

        val comboItemsJson = promoObj.optJSONArray("comboItems")
        val comboItems = mutableListOf<ComboItem>()
        if (comboItemsJson != null) {
            for (j in 0 until comboItemsJson.length()) {
                val comboItemObj = comboItemsJson.getJSONObject(j)
                comboItems.add(
                    ComboItem(
                        productId = comboItemObj.getString("productId"),
                        requiredQty = comboItemObj.getInt("requiredQty")
                    )
                )
            }
        }

        return Promotion(
            _id = promoObj.getString("_id"),
            name = promoObj.getString("name"),
            description = promoObj.getString("description"),
            type = promoObj.getString("type"),
            scope = promoObj.getString("scope"),
            value = promoObj.getDouble("value"),
            startDate = promoObj.optString("startDate"),
            endDate = promoObj.optString("endDate"),
            minOrderTotal = promoObj.optDouble("minOrderTotal"),
            isActive = promoObj.optBoolean("isActive", true),
            productIds = productIds,
            categories = categories,
            comboItems = comboItems
        )
    }

}
