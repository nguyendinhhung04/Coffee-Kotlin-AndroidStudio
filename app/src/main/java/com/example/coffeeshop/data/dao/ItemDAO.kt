package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object ItemDAO {
    private const val BASE_URL = "https://c76lgf-3000.csb.app/item"
    private val client = OkHttpClient()

    // 🟢 CREATE
    fun createItem(item: JSONObject, callback: Callback) {
        val body = item.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/create") // ✅ adjust if backend route differs
            .post(body)
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🔵 READ ALL (all categories)
    fun getAllItems(callback: Callback) {
        val request = Request.Builder()
            .url("$BASE_URL/all") // ✅ adjust if you have a general route
            .get()
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🔵 READ BY CATEGORY
    fun getItemsByCategory(category: String, callback: Callback) {
        val url = when (category.lowercase()) {
            "coffee" -> "$BASE_URL/coffee/getall"
            "chocolate" -> "$BASE_URL/chocolate/getall"
            "other" -> "$BASE_URL/other/getall"
            else -> "$BASE_URL/other/getall"
        }

        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🟡 UPDATE
    fun updateItem(id: String, item: JSONObject, callback: Callback) {
        val body = item.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/update/$id") // ✅ adjust based on backend route
            .put(body)
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🔴 DELETE
    fun deleteItem(id: String, callback: Callback) {
        val request = Request.Builder()
            .url("$BASE_URL/delete/$id") // ✅ adjust based on backend route
            .delete()
            .build()
        client.newCall(request).enqueue(callback)
    }
}
