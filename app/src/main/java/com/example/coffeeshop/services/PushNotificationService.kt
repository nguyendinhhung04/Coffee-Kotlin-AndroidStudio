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
        // Gửi token về server nếu cần thiết để quản lý thiết bị
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "Order Status Update"
        val body = message.notification?.body ?: "Your order is being processed."

        // 1. Lưu trạng thái "Chưa đọc" vào bộ nhớ máy để hiện chấm đỏ và rung chuông ở MainActivity
        val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, true).apply()

        // 2. Gửi tín hiệu nội bộ sang MainActivity để kích hoạt hiệu ứng rung lắc (Shake) và chấm đỏ ngay lập tức
        val intent = Intent(NotificationConstants.ACTION_NEW_ORDER)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // 3. Hiển thị thông báo trên thanh trạng thái (System Notification) với chế độ rung dài
        showNotification(title, body)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(title: String, body: String) {
        val channelId = "coffeeshop_channel"

        // Mẫu rung mạnh: Chờ 0s -> Rung 1s -> Nghỉ 0.5s -> Rung 1s
        val longVibrationPattern = longArrayOf(0, 1000, 500, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Coffeeshop Notifications",
                NotificationManager.IMPORTANCE_HIGH // Đảm bảo hiện popup và âm thanh
            ).apply {
                description = "Thông báo đơn hàng và khuyến mãi"
                enableVibration(true)
                vibrationPattern = longVibrationPattern // Áp dụng mẫu rung lâu hơn cho Android 8.0+
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Icon thông báo (nên dùng dạng silhouette trắng)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Mức ưu tiên tối đa để hiện Heads-up notification
            .setVibrate(longVibrationPattern)            // Rung cho các thiết bị đời cũ (dưới Android 8)
            .setDefaults(NotificationCompat.DEFAULT_SOUND) // Sử dụng âm thanh mặc định của hệ thống
            .setAutoCancel(true)                           // Tự động biến mất khi người dùng nhấn vào

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}