package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.OrderItem
import java.text.NumberFormat
import java.util.*

class OrderItemAdapter(
    private var orderItems: List<OrderItem>
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    inner class OrderItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderItemName: TextView = view.findViewById(R.id.tvOrderItemName)
        val tvOrderItemOptions: TextView = view.findViewById(R.id.tvOrderItemOptions)
        val tvOrderItemPrice: TextView = view.findViewById(R.id.tvOrderItemPrice)
        val tvOrderItemNote: TextView = view.findViewById(R.id.tvOrderItemNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = orderItems[position]

        holder.tvOrderItemName.text = "${item.productName} - x${item.quantity}"

        val options = mutableListOf<String>()
        if (item.sizeChosen.isNotEmpty()) options.add("Size: ${item.sizeChosen}")
        if (item.tempChosen.isNotEmpty()) options.add(item.tempChosen)
        if (item.iceLevel.isNotEmpty() && item.iceLevel != "N/A") options.add("Đá: ${item.iceLevel}")
        if (item.sugarLevel.isNotEmpty() && item.sugarLevel != "N/A") options.add("Đường: ${item.sugarLevel}")
        if (item.chosenToppings.isNotEmpty()) {
            options.add("Topping: ${item.chosenToppings.joinToString(", ") { it.name }}")
        }
        holder.tvOrderItemOptions.text = options.joinToString(" | ")

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val itemTotal = item.finalUnitPrice * item.quantity
        holder.tvOrderItemPrice.text = "${formatter.format(item.finalUnitPrice)} x ${item.quantity} = ${formatter.format(itemTotal)}"

        if (item.itemNote.isNotEmpty()) {
            holder.tvOrderItemNote.text = "Ghi chú: ${item.itemNote}"
            holder.tvOrderItemNote.visibility = View.VISIBLE
        } else {
            holder.tvOrderItemNote.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = orderItems.size

    fun updateOrderItems(newOrderItems: List<OrderItem>) {
        orderItems = newOrderItems
        notifyDataSetChanged()
    }
}
