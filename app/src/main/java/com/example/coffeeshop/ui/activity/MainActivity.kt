package com.example.coffeeshop.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Promotion
import com.example.coffeeshop.data.models.FcmTokenRequest
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.dao.PromotionDAO
import com.example.coffeeshop.ui.adapter.RecommendationAdapter
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: UserSessionManager
    private lateinit var tvGreeting: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var ivMenu: ImageView
    private lateinit var ivNotification: ImageView

    // Best seller views
    private lateinit var tvBestSellerTitle: TextView
    private lateinit var tvBestSellerSubtitle: TextView
    private lateinit var ivBestSellerImage: ImageView

    // Promotion / lemonade section
    private lateinit var tvNewLemonadeTitle: TextView
    private lateinit var ivLemonadeImage: ImageView
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var recAdapter: RecommendationAdapter

    private var allItemsMap: Map<String, Item> = emptyMap()


    // launcher xin quyền thông báo
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permission", "POST_NOTIFICATIONS permission granted.")
            sessionManager.getUserId()?.let { saveFcmToken(it) }
        } else {
            Log.w("Permission", "POST_NOTIFICATIONS permission denied.")
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

        // Bind views
        tvGreeting = findViewById(R.id.tvGreeting)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        ivMenu = findViewById(R.id.ivMenu)
        ivNotification = findViewById(R.id.ivBell)

        tvBestSellerTitle = findViewById(R.id.tvBestSellerTitle)
        tvBestSellerSubtitle = findViewById(R.id.tvBestSellerSubtitle)
        ivBestSellerImage = findViewById(R.id.ivBestSellerImage)

        tvNewLemonadeTitle = findViewById(R.id.tvNewLemonadeTitle)
        ivLemonadeImage = findViewById(R.id.ivLemonadeImage)

        rvRecommendations = findViewById(R.id.rvRecommendations)
        recAdapter = RecommendationAdapter(emptyList()) { item: Item ->
            // handle click, ví dụ: mở màn chi tiết
            startActivity(Intent(this, DrinkMenuActivity::class.java))
        }
        rvRecommendations.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recAdapter
        }

        ivMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        ivNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        loadUserInfo()
        setupBottomNavigation()
        askNotificationPermission()
        loadHomeContent()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    private fun loadHomeContent() {
        // B1: load toàn bộ items trước
        ItemDAO.getAllItems { success, msg, items ->
            runOnUiThread {
                if (!success || items == null) {
                    Log.w("Main", "All items error: $msg")
                    // vẫn gọi top-selling, nhưng sẽ không có giá
                    loadTopSellingSection()
                    loadPromotionSection()
                    return@runOnUiThread
                }
                // cache map theo _id
                allItemsMap = items.associateBy { it._id as String }
                // sau khi có map rồi mới load top-selling + promotion
                loadTopSellingSection()
                loadPromotionSection()
            }
        }
    }

    private fun loadTopSellingSection() {
        ItemDAO.getTopSellingItems { success, msg, topItems ->
            runOnUiThread {
                Log.d("Main", "topSelling success=$success size=${topItems?.size} msg=$msg")
                if (!success || topItems == null || topItems.isEmpty()) {
                    Log.w("Main", "Top selling error: $msg")
                    return@runOnUiThread
                }

                // map từng top item sang bản đầy đủ nếu có
                val enriched = topItems.map { top ->
                    allItemsMap[top._id] ?: top   // nếu không có thì dùng bản đơn giản
                }

                // best seller = item đầu
                val best = enriched[0]
                tvBestSellerTitle.text = best.name
                tvBestSellerSubtitle.text =
                    if (best.category.isNotBlank())
                        best.category.replaceFirstChar { it.uppercase() }
                    else
                        "Best seller"
                loadItemImage(best.image_url, ivBestSellerImage)

                // recommendations = top 5
                val recList = enriched.take(5)
                recAdapter.update(recList)
            }
        }
    }


    private fun loadPromotionSection() {
        PromotionDAO.getActivePromotions { success, msg, promos ->
            runOnUiThread {
                if (!success || promos == null || promos.isEmpty()) {
                    Log.w("Main", "Promotions error: $msg")
                    return@runOnUiThread
                }
                val p = promos[0]
                tvNewLemonadeTitle.text = p.name
            }
        }
    }


    // ====== Notification permission ======

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                sessionManager.getUserId()?.let { saveFcmToken(it) }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            sessionManager.getUserId()?.let { saveFcmToken(it) }
        }
    }

    // ====== UI logic ======

    private fun loadUserInfo() {
        val displayName = sessionManager.getDisplayName()
        tvGreeting.text = "Good day, $displayName"
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> true
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
        bottomNavigationView.selectedItemId = R.id.navigation_home
    }

    // ====== Call APIs for home content ======

    // ====== Helpers ======

    private fun loadItemImage(imageName: String, imageView: ImageView) {
        try {
            val inputStream = assets.open("item_img/$imageName")
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.socola)
        }
    }

    private fun formatPrice(price: Double): String {
        val nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
        return nf.format(price)
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

    fun logout() {
        sessionManager.clearSession()
        navigateToLogin()
    }
}
