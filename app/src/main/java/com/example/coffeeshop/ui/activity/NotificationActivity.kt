package com.example.coffeeshop.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.coffeeshop.data.dao.NotificationDAO
import com.example.coffeeshop.data.model.Notification
import com.example.coffeeshop.databinding.ActivityNotificationBinding
import com.example.coffeeshop.ui.adapter.NotificationAdapter
import com.example.coffeeshop.utils.NotificationConstants
import com.example.coffeeshop.utils.UserSessionManager
import org.json.JSONArray
import org.json.JSONObject

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var userSessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. Reset trạng thái chuông (Bell) ngay khi mở màn hình ---
        resetUnreadState()

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cài đặt Toolbar
        setSupportActionBar(binding.toolbarNotification)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Khởi tạo Adapter
        notificationAdapter = NotificationAdapter(emptyList())
        binding.recyclerViewNotifications.adapter = notificationAdapter

        userSessionManager = UserSessionManager(this)
        val userId = userSessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Gọi API lấy danh sách thông báo
        fetchNotifications(userId)
    }

    // --- HÀM RESET TRẠNG THÁI CHUÔNG & GỬI BROADCAST ---
    private fun resetUnreadState() {
        try {
            // 1. Lưu trạng thái đã đọc vào bộ nhớ máy
            val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, false).apply()

            // 2. Gửi tín hiệu sang MainActivity để tắt hiệu ứng rung
            val intent = Intent(NotificationConstants.ACTION_NEW_ORDER)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            Log.d("NotificationActivity", "State reset & Broadcast sent")
        } catch (e: Exception) {
            Log.e("NotificationActivity", "Error resetting state: ${e.message}")
        }
    }

    private fun fetchNotifications(userId: String) {
        NotificationDAO.getNotificationsByUserId(userId) { success, message, notificationsJson ->
            runOnUiThread {
                if (success && notificationsJson != null) {
                    val rawList = parseJsonToModelList(notificationsJson)

                    if (rawList.isEmpty()) {
                        binding.recyclerViewNotifications.visibility = View.GONE
                        binding.textViewNoNotifications.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewNotifications.visibility = View.VISIBLE
                        binding.textViewNoNotifications.visibility = View.GONE

                        // --- LOGIC: Tự động đánh dấu tất cả là ĐÃ ĐỌC khi hiển thị ---
                        val readList = rawList.map { it.copy(isRead = true) }
                        notificationAdapter.updateData(readList)
                    }
                } else {
                    binding.recyclerViewNotifications.visibility = View.GONE
                    binding.textViewNoNotifications.visibility = View.VISIBLE
                    // Chỉ toast nếu có thông điệp lỗi thực sự
                    if (message.isNotEmpty() && message != "No data") {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // --- HÀM PARSE JSON AN TOÀN (Hỗ trợ cả String ID và Object ID) ---
    private fun parseJsonToModelList(jsonArray: JSONArray): List<Notification> {
        val list = mutableListOf<Notification>()
        try {
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)

                // Xử lý ID: Nếu là Object {"$oid": "..."} thì lấy giá trị bên trong, nếu là String thì lấy luôn
                val rawId = jsonObject.opt("_id")
                val id = if (rawId is JSONObject) {
                    rawId.optString("\$oid")
                } else {
                    rawId.toString()
                }

                // Xử lý userId tương tự
                val rawUserId = jsonObject.opt("userId")
                val uId = if (rawUserId is JSONObject) {
                    rawUserId.optString("\$oid")
                } else {
                    rawUserId.toString()
                }

                val notification = Notification(
                    id = id,
                    userId = uId,
                    title = jsonObject.optString("title", "Thông báo"),
                    body = jsonObject.optString("body", ""),
                    isRead = jsonObject.optBoolean("isRead", false),
                    createdAt = jsonObject.optString("createdAt", "")
                )
                list.add(notification)
            }
        } catch (e: Exception) {
            Log.e("NotificationParsing", "Error parsing JSON: ${e.message}")
        }
        return list.sortedByDescending { it.createdAt }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}