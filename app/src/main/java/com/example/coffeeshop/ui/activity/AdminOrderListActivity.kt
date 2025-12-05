package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.OrderDAO
import com.example.coffeeshop.ui.adapter.OrderListAdapter

class AdminOrderListActivity : AppCompatActivity() {

    private lateinit var rvAdminOrderList: RecyclerView
    private lateinit var progressBarOrderList: ProgressBar
    private lateinit var tvEmptyOrdersMessage: TextView
    private lateinit var orderListAdapter: OrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_list)

        initViews()
        setupRecyclerView()
        loadOrders()
    }

    private fun initViews() {
        rvAdminOrderList = findViewById(R.id.rvAdminOrderList)
        progressBarOrderList = findViewById(R.id.progressBarOrderList)
        tvEmptyOrdersMessage = findViewById(R.id.tvEmptyOrdersMessage)
    }

    private fun setupRecyclerView() {
        orderListAdapter = OrderListAdapter(emptyList()) { order ->
            // Handle item click - navigate to order detail
            val intent = Intent(this, AdminOrderDetailActivity::class.java).apply {
                putExtra("order_id", order._id)
            }
            startActivity(intent)
        }
        rvAdminOrderList.apply {
            layoutManager = LinearLayoutManager(this@AdminOrderListActivity)
            adapter = orderListAdapter
        }
    }

    private fun loadOrders() {
        showLoading(true)
        OrderDAO.getAllOrders { success, message, orders ->
            runOnUiThread {
                showLoading(false)
                if (success && orders != null) {
                    if (orders.isEmpty()) {
                        showEmptyMessage(true)
                    } else {
                        showEmptyMessage(false)
                        orderListAdapter.updateOrders(orders)
                    }
                } else {
                    showEmptyMessage(true)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBarOrderList.visibility = if (show) View.VISIBLE else View.GONE
        rvAdminOrderList.visibility = if (show) View.GONE else View.VISIBLE
        tvEmptyOrdersMessage.visibility = View.GONE
    }

    private fun showEmptyMessage(show: Boolean) {
        tvEmptyOrdersMessage.visibility = if (show) View.VISIBLE else View.GONE
        rvAdminOrderList.visibility = if (show) View.GONE else View.VISIBLE
        progressBarOrderList.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadOrders() // Refresh orders when returning to this activity
    }
}
