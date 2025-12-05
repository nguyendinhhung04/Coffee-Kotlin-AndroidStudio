package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.OrderDAO
import com.example.coffeeshop.data.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.example.coffeeshop.ui.adapter.OrderItemAdapter

class AdminOrderDetailActivity : AppCompatActivity() {

    private lateinit var tvDetailOrderId: TextView
    private lateinit var tvDetailOrderDate: TextView
    private lateinit var tvDetailUserId: TextView
    private lateinit var tvDetailOrderStatus: TextView
    private lateinit var tvDetailDeliveryAddress: TextView
    private lateinit var rvDetailOrderItems: RecyclerView
    private lateinit var tvDetailSubtotal: TextView
    private lateinit var tvDetailDiscount: TextView
    private lateinit var tvDetailShipping: TextView
    private lateinit var tvDetailTaxes: TextView
    private lateinit var tvDetailTotalAmount: TextView
    private lateinit var spinnerOrderStatus: Spinner
    private lateinit var btnUpdateStatus: Button

    private lateinit var orderItemAdapter: OrderItemAdapter
    private var currentOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_detail)

        initViews()
        setupRecyclerView()
        setupStatusSpinner()
        loadOrderDetail()

        btnUpdateStatus.setOnClickListener { updateOrderStatus() }
    }

    private fun initViews() {
        tvDetailOrderId = findViewById(R.id.tvDetailOrderId)
        tvDetailOrderDate = findViewById(R.id.tvDetailOrderDate)
        tvDetailUserId = findViewById(R.id.tvDetailUserId)
        tvDetailOrderStatus = findViewById(R.id.tvDetailOrderStatus)
        tvDetailDeliveryAddress = findViewById(R.id.tvDetailDeliveryAddress)
        rvDetailOrderItems = findViewById(R.id.rvDetailOrderItems)
        tvDetailSubtotal = findViewById(R.id.tvDetailSubtotal)
        tvDetailDiscount = findViewById(R.id.tvDetailDiscount)
        tvDetailShipping = findViewById(R.id.tvDetailShipping)
        tvDetailTaxes = findViewById(R.id.tvDetailTaxes)
        tvDetailTotalAmount = findViewById(R.id.tvDetailTotalAmount)
        spinnerOrderStatus = findViewById(R.id.spinnerOrderStatus)
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus)
    }

    private fun setupRecyclerView() {
        orderItemAdapter = OrderItemAdapter(emptyList())
        rvDetailOrderItems.apply {
            layoutManager = LinearLayoutManager(this@AdminOrderDetailActivity)
            adapter = orderItemAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupStatusSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.order_statuses,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerOrderStatus.adapter = adapter
        }
    }

    private fun loadOrderDetail() {
        val orderId = intent.getStringExtra("order_id")
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        OrderDAO.getOrderById(orderId) { success, message, order ->
            runOnUiThread {
                if (success && order != null) {
                    currentOrder = order
                    displayOrderDetails(order)
                } else {
                    Toast.makeText(this@AdminOrderDetailActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun displayOrderDetails(order: Order) {
        tvDetailOrderId.text = "Order ID: #${order._id}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvDetailOrderDate.text = "Date: ${dateFormat.format(Date(order.orderDate))}"
        tvDetailUserId.text = "User ID: ${order.userId}"
        tvDetailOrderStatus.text = "Status: ${order.status}"

        val address = order.deliveryAddress
        tvDetailDeliveryAddress.text = "${address.fullName}, ${address.phone}\n${address.street}, ${address.ward}, ${address.district}, ${address.city}"

        orderItemAdapter.updateOrderItems(order.items)

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        tvDetailSubtotal.text = "Tổng phụ: ${formatter.format(order.subtotal)}"
        tvDetailDiscount.text = "Giảm giá: ${formatter.format(order.discountAmount)}"
        tvDetailShipping.text = "Phí vận chuyển: ${formatter.format(order.shippingFee)}"
        tvDetailTaxes.text = "Thuế: ${formatter.format(order.taxes)}"
        tvDetailTotalAmount.text = "Tổng cộng: ${formatter.format(order.totalAmount)}"

        // Set spinner selection
        val statusArray = resources.getStringArray(R.array.order_statuses)
        val currentStatusIndex = statusArray.indexOf(order.status)
        if (currentStatusIndex != -1) {
            spinnerOrderStatus.setSelection(currentStatusIndex)
        }
    }

    private fun updateOrderStatus() {
        currentOrder?._id?.let { orderId ->
            val newStatus = spinnerOrderStatus.selectedItem.toString()
            OrderDAO.updateOrderStatus(orderId, newStatus) { success, message ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        // Optionally refresh the order detail or go back
                        loadOrderDetail() // Refresh to show new status
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: Toast.makeText(this, "Không có đơn hàng để cập nhật", Toast.LENGTH_SHORT).show()
    }
}
