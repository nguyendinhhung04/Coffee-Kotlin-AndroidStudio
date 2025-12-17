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
import com.example.coffeeshop.utils.CartManager
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.ui.adapter.PromotionAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import android.os.Handler
import android.os.Looper
import androidx.cardview.widget.CardView
import com.example.coffeeshop.data.repo.ItemRepository
import com.example.coffeeshop.data.dao.PointsDAO

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: UserSessionManager
    private lateinit var tvGreeting: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var ivMenu: ImageView
    private lateinit var ivNotification: ImageView

    // Best seller views
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

        // ===== Bind views chung =====
        tvGreeting = findViewById(R.id.tvGreeting)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        ivMenu = findViewById(R.id.ivMenu)
        ivNotification = findViewById(R.id.ivBell)

        tvBestSellerTitle = findViewById(R.id.tvBestSellerTitle)
        tvBestSellerSubtitle = findViewById(R.id.tvBestSellerSubtitle)
        ivBestSellerImage = findViewById(R.id.ivBestSellerImage)

        //loyalty points
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
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recAdapter
        }

        rvRecommendations.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recAdapter
        }

//        val recSnapHelper = PagerSnapHelper()
//        recSnapHelper.attachToRecyclerView(rvRecommendations)

        // ===== Promotions slider =====
        rvPromotions = findViewById(R.id.rvPromotions)
        promoAdapter = PromotionAdapter(emptyList()) { promo: Promotion ->
            // ví dụ: mở màn Order hoặc chi tiết promotion
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

        ivNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // ===== Logic khởi tạo =====
        loadUserInfo()
        setupBottomNavigation()
        askNotificationPermission()
        loadHomeContent()
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
        if (promoAdapter.itemCount > 0) {
            startPromoAutoSlide()
        }
        if (recAdapter.itemCount > 0) {
            startRecAutoSlide()
        }
    }

    private fun loadUserPoints() {
//        val session = UserSessionManager(this)
//        val userId = session.getUserId() ?: return
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
        // huỷ runnable cũ nếu có
        recAutoScrollRunnable?.let { recHandler.removeCallbacks(it) }


        recAutoScrollRunnable = object : Runnable {
            override fun run() {
                val lm = rvRecommendations.layoutManager as? LinearLayoutManager ?: return
                val itemCount = recAdapter.itemCount
                if (itemCount == 0) return

                val current = lm.findFirstCompletelyVisibleItemPosition()
                val safeCurrent = if (current == RecyclerView.NO_POSITION) 0 else current
                val next = (safeCurrent + 1) % itemCount
                Log.d("Main", "Rec auto tick: count=$itemCount current=$current next=$next")
                rvRecommendations.scrollToPosition(next)


                // DÒNG NÀY RẤT QUAN TRỌNG, nếu thiếu sẽ chỉ chạy 1 lần
                recHandler.postDelayed(this, intervalMs)
            }
        }
        recHandler.postDelayed(recAutoScrollRunnable!!, intervalMs)

    }

    private fun startPromoAutoSlide(intervalMs: Long = 3000L) {
        // huỷ cũ nếu có
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
                    Log.w("Main", "All items error: $msg")
                    // vẫn load các section khác nếu muốn
                    loadTopSellingSection()
                    loadPromotionSection()
                    return@runOnUiThread
                }

                // success & items != null
                ItemRepository.setItems(items)  // items là List<Item>, ok

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

                startRecAutoSlide()
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
                promoAdapter.update(promos)
                startPromoAutoSlide()
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
