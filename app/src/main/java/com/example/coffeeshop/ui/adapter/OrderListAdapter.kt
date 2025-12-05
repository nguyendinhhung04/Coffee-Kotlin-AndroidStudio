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

class OrderListAdapter(
    private var orders: List<Order>,
    private val onDetailsClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderListAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Order ID: #${order._id}"
        holder.tvOrderStatus.text = "Status: ${order.status}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvOrderDate.text = "Date: ${dateFormat.format(Date(order.orderDate))}"

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.tvOrderTotal.text = "Total: ${formatter.format(order.totalAmount)}"

        holder.btnViewDetails.setOnClickListener {
            onDetailsClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
