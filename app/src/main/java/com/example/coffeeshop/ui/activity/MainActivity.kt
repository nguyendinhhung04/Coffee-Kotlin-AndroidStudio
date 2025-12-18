package com.example.coffeeshop.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.dao.PointsDAO
import com.example.coffeeshop.data.dao.PromotionDAO
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Promotion
import com.example.coffeeshop.data.models.FcmTokenRequest
import com.example.coffeeshop.data.repo.ItemRepository
import com.example.coffeeshop.ui.adapter.PromotionAdapter
import com.example.coffeeshop.ui.adapter.RecommendationAdapter
import com.example.coffeeshop.utils.CartManager
import com.example.coffeeshop.utils.NotificationConstants
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: UserSessionManager
    private lateinit var tvGreeting: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var ivMenu: ImageView
    private lateinit var layoutBell: FrameLayout // FrameLayout bao ngoài chuông
    private lateinit var ivNotification: ImageView
    private lateinit var viewNotificationDot: View // Chấm đỏ

    // Best seller views
    private lateinit var tvBestSellerTitle: TextView
    private lateinit var tvBestSellerSubtitle: TextView
    private lateinit var ivBestSellerImage: ImageView

    // Recommendations + promotions
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var recAdapter: RecommendationAdapter
    private var allItemsMap: Map<String, Item> = emptyMap()

    private lateinit var rvPromotions: RecyclerView
    private lateinit var promoAdapter: PromotionAdapter

    private val promoHandler = Handler(Looper.getMainLooper())
    private var promoAutoScrollRunnable: Runnable? = null
    private val recHandler = Handler(Looper.getMainLooper())
    private var recAutoScrollRunnable: Runnable? = null

    private lateinit var cardPoints: CardView
    private lateinit var tvPointsValue: TextView

    // --- 1. RECEIVER LẮNG NGHE THÔNG BÁO MỚI ---
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBellUI()
        }
    }

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

        // ===== Bind views chung =====
        tvGreeting = findViewById(R.id.tvGreeting)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        ivMenu = findViewById(R.id.ivMenu)

        // Khởi tạo phần Chuông thông báo mới
        layoutBell = findViewById(R.id.layoutBell)
        ivNotification = findViewById(R.id.ivBell)
        viewNotificationDot = findViewById(R.id.viewNotificationDot)

        tvBestSellerTitle = findViewById(R.id.tvBestSellerTitle)
        tvBestSellerSubtitle = findViewById(R.id.tvBestSellerSubtitle)
        ivBestSellerImage = findViewById(R.id.ivBestSellerImage)

        // loyalty points
        cardPoints = findViewById(R.id.cardPoints)
        tvPointsValue = findViewById(R.id.tvPointsValue)

        loadUserPoints()

        cardPoints.setOnClickListener {
            val intent = Intent(this, DrinkMenuActivity::class.java)
            startActivity(intent)
        }

        // ===== Recommendations =====
        rvRecommendations = findViewById(R.id.rvRecommendations)
        recAdapter = RecommendationAdapter(emptyList()) { item: Item ->
            val price = if (item.discountedPrice > 0) item.discountedPrice else item.basePrice
            val cartItem = CartItem(
                item = item,
                quantity = 1,
                customizations = emptyMap(),
                price = price
            )
            CartManager.addItem(cartItem)

            val intent = Intent(this, DrinkMenuActivity::class.java)
            intent.putExtra("open_from_recommendation", true)
            startActivity(intent)
        }
        rvRecommendations.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = recAdapter
        }

        // ===== Promotions slider =====
        rvPromotions = findViewById(R.id.rvPromotions)
        promoAdapter = PromotionAdapter(emptyList()) { promo: Promotion ->
            startActivity(Intent(this, YourOrderActivity::class.java))
        }
        rvPromotions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPromotions.adapter = promoAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvPromotions)

        // ===== Clicks top bar =====
        ivMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // --- 2. CLICK CHUÔNG: TẮT HIỆU ỨNG VÀ MỞ MÀN HÌNH ---
        layoutBell.setOnClickListener {
            // Reset trạng thái chưa đọc
            val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, false).apply()

            updateBellUI() // Cập nhật ngay UI (ẩn chấm đỏ, tắt rung)

            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // ===== Logic khởi tạo =====
        loadUserInfo()
        setupBottomNavigation()
        askNotificationPermission()
        loadHomeContent()
    }

    // --- 3. HÀM CẬP NHẬT GIAO DIỆN CHUÔNG (CHẤM ĐỎ + RUNG) ---
    private fun updateBellUI() {
        val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
        val hasUnread = prefs.getBoolean(NotificationConstants.KEY_HAS_UNREAD, false)

        if (hasUnread) {
            viewNotificationDot.visibility = View.VISIBLE
            val anim = AnimationUtils.loadAnimation(this, R.anim.bell_shake)
            anim.repeatCount = 10
            ivNotification.startAnimation(anim)
        } else {
            viewNotificationDot.visibility = View.GONE
            ivNotification.clearAnimation()
        }
    }

    // --- 4. ĐĂNG KÝ/HỦY RECEIVER ---
    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            notificationReceiver,
            IntentFilter(NotificationConstants.ACTION_NEW_ORDER)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
    }

    override fun onPause() {
        super.onPause()
        promoAutoScrollRunnable?.let { promoHandler.removeCallbacks(it) }
        recAutoScrollRunnable?.let { recHandler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        loadUserPoints()
        updateBellUI() // Kiểm tra trạng thái chuông mỗi khi quay lại màn hình

        if (promoAdapter.itemCount > 0) startPromoAutoSlide()
        if (recAdapter.itemCount > 0) startRecAutoSlide()
    }

    private fun loadUserPoints() {
        val userId = sessionManager.getUserId() ?: return
        PointsDAO.getUserPoints(userId) { success, message, points ->
            runOnUiThread {
                if (success && points != null) {
                    tvPointsValue.text = "$points điểm"
                    sessionManager.saveLoyaltyPoints(points)
                } else {
                    tvPointsValue.text = "0 điểm"
                    sessionManager.saveLoyaltyPoints(0)
                }
            }
        }
    }

    private fun startRecAutoSlide(intervalMs: Long = 4000L) {
        recAutoScrollRunnable?.let { recHandler.removeCallbacks(it) }
        recAutoScrollRunnable = object : Runnable {
            override fun run() {
                val lm = rvRecommendations.layoutManager as? LinearLayoutManager ?: return
                val itemCount = recAdapter.itemCount
                if (itemCount == 0) return
                val current = lm.findFirstCompletelyVisibleItemPosition()
                val safeCurrent = if (current == RecyclerView.NO_POSITION) 0 else current
                val next = (safeCurrent + 1) % itemCount
                rvRecommendations.scrollToPosition(next)
                recHandler.postDelayed(this, intervalMs)
            }
        }
        recHandler.postDelayed(recAutoScrollRunnable!!, intervalMs)
    }

    private fun startPromoAutoSlide(intervalMs: Long = 3000L) {
        promoAutoScrollRunnable?.let { promoHandler.removeCallbacks(it) }
        promoAutoScrollRunnable = object : Runnable {
            override fun run() {
                val lm = rvPromotions.layoutManager as? LinearLayoutManager ?: return
                val itemCount = promoAdapter.itemCount
                if (itemCount == 0) return
                val current = lm.findFirstVisibleItemPosition()
                val next = (current + 1) % itemCount
                rvPromotions.smoothScrollToPosition(next)
                promoHandler.postDelayed(this, intervalMs)
            }
        }
        promoHandler.postDelayed(promoAutoScrollRunnable!!, intervalMs)
    }

    private fun loadHomeContent() {
        ItemDAO.getAllItems { success, msg, items ->
            runOnUiThread {
                if (!success || items == null) {
                    loadTopSellingSection()
                    loadPromotionSection()
                    return@runOnUiThread
                }
                ItemRepository.setItems(items)
                allItemsMap = items.associateBy { it._id as String }
                loadTopSellingSection()
                loadPromotionSection()
            }
        }
    }

    private fun loadTopSellingSection() {
        ItemDAO.getTopSellingItems { success, msg, topItems ->
            runOnUiThread {
                if (!success || topItems.isNullOrEmpty()) return@runOnUiThread
                val enriched = topItems.map { allItemsMap[it._id] ?: it }
                val best = enriched[0]
                tvBestSellerTitle.text = best.name
                tvBestSellerSubtitle.text = if (best.category.isNotBlank()) best.category.replaceFirstChar { it.uppercase() } else "Best seller"
                loadItemImage(best.image_url, ivBestSellerImage)
                recAdapter.update(enriched.take(5))
                startRecAutoSlide()
            }
        }
    }

    private fun loadPromotionSection() {
        PromotionDAO.getActivePromotions { success, msg, promos ->
            runOnUiThread {
                if (success && !promos.isNullOrEmpty()) {
                    promoAdapter.update(promos)
                    startPromoAutoSlide()
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                sessionManager.getUserId()?.let { saveFcmToken(it) }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            sessionManager.getUserId()?.let { saveFcmToken(it) }
        }
    }

    private fun loadUserInfo() {
        tvGreeting.text = "Good day, ${sessionManager.getDisplayName()}"
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_drink_menu -> {
                    startActivity(Intent(this, DrinkMenuActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_your_order -> {
                    startActivity(Intent(this, YourOrderActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.navigation_home
    }

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

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val deviceToken = task.result
            val request = FcmTokenRequest(userId = userId, deviceToken = deviceToken)
            ApiClient.fcmApi.saveToken(request).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
        }
    }

    fun logout() {
        sessionManager.clearSession()
        navigateToLogin()
    }
}