package com.example.coffeeshop.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.models.FcmTokenRequest
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.ImageView
import com.example.coffeeshop.ui.activity.SettingsActivity
class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: UserSessionManager
    private lateinit var tvGreeting: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var ivMenu: ImageView

    private lateinit var ivNotification: ImageView



    // Trình khởi chạy cho yêu cầu quyền
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền đã được cấp. Bây giờ bạn có thể lưu token.
            Log.d("Permission", "POST_NOTIFICATIONS permission granted.")
            sessionManager.getUserId()?.let { saveFcmToken(it) }
        } else {
            // Giải thích cho người dùng rằng thông báo đã bị tắt.
            Log.w("Permission", "POST_NOTIFICATIONS permission denied.")
            // Bạn có thể muốn hiển thị một hộp thoại hoặc snackbar ở đây.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = UserSessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        tvGreeting = findViewById(R.id.tvGreeting)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        ivMenu = findViewById(R.id.ivMenu)   // <-- use ivMenu from XML
        ivNotification = findViewById(R.id.ivBell)

        ivMenu.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        ivNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }


        loadUserInfo()
        setupBottomNavigation()
        askNotificationPermission()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()  // đọc lại từ UserSessionManager mỗi lần quay về màn chính
    }

    private fun askNotificationPermission() {
        // Điều này chỉ cần thiết cho API cấp 33 (TIRAMISU) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // SDK FCM (và ứng dụng của bạn) có thể đăng thông báo.
                Log.d("Permission", "POST_NOTIFICATIONS permission already granted.")
                sessionManager.getUserId()?.let { saveFcmToken(it) }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Hiển thị giao diện người dùng giải thích lý do tại sao quyền là cần thiết
                // Trong ví dụ này, chúng tôi sẽ chỉ yêu cầu quyền
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Yêu cầu quyền trực tiếp
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Đối với các phiên bản cũ hơn, quyền được cấp theo mặc định
            sessionManager.getUserId()?.let { saveFcmToken(it) }
        }
    }


    private fun loadUserInfo() {
        // Lấy tên người dùng từ phiên
        val displayName = sessionManager.getDisplayName()
        tvGreeting.text = "Good day, $displayName"
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    // Đã ở trang chủ
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

    // Nếu bạn có menu đăng xuất hoặc nút đăng xuất
    fun logout() {
        sessionManager.clearSession()
        navigateToLogin()
    }
}
