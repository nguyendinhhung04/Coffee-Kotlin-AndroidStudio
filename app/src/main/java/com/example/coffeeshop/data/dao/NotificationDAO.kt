package com.example.coffeeshop.data.dao

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * DAO (Data Access Object) để xử lý các hoạt động liên quan đến thông báo (Notifications).
 */
object NotificationDAO {

    // Sử dụng lại cấu trúc client tương tự UserDAO, tăng thời gian chờ để ổn định hơn
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Cùng BASE_URL với các DAO khác
    private const val BASE_URL = "https://coffeeshop-mobileappproject-backend.onrender.com"

    /**
     * Lấy tất cả thông báo của một người dùng dựa trên userId.
     *
     * @param userId ID của người dùng.
     * @param callback Hàm callback trả về:
     * - success: Boolean - Trạng thái thành công/thất bại của request.
     * - message: String - Thông báo lỗi hoặc thành công.
     * - notifications: JSONArray? - Mảng JSON chứa danh sách các thông báo, hoặc null nếu có lỗi.
     */
    fun getNotificationsByUserId(
        userId: String,
        callback: (success: Boolean, message: String, notifications: JSONArray?) -> Unit
    ) {
        // Xây dựng URL với endpoint /fcm/{userId}
        val request = Request.Builder()
            .url("$BASE_URL/fcm/user/$userId")
            .get() // Sử dụng phương thức GET
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Xử lý khi có lỗi mạng (không kết nối được, timeout,...)
                callback(false, "Lỗi mạng: ${e.message}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    try {
                        // Backend trả về một mảng JSON, nên parse bằng JSONArray
                        val jsonArray = JSONArray(responseBody)
                        callback(true, "Lấy thông báo thành công", jsonArray)
                    } catch (e: Exception) {
                        // Xử lý khi response không phải là JSON hợp lệ
                        callback(false, "Lỗi phân tích dữ liệu: ${e.message}", null)
                    }
                } else {
                    // Xử lý khi server trả về lỗi (404 Not Found, 500 Internal Server Error,...)
                    val errorMessage = "Không thể lấy thông báo. Mã lỗi: ${response.code}"
                    callback(false, errorMessage, null)
                }
            }
        })
    }
}