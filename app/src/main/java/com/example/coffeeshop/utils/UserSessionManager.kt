package com.example.coffeeshop.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

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
        private const val KEY_LOYALTY_POINTS = "loyaltyPoints"
        private const val KEY_PASSWORD = "password"

        private const val KEY_ADDRESS_STREET = "street"
        private const val KEY_ADDRESS_WARD = "ward"
        private const val KEY_ADDRESS_DISTRICT = "district"
        private const val KEY_ADDRESS_CITY = "city"
        private const val KEY_ROLE = "role"
        private const val KEY_DEFAULT_ADDRESS = "defaultAddress"
    }

    // Save session (gọi sau login)
    fun saveUserSession(
        userId: String,
        username: String,
        fullName: String,
        password: String,
        email: String?,
        phone: String?,
        role: String?,
        street: String?,
        ward: String?,
        district: String?,
        city: String?,
        token: String?
    ) {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)

            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_PASSWORD, password)

            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone)
            putString(KEY_ROLE, role)

            // Address
            putString(KEY_ADDRESS_STREET, street)
            putString(KEY_ADDRESS_WARD, ward)
            putString(KEY_ADDRESS_DISTRICT, district)
            putString(KEY_ADDRESS_CITY, city)

            putString(KEY_TOKEN, token)
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

    fun getRole(): String? =
        prefs.getString(KEY_ROLE, null)

    // Default address flag
    fun isDefaultAddress(): Boolean =
        prefs.getBoolean(KEY_DEFAULT_ADDRESS, false)

    fun setDefaultAddress(isDefault: Boolean) {
        prefs.edit { putBoolean(KEY_DEFAULT_ADDRESS, isDefault) }
    }

    // Address getters
    fun getStreet(): String? =
        prefs.getString(KEY_ADDRESS_STREET, null)

    fun getWard(): String? =
        prefs.getString(KEY_ADDRESS_WARD, null)

    fun getDistrict(): String? =
        prefs.getString(KEY_ADDRESS_DISTRICT, null)

    fun getCity(): String? =
        prefs.getString(KEY_ADDRESS_CITY, null)

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

    // Setters cho EditProfileActivity

    fun setFullName(fullName: String) {
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply()
    }

    fun setEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun setPhone(phone: String) {
        prefs.edit().putString(KEY_PHONE, phone).apply()
    }

    fun getPassword(): String? =
        prefs.getString(KEY_PASSWORD, null)

    fun setPassword(newPassword: String) {
        prefs.edit().putString(KEY_PASSWORD, newPassword).apply()
    }

    fun setRole(role: String?) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    // Address setters
    fun setStreet(street: String?) {
        prefs.edit().putString(KEY_ADDRESS_STREET, street).apply()
    }

    fun setWard(ward: String?) {
        prefs.edit().putString(KEY_ADDRESS_WARD, ward).apply()
    }

    fun setDistrict(district: String?) {
        prefs.edit().putString(KEY_ADDRESS_DISTRICT, district).apply()
    }

    fun setCity(city: String?) {
        prefs.edit().putString(KEY_ADDRESS_CITY, city).apply()
    }

    // Loyalty points
    fun saveLoyaltyPoints(points: Int) {
        prefs.edit().putInt(KEY_LOYALTY_POINTS, points).apply()
    }

    fun getLoyaltyPoints(): Int {
        return prefs.getInt(KEY_LOYALTY_POINTS, 0)
    }
}
