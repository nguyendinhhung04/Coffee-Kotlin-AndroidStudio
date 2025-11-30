package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import com.example.coffeeshop.utils.UserSessionManager

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

    // Nếu bạn có menu logout hoặc nút logout
    fun logout() {
        sessionManager.clearSession()
        navigateToLogin()
    }
}