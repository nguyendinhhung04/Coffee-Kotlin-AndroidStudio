package com.example.coffeeshop.ui.activity


import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.data.dao.NotificationDAO
import com.example.coffeeshop.data.model.Notification
import com.example.coffeeshop.databinding.ActivityNotificationBinding
import com.example.coffeeshop.ui.adapter.NotificationAdapter
import com.example.coffeeshop.utils.UserSessionManager // <<--- 1. Import UserSessionManager
import org.json.JSONArray
import org.json.JSONObject

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var userSessionManager: UserSessionManager // <<--- 2. Khai báo biến cho session manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cài đặt Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Khởi tạo Adapter với danh sách rỗng
        notificationAdapter = NotificationAdapter(emptyList())
        binding.recyclerViewNotifications.adapter = notificationAdapter

        // <<--- 3. Khởi tạo UserSessionManager
        userSessionManager = UserSessionManager(this)

        // <<--- 4. Lấy userId từ UserSessionManager
        val userId = userSessionManager.getUserId()

        // Kiểm tra xem người dùng đã đăng nhập và có userId hay chưa
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
            finish() // Đóng activity nếu không có user id
            return
        }

        // Gọi API để lấy danh sách thông báo
        fetchNotifications(userId)
    }

    private fun fetchNotifications(userId: String) {
        NotificationDAO.getNotificationsByUserId(userId) { success, message, notificationsJson ->
            runOnUiThread {
                if (success && notificationsJson != null) {
                    val notificationList = parseJsonToModelList(notificationsJson)
                    if (notificationList.isEmpty()) {
                        binding.recyclerViewNotifications.visibility = View.GONE
                        binding.textViewNoNotifications.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewNotifications.visibility = View.VISIBLE
                        binding.textViewNoNotifications.visibility = View.GONE
                        notificationAdapter.updateData(notificationList)
                    }
                } else {
                    binding.recyclerViewNotifications.visibility = View.GONE
                    binding.textViewNoNotifications.visibility = View.VISIBLE
                    binding.textViewNoNotifications.text = "Lỗi: $message"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseJsonToModelList(jsonArray: JSONArray): List<Notification> {
        val list = mutableListOf<Notification>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val notification = Notification(
                id = jsonObject.getString("_id"),
                userId = jsonObject.getString("userId"),
                title = jsonObject.getString("title"),
                body = jsonObject.getString("body"),
                isRead = jsonObject.getBoolean("isRead"),
                createdAt = jsonObject.getString("createdAt")
            )
            list.add(notification)
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