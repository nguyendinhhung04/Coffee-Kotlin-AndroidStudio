package com.example.coffeeshop.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * ============================================================================
 * Constants - Các hằng số dùng chung
 * ============================================================================
 */
object Constants {
    // SharedPreferences keys
    const val PREF_NAME = "CoffeeShopPrefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_TOKEN = "token"
    const val KEY_CURRENT_REWARD_CODE = "current_reward_code"
    const val KEY_CURRENT_REWARD_NAME = "current_reward_name"
    const val KEY_CURRENT_REWARD_DISCOUNT = "current_reward_discount"
    const val KEY_REWARD_EXPIRY = "reward_expiry"
    const val KEY_SAVED_ADDRESS = "saved_delivery_address"

    // API Base URL
    const val BASE_API_URL = "https://c76lgf-3000.csb.app"

    // Order Status
    const val ORDER_STATUS_PENDING = "Pending"
    const val ORDER_STATUS_CONFIRMED = "Confirmed"
    const val ORDER_STATUS_DELIVERING = "Delivering"
    const val ORDER_STATUS_DELIVERED = "Delivered"
    const val ORDER_STATUS_CANCELLED = "Cancelled"

    // Payment Methods
    const val PAYMENT_METHOD_COD = "COD"
    const val PAYMENT_METHOD_TRANSFER = "Transfer"
    const val PAYMENT_METHOD_CARD = "Card"

    // Default values
    const val DEFAULT_SHIPPING_FEE = 15000.0
    const val REWARD_EXPIRY_DAYS = 7L

    // Item Categories
    const val CATEGORY_COFFEE = "Coffee"
    const val CATEGORY_TEA = "Tea"
    const val CATEGORY_CAKE = "Cake"
    const val CATEGORY_SMOOTHIE = "Smoothie"
    const val CATEGORY_MILK_TEA = "Milk Tea"
}

/**
 * ============================================================================
 * SharedPreferencesHelper - Quản lý SharedPreferences
 * ============================================================================
 */
object SharedPreferencesHelper {
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    }

    // User
    fun setUserId(userId: String) = preferences.edit().putString(Constants.KEY_USER_ID, userId).apply()
    fun getUserId(): String? = preferences.getString(Constants.KEY_USER_ID, null)

    fun setUsername(username: String) = preferences.edit().putString(Constants.KEY_USERNAME, username).apply()
    fun getUsername(): String? = preferences.getString(Constants.KEY_USERNAME, null)

    fun setToken(token: String) = preferences.edit().putString(Constants.KEY_TOKEN, token).apply()
    fun getToken(): String? = preferences.getString(Constants.KEY_TOKEN, null)

    // Reward
    fun setReward(code: String, name: String, discount: Int, expiryTime: Long) {
        preferences.edit().apply {
            putString(Constants.KEY_CURRENT_REWARD_CODE, code)
            putString(Constants.KEY_CURRENT_REWARD_NAME, name)
            putInt(Constants.KEY_CURRENT_REWARD_DISCOUNT, discount)
            putLong(Constants.KEY_REWARD_EXPIRY, expiryTime)
            apply()
        }
    }

    fun getRewardCode(): String? = preferences.getString(Constants.KEY_CURRENT_REWARD_CODE, null)
    fun getRewardName(): String? = preferences.getString(Constants.KEY_CURRENT_REWARD_NAME, null)
    fun getRewardDiscount(): Int = preferences.getInt(Constants.KEY_CURRENT_REWARD_DISCOUNT, 0)
    fun getRewardExpiry(): Long = preferences.getLong(Constants.KEY_REWARD_EXPIRY, 0)

    fun isRewardValid(): Boolean {
        return getRewardCode() != null && System.currentTimeMillis() < getRewardExpiry()
    }

    fun clearReward() {
        preferences.edit().apply {
            remove(Constants.KEY_CURRENT_REWARD_CODE)
            remove(Constants.KEY_CURRENT_REWARD_NAME)
            remove(Constants.KEY_CURRENT_REWARD_DISCOUNT)
            remove(Constants.KEY_REWARD_EXPIRY)
            apply()
        }
    }

    // Address
    fun setAddress(address: String) = preferences.edit().putString(Constants.KEY_SAVED_ADDRESS, address).apply()
    fun getAddress(): String? = preferences.getString(Constants.KEY_SAVED_ADDRESS, null)

    // Logout
    fun clearAll() {
        preferences.edit().clear().apply()
    }
}

/**
 * ============================================================================
 * DateHelper - Hỗ trợ định dạng ngày tháng
 * ============================================================================
 */
object DateHelper {
    private val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("vi"))
    private val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
    private val sdfTime = SimpleDateFormat("HH:mm", Locale("vi"))
    private val sdfShort = SimpleDateFormat("dd/MM HH:mm", Locale("vi"))

    /**
     * Chuyển timestamp thành string định dạng đầy đủ
     */
    fun formatFullDateTime(timestamp: Long): String {
        return sdfFull.format(Date(timestamp))
    }

    /**
     * Chuyển timestamp thành string ngày tháng năm
     */
    fun formatDate(timestamp: Long): String {
        return sdfDate.format(Date(timestamp))
    }

    /**
     * Chuyển timestamp thành string giờ phút
     */
    fun formatTime(timestamp: Long): String {
        return sdfTime.format(Date(timestamp))
    }

    /**
     * Chuyển timestamp thành string ngắn (dd/MM HH:mm)
     */
    fun formatShort(timestamp: Long): String {
        return sdfShort.format(Date(timestamp))
    }

    /**
     * Lấy ngày hôm nay
     */
    fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Kiểm tra xem có phải hôm nay không
     */
    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestamp }

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
    }
}

/**
 * ============================================================================
 * CurrencyHelper - Hỗ trợ định dạng tiền tệ
 * ============================================================================
 */
object CurrencyHelper {
    /**
     * Format số tiền theo VND
     */
    fun formatVND(amount: Double): String {
        val formatter = java.text.DecimalFormat("#,##0")
        return "${formatter.format(amount)} VND"
    }

    /**
     * Format số tiền không có đơn vị
     */
    fun formatAmount(amount: Double): String {
        val formatter = java.text.DecimalFormat("#,##0")
        return formatter.format(amount)
    }

    /**
     * Parse string thành Double
     */
    fun parseAmount(str: String): Double {
        return str.replace(",", "").toDoubleOrNull() ?: 0.0
    }
}

/**
 * ============================================================================
 * ValidationHelper - Hỗ trợ validate dữ liệu
 * ============================================================================
 */
object ValidationHelper {
    /**
     * Kiểm tra email hợp lệ
     */
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailPattern.toRegex())
    }

    /**
     * Kiểm tra phone hợp lệ
     */
    fun isValidPhone(phone: String): Boolean {
        return phone.matches("^[0-9]{10}$".toRegex())
    }

    /**
     * Kiểm tra username hợp lệ
     */
    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.length <= 20 &&
                username.matches("^[a-zA-Z0-9_-]+$".toRegex())
    }

    /**
     * Kiểm tra password mạnh
     */
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isDigit() }
    }

    /**
     * Kiểm tra không được trống
     */
    fun isEmpty(text: String): Boolean {
        return text.trim().isEmpty()
    }
}

/**
 * ============================================================================
 * ViewHelper - Hỗ trợ tính toán View
 * ============================================================================
 */
object ViewHelper {
    /**
     * Chuyển DP sang Pixel
     */
    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    /**
     * Chuyển Pixel sang DP
     */
    fun pxToDp(context: Context, px: Int): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    /**
     * Lấy chiều rộng màn hình
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * Lấy chiều cao màn hình
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }
}