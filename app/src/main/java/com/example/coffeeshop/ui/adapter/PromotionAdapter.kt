package com.example.coffeeshop.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Promotion

class PromotionAdapter(
    private var items: List<Promotion>,
    private val onClick: (Promotion) -> Unit
) : RecyclerView.Adapter<PromotionAdapter.PromoViewHolder>() {

    class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvPromoTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvPromoDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotion, parent, false)
        return PromoViewHolder(v)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val promo = items[position]
        holder.tvTitle.text = promo.name
        holder.tvDesc.text = promo.description
        holder.itemView.setOnClickListener { onClick(promo) }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Promotion>) {
        items = newItems
        notifyDataSetChanged()
    }
}
