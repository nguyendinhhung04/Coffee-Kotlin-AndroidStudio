package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.OrderDAO
import com.example.coffeeshop.data.model.DeliveryAddress
import com.example.coffeeshop.data.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var tvOrderId: TextView
    private lateinit var tvOrderStatus: TextView
    private lateinit var tvOrderTotal: TextView
    private lateinit var tvItemsSummary: TextView
    private lateinit var tvUsedPoints: TextView
    private lateinit var tvDiscountByPoints: TextView

    private lateinit var rgDeliveryMethod: RadioGroup
    private lateinit var rbPickup: RadioButton
    private lateinit var rbCod: RadioButton

    private lateinit var layoutAddress: LinearLayout
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etStreet: EditText
    private lateinit var etWard: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etCity: EditText

    private lateinit var btnCancelOrder: Button
    private lateinit var btnConfirmOrder: Button

    private var currentOrder: Order? = null
    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        initViews()
        val orderId = intent.getStringExtra("order_id")
        if (orderId.isNullOrBlank()) {
            Toast.makeText(this, "Không có order id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        loadOrder(orderId)
        setupListeners()
    }

    private fun initViews() {
        tvOrderId = findViewById(R.id.tvOrderId)
        tvOrderStatus = findViewById(R.id.tvOrderStatusDetail)
        tvOrderTotal = findViewById(R.id.tvOrderTotalDetail)
        tvItemsSummary = findViewById(R.id.tvItemsSummary)
        tvUsedPoints = findViewById(R.id.tvUsedPoints)
        tvDiscountByPoints = findViewById(R.id.tvDiscountByPoints)

        rgDeliveryMethod = findViewById(R.id.rgDeliveryMethod)
        rbPickup = findViewById(R.id.rbPickup)
        rbCod = findViewById(R.id.rbCod)

        layoutAddress = findViewById(R.id.layoutAddress)
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etStreet = findViewById(R.id.etStreet)
        etWard = findViewById(R.id.etWard)
        etDistrict = findViewById(R.id.etDistrict)
        etCity = findViewById(R.id.etCity)

        btnCancelOrder = findViewById(R.id.btnCancelOrder)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)
    }

    private fun loadOrder(orderId: String) {
        OrderDAO.getOrderById(orderId) { success, message, order ->
            runOnUiThread {
                if (!success || order == null) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    currentOrder = order
                    bindOrder(order)
                }
            }
        }
    }

    private fun bindOrder(order: Order) {
        tvOrderId.text = "Order: ${order._id}"
        tvOrderStatus.text = "Status: ${order.status}"

        if (order.usedPointAmount.toDouble() > 0) {
            tvUsedPoints.visibility = View.VISIBLE
            tvDiscountByPoints.visibility = View.VISIBLE
            tvUsedPoints.text = "Điểm đã dùng: ${order.usedPointAmount}"
            tvDiscountByPoints.text = "Giảm giá: -${formatter.format(order.discountByPointAmount)}"
        }

        tvOrderTotal.text = "Total: ${formatter.format(order.totalAmount)}"

        // Items summary đơn giản
        val itemsText = buildString {
            // order.orderDate là chuỗi ISO: 2025-12-03T08:19:08.423Z
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

            val dateText = try {
                val d = inputFormat.parse(order.orderDate)
                if (d != null) outputFormat.format(d) else "-"
            } catch (e: Exception) {
                "-"
            }

            append("Thời gian: $dateText\n\n")
            order.items.forEach { item ->
                append("- ${item.productName} x${item.quantity} (${formatter.format(item.finalUnitPrice)})\n")
            }
        }
        tvItemsSummary.text = itemsText

        // Nếu địa chỉ trống -> mặc định coi là pickup
        val addr: DeliveryAddress? = order.deliveryAddress
        val hasAddress = addr != null && !addr.street.isNullOrBlank()
        if (hasAddress) {
            rbCod.isChecked = true
            layoutAddress.visibility = View.VISIBLE
            etFullName.setText(addr?.fullName)
            etPhone.setText(addr?.phone)
            etStreet.setText(addr?.street)
            etWard.setText(addr?.ward)
            etDistrict.setText(addr?.district)
            etCity.setText(addr?.city)
        } else {
            rbPickup.isChecked = true
            layoutAddress.visibility = View.GONE
        }

        // Ẩn/hiện nút hủy theo trạng thái
        val blocked = listOf("Confirmed", "Delivering", "Delivered")
        if (blocked.contains(order.status)) {
            btnCancelOrder.visibility = View.GONE
            btnConfirmOrder.visibility = View.GONE
        } else {
            btnCancelOrder.visibility = View.VISIBLE
            btnConfirmOrder.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        rgDeliveryMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCod) {
                layoutAddress.visibility = View.VISIBLE
            } else {
                layoutAddress.visibility = View.GONE
            }
        }

        btnCancelOrder.setOnClickListener {
            val order = currentOrder ?: return@setOnClickListener

            // Không cho hủy nếu đã Confirmed / Delivering / Delivered
            val blocked = listOf("Confirmed", "Delivering", "Delivered")
            if (blocked.contains(order.status)) {
                Toast.makeText(
                    this,
                    "Đơn đã ở trạng thái ${order.status}, không thể hủy.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val orderId = order._id ?: return@setOnClickListener
            OrderDAO.cancelOrder(orderId) { success, message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        tvOrderStatus.text = "Status: Cancel"
                        finish()
                    }
                }
            }
        }

        btnConfirmOrder.setOnClickListener {
            val order = currentOrder ?: return@setOnClickListener

            if (rbCod.isChecked) {
                // Kiểm tra địa chỉ
                if (etFullName.text.isNullOrBlank() ||
                    etPhone.text.isNullOrBlank() ||
                    etStreet.text.isNullOrBlank() ||
                    etWard.text.isNullOrBlank() ||
                    etDistrict.text.isNullOrBlank() ||
                    etCity.text.isNullOrBlank()
                ) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ địa chỉ", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Ở đây bạn cần có API update đầy đủ order (address + shippingFee + paymentMethod)
                // Hiện tại OrderDAO.updateOrderStatus chỉ update được status.
                // Tối thiểu: set status = Confirmed, và bạn xử lý +15k phí ship ở backend.
                val orderId = order._id ?: return@setOnClickListener
                OrderDAO.updateOrderStatus(orderId, "Confirmed") { success, message ->
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            tvOrderStatus.text = "Status: Confirmed (COD)"
                        }
                    }
                }
            } else {
                // Nhận tại quán: status -> Confirmed, phí ship 0 (xử lý ở backend)
                val orderId = order._id ?: return@setOnClickListener
                OrderDAO.updateOrderStatus(orderId, "Confirmed") { success, message ->
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        if (success) {
                            tvOrderStatus.text = "Status: Confirmed (Pickup)"
                        }
                    }
                }
            }
            finish()
        }
    }
}
