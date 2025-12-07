package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class  OrderAdapter(
    private var orders: List<Order>,
    private val onDetailsClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivOrderThumb: ImageView = view.findViewById(R.id.ivOrderThumb)
        val tvOrderTitle: TextView = view.findViewById(R.id.tvOrderTitle)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val btnOrderDetails: Button = view.findViewById(R.id.btnOrderDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // Title: dùng sản phẩm đầu tiên hoặc "X items"
        val firstName = order.items.firstOrNull()?.productName ?: "Order"
        val count = order.items.sumOf { it.quantity }
        holder.tvOrderTitle.text = "$firstName (${count} items)"

        // Date
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

        val date = try {
            inputFormat.parse(order.orderDate)
        } catch (e: Exception) {
            null
        }

        holder.tvOrderDate.text = if (date != null) {
            outputFormat.format(date)
        } else {
            "-"
        }

        // Status
        holder.tvOrderStatus.text = order.status

        // Total amount
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.tvOrderTotal.text = formatter.format(order.totalAmount)

        // Ảnh thumb: tạm dùng ảnh cố định
        holder.ivOrderThumb.setImageResource(R.drawable.socola)

        holder.btnOrderDetails.setOnClickListener {
            onDetailsClick(order)
        }
    }

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
