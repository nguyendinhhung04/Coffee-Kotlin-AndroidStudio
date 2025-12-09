package com.example.coffeeshop.data.dao

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Size
import com.example.coffeeshop.data.model.TempOption
import com.example.coffeeshop.data.model.Topping
import okhttp3.RequestBody.Companion.toRequestBody

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

        return Item(
            _id = obj.optString("_id"),
            name = obj.optString("name"),
            category = obj.optString("category"),
            image_url = obj.optString("image_url"),
            basePrice = obj.optDouble("basePrice", 0.0),
            description = obj.optString("description"),
            sizes = sizes,
            tempOptions = temps,
            iceLevels = iceLevels,
            sugarLevels = sugarLevels,
            toppings = toppings,
            isActive = obj.optBoolean("isActive", true)
        )
    }

}
