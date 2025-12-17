package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

object PointsDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"

    fun getUserPoints(
        userId: String,
        callback: (success: Boolean, message: String, points: Int?) -> Unit
    ) {
        val url = "$BASE_URL/points/$userId"
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
                    val json = JSONObject(body)          // org.json parsing JSON đơn giản. [web:938][web:941]
                    val points = json.optInt("points", 0)
                    callback(true, "OK", points)
                } catch (e: Exception) {
                    callback(false, "Lỗi parse JSON: ${e.message}", null)
                }
            }
        })
    }
}
