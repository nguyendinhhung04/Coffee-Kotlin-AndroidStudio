package com.example.coffeeshop.utils

import android.content.Context
import android.content.SharedPreferences

class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

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

    // Save session
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

    fun isLoggedIn(): Boolean =
        prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): String? =
        prefs.getString(KEY_USER_ID, null)

    fun getUsername(): String? =
        prefs.getString(KEY_USERNAME, null)

    fun getFullName(): String? =
        prefs.getString(KEY_FULL_NAME, null)

    fun getEmail(): String? =
        prefs.getString(KEY_EMAIL, null)

    fun getPhone(): String? =
        prefs.getString(KEY_PHONE, null)

    fun getToken(): String? =
        prefs.getString(KEY_TOKEN, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getDisplayName(): String {
        val fullName = getFullName()
        val username = getUsername()
        return when {
            !fullName.isNullOrEmpty() -> fullName
            !username.isNullOrEmpty() -> username
            else -> "User"
        }
    }

    // NEW: setters for EditProfileActivity

    fun setFullName(fullName: String) {
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply()
    }

    fun setEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun setPhone(phone: String) {
        prefs.edit().putString(KEY_PHONE, phone).apply()
    }
}
