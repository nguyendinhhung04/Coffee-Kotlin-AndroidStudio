package com.example.coffeeshop.data.dao

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

object UserDAO {
    private val client = OkHttpClient()
    // Make sure this matches your backend URL
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Login Function
     * Updates:
     * 1. Returns a 'token' string in the callback.
     * 2. Parses the 'message' from the server response.
     */
    fun checkLogin(username: String, password: String, callback: (success: Boolean, message: String, user: JSONObject?, token: String?) -> Unit) {
        val json = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val requestBody = RequestBody.create(JSON_MEDIA_TYPE, json.toString())

        val request = Request.Builder()
            .url("$BASE_URL/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}", null, null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonRes = JSONObject(responseBody)
                        val message = jsonRes.optString("message", "Login successful")
                        val user = jsonRes.optJSONObject("user")
                        // Extract the token sent by the Node.js backend
                        val token = jsonRes.optString("token")

                        callback(true, message, user, token)
                    } catch (e: Exception) {
                        callback(false, "Login failed: Invalid JSON response", null, null)
                    }
                } else {
                    // Handle server errors (401, 404, etc.)
                    val errorMsg = try {
                        JSONObject(responseBody ?: "").optString("message", "Login failed")
                    } catch (e: Exception) {
                        "Login failed: ${response.code}"
                    }
                    callback(false, errorMsg, null, null)
                }
            }
        })
    }

    fun updateUser(
        userId: String,
        fullName: String,
        email: String,
        phone: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("fullName", fullName)
            put("email", email)
            put("phone", phone)
            // Do NOT put username because API says it cannot be changed
        }

        val body = RequestBody.create(JSON_MEDIA_TYPE, json.toString())

        val request = Request.Builder()
            .url("$BASE_URL/users/$userId")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (response.isSuccessful) {
                    callback(true, "Profile updated")
                } else {
                    val errorMsg = try {
                        JSONObject(bodyStr ?: "").optString("message", "Update failed")
                    } catch (e: Exception) {
                        "Update failed: ${response.code}"
                    }
                    callback(false, errorMsg)
                }
            }
        })
    }

    /**
     * Register Function
     * Updates:
     * 1. Uses "fullName" key instead of "name" to match the MongoDB User Schema.
     */
    fun registerUser(fullName: String, email: String, phone: String, username: String, password: String, callback: (Boolean, String) -> Unit) {
        val json = JSONObject().apply {
            put("fullName", fullName) // Key changed to match Backend
            put("username", username)
            put("password", password)
            put("email", email)
            put("phone", phone)
        }

        val body = RequestBody.create(JSON_MEDIA_TYPE, json.toString())

        val request = Request.Builder()
            .url("$BASE_URL/register")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string()
                if (response.isSuccessful) {
                    callback(true, "Register success")
                } else {
                    val errorMsg = try {
                        JSONObject(bodyStr ?: "").optString("message", "Register failed")
                    } catch (e: Exception) {
                        "Register failed: ${response.code}"
                    }
                    callback(false, errorMsg)
                }
            }
        })
    }
}