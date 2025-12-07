package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.models.FcmTokenRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: UserSessionManager
    private lateinit var tvGreeting: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session manager
        sessionManager = UserSessionManager(this)

        // Kiểm tra login status
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        tvGreeting = findViewById(R.id.tvGreeting)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Load user info from session
        loadUserInfo()

        setupBottomNavigation()

        // Save FCM token
        sessionManager.getUserId()?.let { saveFcmToken(it) }
    }

    private fun loadUserInfo() {
        // Lấy tên user từ session
        val displayName = sessionManager.getDisplayName()
        tvGreeting.text = "Good day, $displayName"
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    // Already on Home
                    true
                }
                R.id.navigation_drink_menu -> {
                    startActivity(Intent(this, DrinkMenuActivity::class.java))
                    true
                }
                R.id.navigation_your_order -> {
                    startActivity(Intent(this, YourOrderActivity::class.java))
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                else -> false
            }
        }
        // Đặt mục Home được chọn mặc định
        bottomNavigationView.selectedItemId = R.id.navigation_home
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val deviceToken = task.result
            Log.d("FCM", "FCM Token: $deviceToken")

            val request = FcmTokenRequest(userId = userId, deviceToken = deviceToken)
            ApiClient.fcmApi.saveToken(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("FCM", "Token saved successfully")
                    } else {
                        Log.e("FCM", "Failed to save token: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("FCM", "Failed to save token", t)
                }
            })
        }
    }

    // Nếu bạn có menu logout hoặc nút logout
    fun logout() {
        sessionManager.clearSession()
        navigateToLogin()
    }
}
