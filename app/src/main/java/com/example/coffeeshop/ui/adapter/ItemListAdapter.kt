package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Item
import com.google.android.material.button.MaterialButton

class ItemListAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvItemPrice: TextView = view.findViewById(R.id.tvItemPrice)
        val ivItemImage: ImageView = view.findViewById(R.id.ivItemImage)
        val btnAddToCombo: MaterialButton = view.findViewById(R.id.btnAddToCombo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink_card_simple, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        
        holder.tvItemName.text = item.name
        holder.tvItemPrice.text = "${item.basePrice.toInt()}đ"
        
        // TODO: Load image from URL if needed
        holder.ivItemImage.setImageResource(R.drawable.socola)
        
        holder.btnAddToCombo.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}

