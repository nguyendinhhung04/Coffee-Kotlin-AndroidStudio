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
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.dao.PromotionDAO
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Promotion
import com.example.coffeeshop.data.models.FcmTokenRequest
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
    private lateinit var layoutBell: FrameLayout // Thay đổi từ ImageView sang FrameLayout để chứa chấm đỏ
    private lateinit var ivBell: ImageView

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

    // --- 1. RECEIVER LẮNG NGHE FCM ĐỂ RUNG CHUÔNG ---
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

        // Khởi tạo phần Chuông thông báo
        layoutBell = findViewById(R.id.layoutBell)
        ivBell = findViewById(R.id.ivBell)

        tvBestSellerTitle = findViewById(R.id.tvBestSellerTitle)
        tvBestSellerSubtitle = findViewById(R.id.tvBestSellerSubtitle)
        ivBestSellerImage = findViewById(R.id.ivBestSellerImage)

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
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recAdapter
        }

        // ===== Promotions slider =====
        rvPromotions = findViewById(R.id.rvPromotions)
        promoAdapter = PromotionAdapter(emptyList()) { promo: Promotion ->
            startActivity(Intent(this, YourOrderActivity::class.java))
        }
        rvPromotions.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPromotions.adapter = promoAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvPromotions)

        // ===== Clicks top bar =====
        ivMenu.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // --- 2. LOGIC CLICK CHUÔNG: Tắt hiệu ứng, ẩn chấm đỏ và mở màn hình thông báo ---
        layoutBell.setOnClickListener {
            // 1. Lưu trạng thái đã đọc vào Prefs
            val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, false).apply()

            // 2. Cập nhật lại UI để mất chấm đỏ và dừng rung
            updateBellUI()

            // 3. Chuyển sang màn hình thông báo
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // ===== Logic khởi tạo =====
        loadUserInfo()
        setupBottomNavigation()
        askNotificationPermission()
        loadHomeContent()

        // Cập nhật trạng thái chuông khi mở app
        updateBellUI()
    }

    // --- 3. HÀM UPDATE CHUÔNG (CHẤM ĐỎ + RUNG) ---
    private fun updateBellUI() {
        val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
        val hasUnread = prefs.getBoolean(NotificationConstants.KEY_HAS_UNREAD, false)

        val viewDot = findViewById<View>(R.id.viewNotificationDot)
        // ivBell đã được khai báo lateinit ở trên

        if (hasUnread) {
            viewDot.visibility = View.VISIBLE // Hiện chấm đỏ

            // Tạo Animation rung mạnh hơn hoặc lặp lại nhiều lần hơn
            val anim = AnimationUtils.loadAnimation(this, R.anim.bell_shake)
            anim.repeatCount = 10 // Tăng số lần lặp lại để rung lâu hơn
            ivBell.startAnimation(anim)
        } else {
            viewDot.visibility = View.GONE // Ẩn chấm đỏ
            ivBell.clearAnimation()
        }
    }

    // --- 4. QUẢN LÝ BROADCAST RECEIVER ---
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

        // Cập nhật chuông khi quay lại màn hình
        updateBellUI()

        if (promoAdapter.itemCount > 0) {
            startPromoAutoSlide()
        }
        if (recAdapter.itemCount > 0) {
            startRecAutoSlide()
        }
    }

    // --- AUTO SCROLL LOGIC ---
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

    // --- DATA LOADING LOGIC ---
    private fun loadHomeContent() {
        ItemDAO.getAllItems { success, msg, items ->
            runOnUiThread {
                if (!success || items == null) {
                    loadTopSellingSection()
                    loadPromotionSection()
                    return@runOnUiThread
                }
                allItemsMap = items.associateBy { it._id as String }
                loadTopSellingSection()
                loadPromotionSection()
            }
        }
    }

    private fun loadTopSellingSection() {
        ItemDAO.getTopSellingItems { success, msg, topItems ->
            runOnUiThread {
                if (!success || topItems == null || topItems.isEmpty()) {
                    return@runOnUiThread
                }
                val enriched = topItems.map { top ->
                    allItemsMap[top._id] ?: top
                }
                val best = enriched[0]
                tvBestSellerTitle.text = best.name
                tvBestSellerSubtitle.text =
                    if (best.category.isNotBlank())
                        best.category.replaceFirstChar { it.uppercase() }
                    else
                        "Best seller"
                loadItemImage(best.image_url, ivBestSellerImage)

                val recList = enriched.take(5)
                recAdapter.update(recList)
                startRecAutoSlide()
            }
        }
    }

    private fun loadPromotionSection() {
        PromotionDAO.getActivePromotions { success, msg, promos ->
            runOnUiThread {
                if (!success || promos == null || promos.isEmpty()) {
                    return@runOnUiThread
                }
                promoAdapter.update(promos)
                startPromoAutoSlide()
            }
        }
    }

    // --- PERMISSION & USER INFO ---
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                sessionManager.getUserId()?.let { saveFcmToken(it) }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            sessionManager.getUserId()?.let { saveFcmToken(it) }
        }
    }

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

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}