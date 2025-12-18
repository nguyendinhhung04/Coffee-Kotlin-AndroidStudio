package com.example.coffeeshop.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.coffeeshop.R
import com.example.coffeeshop.utils.NotificationConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // Token này thường được xử lý lưu ở MainActivity qua FirebaseMessaging.getInstance().token
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Thông báo mới"
        val body = message.notification?.body ?: "Bạn có tin nhắn mới từ Coffee Shop"

        // --- BƯỚC 1: LƯU TRẠNG THÁI CHƯA ĐỌC VÀO PREFS ---
        // Việc này giúp khi người dùng mở App lên, chuông sẽ rung ngay lập tức
        val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, true).apply()

        // --- BƯỚC 2: GỬI BROADCAST ĐỂ RUNG CHUÔNG REAL-TIME ---
        // Nếu người dùng đang mở App, lệnh này sẽ kích hoạt hàm updateBellUI() ở các Activity
        val intent = Intent(NotificationConstants.ACTION_NEW_ORDER)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // --- BƯỚC 3: HIỂN THỊ THÔNG BÁO HỆ THỐNG ---
        showNotification(title, body)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, body: String) {
        val channelId = "coffeeshop_channel"

        // Mẫu rung: [Nghỉ, Rung, Nghỉ, Rung] (đơn vị milis)
        val longVibrationPattern = longArrayOf(0, 1000, 500, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Coffeeshop Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo đơn hàng"
                enableVibration(true)
                vibrationPattern = longVibrationPattern
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Đảm bảo icon này là ảnh trắng nền trong suốt
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setVibrate(longVibrationPattern)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Đẩy lên MAX để hiện Popup trên đầu màn hình
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}