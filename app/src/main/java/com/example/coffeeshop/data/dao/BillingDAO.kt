package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object BillingDAO {
    private const val BASE_URL = "https://c76lgf-3000.csb.app/billing"
    private val client = OkHttpClient()

    // 🟢 CREATE
    fun createBilling(billing: JSONObject, callback: Callback) {
        val body = billing.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/create")
            .post(body)
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🔵 READ ALL
    fun getAllBillings(callback: Callback) {
        val request = Request.Builder()
            .url("$BASE_URL/getall")
            .get()
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🟡 UPDATE
    fun updateBilling(id: String, billing: JSONObject, callback: Callback) {
        val body = billing.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/update/$id")
            .put(body)
            .build()
        client.newCall(request).enqueue(callback)
    }

    // 🔴 DELETE
    fun deleteBilling(id: String, callback: Callback) {
        val request = Request.Builder()
            .url("$BASE_URL/delete/$id")
            .delete()
            .build()
        client.newCall(request).enqueue(callback)
    }
}
