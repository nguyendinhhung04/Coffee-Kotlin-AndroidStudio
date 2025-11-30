package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.CartItem
import java.text.NumberFormat
import java.util.Locale

class CartItemAdapter(
    private var items: List<CartItem>,
    private val onPlusClick: (CartItem) -> Unit,
    private val onMinusClick: (CartItem) -> Unit,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivCartItemImage)
        val tvTitle: TextView = view.findViewById(R.id.tvCartProductName)
        val tvDescription: TextView = view.findViewById(R.id.tvCartItemOptions)
        val tvPrice: TextView = view.findViewById(R.id.tvCartPrice)
        val tvQuantity: TextView = view.findViewById(R.id.tvCartQuantity)
        val btnPlus: Button = view.findViewById(R.id.btnPlus)
        val btnMinus: Button = view.findViewById(R.id.btnMinus)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_product, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = items[position]
        val item = cartItem.item

        holder.tvTitle.text = item.name
        holder.tvQuantity.text = cartItem.quantity.toString()

        // Show customizations (size, temp, etc.)
        holder.tvDescription.text =
            if (cartItem.customizations.isNotEmpty()) {
                cartItem.customizations.entries.joinToString { "${it.key}: ${it.value}" }
            } else {
                holder.itemView.context.getString(R.string.no_customization)
            }

        // Format total price in VND
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.tvPrice.text = formatter.format(cartItem.getTotalPrice())

        // Image
        Glide.with(holder.itemView.context)
            .load(item.image_url)
            .placeholder(R.drawable.socola)
            .error(R.drawable.socola)
            .into(holder.ivImage)

        // Actions
        holder.btnPlus.setOnClickListener { onPlusClick(cartItem) }
        holder.btnMinus.setOnClickListener { onMinusClick(cartItem) }
        holder.btnDelete.setOnClickListener { onDeleteClick(cartItem) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
