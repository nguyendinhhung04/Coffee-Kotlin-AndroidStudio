package com.example.coffeeshop.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.OrderDAO
import com.example.coffeeshop.data.model.Order
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.OrderItem
import com.example.coffeeshop.data.repo.ItemRepository
import com.example.coffeeshop.utils.CartManager
import com.example.coffeeshop.utils.NotificationConstants
import com.example.coffeeshop.utils.UserSessionManager
import com.example.coffeeshop.ui.adapter.OrderAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class YourOrderActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnRecently: Button
    private lateinit var btnPastOrders: Button
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmptyOrders: TextView
    private lateinit var ivMenu: ImageView

    // Notification components
    private lateinit var layoutBell: FrameLayout
    private lateinit var ivNotification: ImageView
    private lateinit var viewNotificationDot: View

    private lateinit var orderAdapter: OrderAdapter
    private var allOrders: List<Order> = emptyList()
    private var showingRecently: Boolean = true

    // --- 1. RECEIVER LẮNG NGHE THÔNG BÁO ĐỂ CẬP NHẬT CHUÔNG ---
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBellUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_order)

        initViews()
        setupBottomNavigationView()
        setupRecyclerView()
        setupOrderFilterButtons()

        // ===== Clicks top bar =====
        ivMenu.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Click chuông: Tắt hiệu ứng và mở màn hình thông báo
        layoutBell.setOnClickListener {
            val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, false).apply()

            updateBellUI() // Cập nhật UI ngay lập tức

            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        // Mặc định: Recently
        btnRecently.performClick()
        loadOrdersFromServer()
    }

    private fun initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_your_order)
        btnRecently = findViewById(R.id.btnRecently)
        btnPastOrders = findViewById(R.id.btnPastOrders)
        rvOrders = findViewById(R.id.rvOrders)
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders)
        ivMenu = findViewById(R.id.ivYourOrdersMenu)

        // Bind notification views
        layoutBell = findViewById(R.id.layoutBell)
        ivNotification = findViewById(R.id.ivBell)
        viewNotificationDot = findViewById(R.id.viewNotificationDot)
    }

    // --- 2. HÀM CẬP NHẬT GIAO DIỆN CHUÔNG ---
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

    override fun onResume() {
        super.onResume()
        updateBellUI() // Kiểm tra trạng thái chuông khi quay lại màn hình
        loadOrdersFromServer()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            emptyList(),
            onOrderClick = { order ->
                val intent = Intent(this, OrderDetailActivity::class.java)
                intent.putExtra("order_id", order._id ?: "")
                startActivity(intent)
            },
            onReorderClick = { order ->
                handleReorder(order)
            }
        )

        rvOrders.apply {
            layoutManager = LinearLayoutManager(this@YourOrderActivity)
            adapter = orderAdapter
        }
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_your_order
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_drink_menu -> {
                    startActivity(Intent(this, DrinkMenuActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.navigation_your_order -> true
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupOrderFilterButtons() {
        btnRecently.setOnClickListener {
            showingRecently = true
            selectOrderFilterButton(btnRecently)
            applyFilterAndShow()
        }
        btnPastOrders.setOnClickListener {
            showingRecently = false
            selectOrderFilterButton(btnPastOrders)
            applyFilterAndShow()
        }
    }

    private fun selectOrderFilterButton(selectedButton: Button) {
        btnRecently.setBackgroundResource(R.color.backgroundLight)
        btnRecently.setTextColor(resources.getColor(R.color.dark_brown, null))
        btnPastOrders.setBackgroundResource(R.color.backgroundLight)
        btnPastOrders.setTextColor(resources.getColor(R.color.dark_brown, null))

        selectedButton.setBackgroundResource(R.color.brown)
        selectedButton.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun loadOrdersFromServer() {
        val session = UserSessionManager(this)
        val userId = session.getUserId()
        if (userId.isNullOrBlank()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            showOrders(emptyList())
            return
        }

        OrderDAO.getOrdersByUserId(userId) { success, message, orders ->
            runOnUiThread {
                if (success && orders != null) {
                    allOrders = orders
                    applyFilterAndShow()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    showOrders(emptyList())
                }
            }
        }
    }

    private fun applyFilterAndShow() {
        val filtered = if (showingRecently) {
            allOrders.filter { it.status != "Delivered" && it.status != "Cancelled"}
        } else {
            allOrders.filter { it.status == "Delivered" }
        }
        showOrders(filtered)
    }

    private fun showOrders(list: List<Order>) {
        if (list.isEmpty()) {
            rvOrders.visibility = View.GONE
            tvEmptyOrders.visibility = View.VISIBLE
        } else {
            rvOrders.visibility = View.VISIBLE
            tvEmptyOrders.visibility = View.GONE
            orderAdapter.updateOrders(list)
        }
    }

    // --- LOGIC REORDER ---
    private fun handleReorder(order: Order) {
        if (order.status != "Delivered") return

        CartManager.clearCart()

        order.items.forEach { oi ->
            val item = ItemRepository.getItemById(oi.productId) ?: return@forEach
            val cartItem = orderItemToCartItem(oi, item)
            CartManager.addItem(cartItem)
        }

        val intent = Intent(this, DrinkMenuActivity::class.java)
        intent.putExtra("open_from_reorder", true)
        startActivity(intent)
    }

    private fun orderItemToCartItem(orderItem: OrderItem, item: Item): CartItem {
        val customizations = mapOf(
            "size" to orderItem.sizeChosen,
            "temp" to orderItem.tempChosen,
            "ice" to orderItem.iceLevel,
            "sugar" to orderItem.sugarLevel,
            "toppings" to orderItem.chosenToppings.joinToString(", ") { it.name },
            "note" to orderItem.itemNote
        )

        return CartItem(
            item = item,
            quantity = orderItem.quantity,
            customizations = customizations,
            price = orderItem.finalUnitPrice
        )
    }
}