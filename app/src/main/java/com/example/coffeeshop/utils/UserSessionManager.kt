package com.example.coffeeshop.utils

import android.content.Context
import android.content.SharedPreferences

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val PREFS_NAME = "CoffeeShopPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
    }

    fun saveUserSession(userId: String, username: String, role: String, fullName: String? = null, email: String? = null, phone: String? = null) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_USER_ROLE, role)
        fullName?.let { editor.putString(KEY_FULL_NAME, it) }
        email?.let { editor.putString(KEY_EMAIL, it) }
        phone?.let { editor.putString(KEY_PHONE, it) }
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }

    fun isAdmin(): Boolean {
        return getUserRole() == "admin"
    }

    fun getFullName(): String? {
        return prefs.getString(KEY_FULL_NAME, null)
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getPhone(): String? {
        return prefs.getString(KEY_PHONE, null)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}

