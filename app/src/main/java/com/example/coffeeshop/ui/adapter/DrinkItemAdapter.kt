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
import java.util.*

class DrinkItemAdapter(
    private var items: List<Item>,
    private val onAddClick: (Item) -> Unit,
    private val onFavoriteClick: (Item) -> Unit,
    private val favoriteIds: MutableSet<String>
) : RecyclerView.Adapter<DrinkItemAdapter.DrinkViewHolder>() {

    inner class DrinkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivDrinkItemImage)
        val tvTitle: TextView = view.findViewById(R.id.tvDrinkItemTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDrinkItemDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvDrinkItemPrice)
        val fabAdd: FloatingActionButton = view.findViewById(R.id.fabAddOrder)

        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)


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

        // Load image from assets/item_img folder
        val context = holder.itemView.context
        val imageName = item.image_url

        val isFav = item._id != null && favoriteIds.contains(item._id)
        holder.ivFavorite.setImageResource(
            if (isFav) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )

        holder.ivFavorite.setOnClickListener { onFavoriteClick(item) }
        holder.fabAdd.setOnClickListener { onAddClick(item) }
        holder.itemView.setOnClickListener { onAddClick(item) }

        try {
            // Try to load from assets/item_img/
            val inputStream = context.assets.open("item_img/$imageName")
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            holder.ivImage.setImageBitmap(bitmap)
            inputStream.close()
            android.util.Log.d("DrinkAdapter", "Image loaded: item_img/$imageName")
        } catch (e: Exception) {
            // If not found, use default image
            android.util.Log.w("DrinkAdapter", "Image not found: item_img/$imageName, error: ${e.message}")
            holder.ivImage.setImageResource(R.drawable.socola)
        }


        fun updateItems(newItems: List<Item>) {
            items = newItems
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int = items.size
    fun getItems(): List<Item> = items


    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}