package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.FavoriteDAO
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.ui.adapter.DrinkItemAdapter
import com.example.coffeeshop.utils.CartManager
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ImageView
import com.example.coffeeshop.ui.fragment.CartBottomSheetFragment



class FavoritesActivity : AppCompatActivity() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var tvEmptyFavorites: TextView

    private lateinit var sessionManager: UserSessionManager
    private lateinit var adapter: DrinkItemAdapter

    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var ivFavoritesCart: ImageView

    private lateinit var tvFavoritesCartBadge: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        bottomNavigationView = findViewById(R.id.bottom_navigation_favorites)
        setupBottomNavigation()

        ivFavoritesCart = findViewById(R.id.ivFavoritesCart)
        tvFavoritesCartBadge = findViewById(R.id.tvFavoritesCartBadge)

        ivFavoritesCart.setOnClickListener {
            if (CartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                val cartFragment = CartBottomSheetFragment.newInstance()
                cartFragment.show(supportFragmentManager, "cart_bottom_sheet")
            }
        }


        sessionManager = UserSessionManager(this)

        rvFavorites = findViewById(R.id.rvFavorites)
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites)

        adapter = DrinkItemAdapter(emptyList()) { item ->
            // Add to cart nhanh từ favorites
            val cartItem = com.example.coffeeshop.data.model.CartItem(
                item = item,
                quantity = 1,
                customizations = emptyMap(),
                price = item.basePrice
            )
            CartManager.addItem(cartItem)
            updateCartBadge()
            Toast.makeText(this, "Added ${item.name} to cart", Toast.LENGTH_SHORT).show()
        }

        rvFavorites.layoutManager = LinearLayoutManager(this)
        rvFavorites.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
        updateCartBadge()
    }

    private fun updateCartBadge() {
        val count = CartManager.getItemCount()
        if (count > 0) {
            tvFavoritesCartBadge.visibility = View.VISIBLE
            tvFavoritesCartBadge.text = count.toString()
        } else {
            tvFavoritesCartBadge.visibility = View.GONE
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.selectedItemId = R.id.navigation_favorites
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_drink_menu -> {
                    startActivity(Intent(this, DrinkMenuActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_your_order -> {
                    startActivity(Intent(this, YourOrderActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_favorites -> true

                else -> false
            }
        }
    }

    private fun loadFavorites() {
        val userId = sessionManager.getUserId()
        if (userId.isNullOrBlank()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FavoriteDAO.getFavoritesByUser(userId) { success, message, items ->
            runOnUiThread {
                if (!success || items == null) {
                    tvEmptyFavorites.visibility = View.VISIBLE
                    rvFavorites.visibility = View.GONE
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } else if (items.isEmpty()) {
                    tvEmptyFavorites.visibility = View.VISIBLE
                    rvFavorites.visibility = View.GONE
                } else {
                    tvEmptyFavorites.visibility = View.GONE
                    rvFavorites.visibility = View.VISIBLE
                    adapter.updateItems(items)
                }
            }
        }
    }
}
