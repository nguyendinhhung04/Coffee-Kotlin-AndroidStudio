package com.example.coffeeshop.utils

object NotificationConstants {
    // Tên file SharedPreferences để lưu trạng thái thông báo
    const val PREF_NAME = "notification_pref"

    // Key lưu trạng thái có thông báo chưa đọc hay không (Boolean)
    const val KEY_HAS_UNREAD = "has_unread_notification"

    // Action ID để gửi và nhận tín hiệu Broadcast nội bộ trong App
    const val ACTION_NEW_ORDER = "com.example.coffeeshop.ACTION_NEW_ORDER"
}