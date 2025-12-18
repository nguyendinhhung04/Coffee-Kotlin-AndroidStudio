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
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.ui.adapter.DrinkItemAdapter
import com.example.coffeeshop.ui.fragment.CartBottomSheetFragment
import com.example.coffeeshop.utils.CartManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.coffeeshop.data.dao.FavoriteDAO
import com.example.coffeeshop.utils.NotificationConstants
import com.example.coffeeshop.utils.UserSessionManager
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import com.example.coffeeshop.utils.unaccentLower

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnCoffee: Button
    private lateinit var btnChocolate: Button
    private lateinit var btnOthers: Button
    private lateinit var rvDrinkItems: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvCartBadge: TextView
    private lateinit var fabCart: FloatingActionButton

    private lateinit var etSearch: EditText
    private var allItems: List<Item> = emptyList()
    private var displayedItems: List<Item> = emptyList()

    private lateinit var ivMenu: ImageView

    // Notification components
    private lateinit var layoutBell: FrameLayout
    private lateinit var ivNotification: ImageView
    private lateinit var viewNotificationDot: View

    private lateinit var sessionManager: UserSessionManager
    private val favoriteIds = mutableSetOf<String>()

    // Adapter for menu list
    private lateinit var drinkAdapter: DrinkItemAdapter
    private var currentCategory = "coffee"

    // --- 1. RECEIVER LẮNG NGHE THÔNG BÁO MỚI ---
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBellUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        sessionManager = UserSessionManager(this)

        initViews()
        setupBottomNavigationView()
        setupFilterButtons()
        setupRecyclerView()
        setupCartButton()
        setupSearchBar()

        // ===== Click Bar Menu & Notification =====
        ivMenu.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Click vào cụm chuông: Tắt hiệu ứng và mở màn hình thông báo
        layoutBell.setOnClickListener {
            val prefs = getSharedPreferences(NotificationConstants.PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(NotificationConstants.KEY_HAS_UNREAD, false).apply()

            updateBellUI() // Cập nhật UI ngay lập tức

            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        loadItemsByCategory("coffee")
    }

    private fun initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_drink_menu)
        btnCoffee = findViewById(R.id.btnCoffee)
        btnChocolate = findViewById(R.id.btnChocolate)
        btnOthers = findViewById(R.id.btnOthers)
        rvDrinkItems = findViewById(R.id.rvDrinkItems)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        tvCartBadge = findViewById(R.id.tvCartBadge)
        fabCart = findViewById(R.id.fabCart)
        ivMenu = findViewById(R.id.ivDrinkMenuMenu)
        etSearch = findViewById(R.id.etSearch)

        // Bind notification views
        layoutBell = findViewById(R.id.layoutBell)
        ivNotification = findViewById(R.id.ivBell)
        viewNotificationDot = findViewById(R.id.viewNotificationDot)
    }

    // --- 2. HÀM CẬP NHẬT GIAO DIỆN CHUÔNG (RUNG + CHẤM ĐỎ) ---
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

    private fun setupRecyclerView() {
        drinkAdapter = DrinkItemAdapter(
            emptyList(),
            onAddClick = { item ->
                onItemAddClick(item)
            },
            onFavoriteClick = { item ->
                toggleFavorite(item)
            },
            favoriteIds = favoriteIds
        )

        rvDrinkItems.apply {
            layoutManager = LinearLayoutManager(this@DrinkMenuActivity)
            adapter = drinkAdapter
        }
    }

    private fun toggleFavorite(item: Item) {
        val userId = sessionManager.getUserId()
        val itemId = item._id

        if (userId.isNullOrBlank() || itemId.isNullOrBlank()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            return
        }

        if (favoriteIds.contains(itemId)) {
            FavoriteDAO.removeFavorite(userId, itemId) { success, msg ->
                runOnUiThread {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        favoriteIds.remove(itemId)
                        drinkAdapter.notifyDataSetChanged()
                    }
                }
            }
        } else {
            FavoriteDAO.addFavorite(userId, itemId) { success, msg ->
                runOnUiThread {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        favoriteIds.add(itemId)
                        drinkAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_drink_menu
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_drink_menu -> true
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
    }

    private fun setupFilterButtons() {
        btnCoffee.setOnClickListener {
            selectFilterButton(btnCoffee)
            loadItemsByCategory("coffee")
        }
        btnChocolate.setOnClickListener {
            selectFilterButton(btnChocolate)
            loadItemsByCategory("chocolate")
        }
        btnOthers.setOnClickListener {
            selectFilterButton(btnOthers)
            loadItemsByCategory("other")
        }
        selectFilterButton(btnCoffee)
    }

    private fun selectFilterButton(selectedButton: Button) {
        btnCoffee.setBackgroundResource(R.drawable.bg_button_unselected)
        btnCoffee.setTextColor(resources.getColor(R.color.dark_brown, null))
        btnChocolate.setBackgroundResource(R.drawable.bg_button_unselected)
        btnChocolate.setTextColor(resources.getColor(R.color.dark_brown, null))
        btnOthers.setBackgroundResource(R.drawable.bg_button_unselected)
        btnOthers.setTextColor(resources.getColor(R.color.dark_brown, null))

        selectedButton.setBackgroundResource(R.drawable.bg_button_selected)
        selectedButton.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun loadItemsByCategory(category: String) {
        currentCategory = category
        showLoading(true)

        ItemDAO.getItemsByCategory(category) { success, message, items ->
            runOnUiThread {
                showLoading(false)
                if (success && items != null) {
                    allItems = items
                    displayedItems = items
                    if (items.isEmpty()) {
                        showEmptyMessage(true)
                    } else {
                        showEmptyMessage(false)
                        drinkAdapter.updateItems(displayedItems)
                    }
                } else {
                    showEmptyMessage(true)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearchBar() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                filterDrinks(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterDrinks(query: String) {
        val q = query.unaccentLower()

        displayedItems = if (q.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                item.name.unaccentLower().contains(q)
            }
        }

        if (displayedItems.isEmpty()) {
            tvEmptyMessage.visibility = View.VISIBLE
            rvDrinkItems.visibility = View.GONE
        } else {
            tvEmptyMessage.visibility = View.GONE
            rvDrinkItems.visibility = View.VISIBLE
            drinkAdapter.updateItems(displayedItems)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvDrinkItems.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmptyMessage(show: Boolean) {
        tvEmptyMessage.visibility = if (show) View.VISIBLE else View.GONE
        rvDrinkItems.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun onItemAddClick(item: Item) {
        val cartItem = CartItem(
            item = item,
            quantity = 1,
            customizations = emptyMap(),
            price = item.basePrice
        )
        CartManager.addItem(cartItem)
        updateCartBadge()
        Toast.makeText(this, "Added ${item.name} to cart", Toast.LENGTH_SHORT).show()
    }

    private fun setupCartButton() {
        fabCart.setOnClickListener {
            if (CartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                val cartFragment = CartBottomSheetFragment.newInstance()
                cartFragment.show(supportFragmentManager, "cart_bottom_sheet")
            }
        }
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        if (count > 0) {
            tvCartBadge.visibility = View.VISIBLE
            tvCartBadge.text = count.toString()
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }

    // --- 3. QUẢN LÝ VÒNG ĐỜI (LẮNG NGHE BROADCAST) ---
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
        updateCartBadge()
        updateBellUI() // Luôn cập nhật trạng thái chuông khi quay lại màn hình

        val userId = sessionManager.getUserId() ?: return
        FavoriteDAO.getFavoritesByUser(userId) { success, _, items ->
            if (success && items != null) {
                favoriteIds.clear()
                favoriteIds.addAll(items.mapNotNull { it._id })
                runOnUiThread { drinkAdapter.notifyDataSetChanged() }
            }
        }
    }
}