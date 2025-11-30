package com.example.coffeeshop.utils

import android.content.Context
import android.content.SharedPreferences

class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "CoffeeShopUserSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_TOKEN = "token"
    }

    /**
     * Lưu thông tin user sau khi đăng nhập thành công
     */
    fun saveUserSession(
        userId: String,
        username: String,
        fullName: String?,
        email: String?,
        phone: String?,
        token: String?
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone)
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    /**
     * Kiểm tra user đã đăng nhập chưa
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Lấy User ID
     */
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    /**
     * Lấy Username
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Lấy Full Name
     */
    fun getFullName(): String? {
        return prefs.getString(KEY_FULL_NAME, null)
    }

    /**
     * Lấy Email
     */
    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    /**
     * Lấy Phone
     */
    fun getPhone(): String? {
        return prefs.getString(KEY_PHONE, null)
    }

    /**
     * Lấy Token
     */
    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Xóa session khi logout
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Lấy display name (ưu tiên fullName, fallback về username)
     */
    fun getDisplayName(): String {
        val fullName = getFullName()
        val username = getUsername()
        return when {
            !fullName.isNullOrEmpty() -> fullName
            !username.isNullOrEmpty() -> username
            else -> "User"
        }
    }
}