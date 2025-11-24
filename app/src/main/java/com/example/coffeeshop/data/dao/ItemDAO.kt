package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ItemDAO {
    private val client = OkHttpClient()
    private const val BASE_URL = "https://c76lgf-3000.csb.app" // URL của bạn
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    // LẤY TẤT CẢ MÓN (ĐÃ HOẠT ĐỘNG VỚI SERVER HIỆN TẠI)
    fun getAllItems(callback: (Boolean, String, JSONArray?) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/items") // ĐÚNG ROUTE TRONG SERVER.JS
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
                        callback(true, "Tải thành công ${jsonArray.length()} món", jsonArray)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse JSON: ${e.message}", null)
                    }
                } else {
                    callback(false, "Lỗi server: ${response.code}", null)
                }
            }
        })
    }

    // LỌC THEO CATEGORY (dùng query ?category=Coffee)
    fun getItemsByCategory(category: String, callback: (Boolean, String, JSONArray?) -> Unit) {
        val url = "$BASE_URL/items?category=$category" // Dùng query thay vì route con
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
                        callback(true, "Tải $category: ${jsonArray.length()} món", jsonArray)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse", null)
                    }
                } else {
                    callback(false, "Lỗi: ${response.code}", null)
                }
            }
        })
    }

    // TÌM KIẾM THEO TÊN
    fun searchItems(query: String, callback: (Boolean, String, JSONArray?) -> Unit) {
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
                        callback(true, "Tìm thấy ${jsonArray.length()} kết quả", jsonArray)
                    } catch (e: Exception) {
                        callback(false, "Lỗi parse", null)
                    }
                } else {
                    callback(false, "Không tìm thấy", null)
                }
            }
        })
    }
}