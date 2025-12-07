package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.OrderDAO
import com.example.coffeeshop.data.model.Order
import com.example.coffeeshop.utils.UserSessionManager
import com.example.coffeeshop.ui.adapter.OrderAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

class YourOrderActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnRecently: Button
    private lateinit var btnPastOrders: Button

    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmptyOrders: TextView

    private lateinit var orderAdapter: OrderAdapter
    private var allOrders: List<Order> = emptyList()
    private var showingRecently: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_order)

        bottomNavigationView = findViewById(R.id.bottom_navigation_your_order)
        btnRecently = findViewById(R.id.btnRecently)
        btnPastOrders = findViewById(R.id.btnPastOrders)
        rvOrders = findViewById(R.id.rvOrders)
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders)

        setupBottomNavigationView()
        setupRecyclerView()
        setupOrderFilterButtons()

        // Mặc định: Recently
        btnRecently.performClick()
        loadOrdersFromServer()
    }

    override fun onResume() {
        super.onResume()
        loadOrdersFromServer()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList()) { order ->
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("order_id", order._id ?: "")
            startActivity(intent)
        }
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
                    finish()
                    true
                }

                R.id.navigation_drink_menu -> {
                    startActivity(Intent(this, DrinkMenuActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_your_order -> true

                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
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
        // Reset
        btnRecently.setBackgroundResource(R.color.backgroundLight)
        btnRecently.setTextColor(resources.getColor(R.color.dark_brown, null))
        btnPastOrders.setBackgroundResource(R.color.backgroundLight)
        btnPastOrders.setTextColor(resources.getColor(R.color.dark_brown, null))

        // Selected
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

        // Có thể show loading ở đây nếu bạn thêm ProgressBar
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
            // Recently: Pending, Confirmed, Delivering
            allOrders.filter { it.status != "Delivered" && it.status != "Cancelled"}
        } else {
            // Past: Delivered
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
}
