package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: List<Order>,
    private val onOrderClick: (Order) -> Unit,
    private val onReorderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.VH>() {
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    private val displayFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    // format ngày giờ: 20/12/2025 10:30
    private val orderDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val currencyFormat: NumberFormat = NumberFormat.getInstance(Locale("vi", "VN"))

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderTitle: TextView = itemView.findViewById(R.id.tvOrderTitle)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val btnDetails: Button = itemView.findViewById(R.id.btnOrderDetails)
        val btnReorder: Button = itemView.findViewById(R.id.btnReorder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = orders[position]

        // Tên order = tên món đầu tiên, fallback về mã
        val firstItemName = order.items.firstOrNull()?.productName ?: "Order #${position + 1}"

        holder.tvOrderTitle.text = firstItemName

        // Format thời gian
        val rawDate = order.orderDate  // ví dụ "2025-12-05T08:13:12.771Z"
        val formattedDate = try {
            val date = isoFormatter.parse(rawDate)
            if (date != null) displayFormatter.format(date) else rawDate
        } catch (e: Exception) {
            rawDate
        }
        holder.tvOrderDate.text = formattedDate

        holder.tvOrderDate.text = formattedDate

        holder.tvOrderStatus.text = order.status

        // Format tiền VND
        holder.tvOrderTotal.text = "${currencyFormat.format(order.totalAmount)} đ"

        holder.itemView.setOnClickListener { onOrderClick(order) }
        holder.btnDetails.setOnClickListener { onOrderClick(order) }

        if (order.status == "Delivered") {
            holder.btnReorder.visibility = View.VISIBLE
            holder.btnReorder.setOnClickListener { onReorderClick(order) }
        } else {
            holder.btnReorder.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
