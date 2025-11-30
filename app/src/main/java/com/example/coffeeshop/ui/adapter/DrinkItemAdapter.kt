package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Item
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.Locale

class DrinkItemAdapter(
    private var items: List<Item>,
    private val onAddClick: (Item) -> Unit
) : RecyclerView.Adapter<DrinkItemAdapter.DrinkViewHolder>() {

    inner class DrinkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivDrinkItemImage)
        val tvTitle: TextView = view.findViewById(R.id.tvDrinkItemTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDrinkItemDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvDrinkItemPrice)
        val fabAdd: FloatingActionButton = view.findViewById(R.id.fabAddOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink, parent, false)
        return DrinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: DrinkViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.name
        holder.tvDescription.text = item.description

        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.tvPrice.text = formatter.format(item.basePrice)

        Glide.with(holder.itemView.context)
            .load(item.image_url)
            .placeholder(R.drawable.socola)
            .error(R.drawable.socola)
            .into(holder.ivImage)

        holder.fabAdd.setOnClickListener { onAddClick(item) }
        holder.itemView.setOnClickListener { onAddClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}
