package com.example.coffeeshop.ui.adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Item
import java.text.NumberFormat
import java.util.Locale

class RecommendationAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.RecViewHolder>() {

    // ViewHolder
    class RecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivRecImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvRecTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvRecPrice)
    }

    // tạo ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecViewHolder(v)
    }

    // bind dữ liệu
    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.name
        holder.tvPrice.text = formatPrice(item.basePrice)
        loadItemImage(holder.itemView.context, item.image_url, holder.ivImage)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatPrice(price: Double): String {
        val nf = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return nf.format(price)
    }

    private fun loadItemImage(context: Context, imageName: String, imageView: ImageView) {
        try {
            val inputStream = context.assets.open("item_img/$imageName")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            inputStream.close()
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.socola)
        }
    }
}
