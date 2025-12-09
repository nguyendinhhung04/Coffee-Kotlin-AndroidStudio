package com.example.coffeeshop.data.model

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val isRead: Boolean, // <-- THÊM TRƯỜNG MỚI
    val createdAt: String // Giữ dạng String "2025-12-09T07:05:43.210Z"
) {
    // (Tùy chọn) Hàm tiện ích để định dạng lại ngày tháng cho dễ đọc
    fun getFormattedCreatedAt(): String {
        // Định dạng đầu vào từ ISO 8601
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")

        // Định dạng đầu ra mong muốn
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        return try {
            val date = parser.parse(createdAt)
            formatter.format(date)
        } catch (e: Exception) {
            createdAt // Trả về chuỗi gốc nếu có lỗi parse
        }
    }
}