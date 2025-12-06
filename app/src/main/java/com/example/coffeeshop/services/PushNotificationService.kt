package com.example.coffeeshop.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService() : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token mới: $token")

        // Gửi token lên backend
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        // Call API ExpressJS (POST /save-token)
    }
}