package com.example.coffeeshop.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Order

class PastOrdersAdapter(
    private var orders: List<Order>,
    private val onOrderClick: (Order) -> Unit,
    private val onReorderClick: (Order) -> Unit
) : RecyclerView.Adapter<PastOrdersAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderTitle: TextView = itemView.findViewById(R.id.tvOrderTitle)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        val btnDetails: Button = itemView.findViewById(R.id.btnOrderDetails)
        val btnReorder: Button = itemView.findViewById(R.id.btnReorder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = orders[position]

        holder.tvOrderTitle.text = order._id ?: "Order #${position + 1}"
        holder.tvOrderDate.text = order.orderDate
        holder.tvOrderStatus.text = order.status
        holder.tvOrderTotal.text = String.format("%,.0f đ", order.totalAmount)

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
}


