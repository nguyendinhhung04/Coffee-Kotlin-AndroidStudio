package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.coffeeshop.data.model.Promotion

object PromotionDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun getActivePromotions(
        callback: (success: Boolean, message: String, promotions: List<Promotion>?) -> Unit
    ) {
        val url = "$BASE_URL/promotions/active"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
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
                    val result = mutableListOf<Promotion>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        result.add(parsePromotion(obj))
                    }
                    callback(true, "OK", result)
                } catch (e: Exception) {
                    callback(false, "Parse error: ${e.message}", null)
                }
            }
        })
    }

    private fun parsePromotion(obj: JSONObject): Promotion {
        return Promotion(
            _id = obj.optString("_id"),
            name = obj.optString("name"),
            description = obj.optString("description"),
            type = obj.optString("type"),
            scope = obj.optString("scope"),
            value = obj.optDouble("value", 0.0)
        )
    }
}
