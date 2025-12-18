package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.ui.adapter.DrinkItemAdapter   // <-- use DrinkItemAdapter
import com.example.coffeeshop.ui.fragment.CartBottomSheetFragment
import com.example.coffeeshop.utils.CartManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageView
import com.example.coffeeshop.data.dao.FavoriteDAO
import com.example.coffeeshop.ui.activity.SettingsActivity
import com.example.coffeeshop.utils.UserSessionManager
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

    private lateinit var ivMenu: ImageView

    private lateinit var ivNotification: ImageView

    private lateinit var sessionManager: UserSessionManager
    private val favoriteIds = mutableSetOf<String>()



    private lateinit var etSearch: EditText
    private var allItems: List<Item> = emptyList()
    private var displayedItems: List<Item> = emptyList()
    // Adapter for menu list = Item
    private lateinit var drinkAdapter: DrinkItemAdapter
    private var currentCategory = "coffee"

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


        ivMenu = findViewById(R.id.ivDrinkMenuMenu)
        ivNotification = findViewById(R.id.ivBell)

        ivMenu.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        ivNotification.setOnClickListener {
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
        etSearch = findViewById(R.id.etSearch)

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
            favoriteIds = favoriteIds   // nếu adapter có param này, xem bước 3
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
            // Đã tồn tại -> xoá
            FavoriteDAO.removeFavorite(userId, itemId) { success, msg ->
                runOnUiThread {
                    Log.d("Fav", "favoriteIds=$favoriteIds, click itemId=$itemId")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        favoriteIds.remove(itemId)
                        drinkAdapter.notifyDataSetChanged()
                    }
                }
            }
        } else {
            // Chưa có -> thêm
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
                    // Không dùng finish() để MainActivity load lại
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

    override fun onResume() {
        super.onResume()
        updateCartBadge()

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
