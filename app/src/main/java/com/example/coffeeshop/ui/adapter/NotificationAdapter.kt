package com.example.coffeeshop.ui.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.Notification // <-- Tạo class này ở Bước 4

class NotificationAdapter(private var notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewNotificationTitle)
        val body: TextView = itemView.findViewById(R.id.textViewNotificationBody)
        val date: TextView = itemView.findViewById(R.id.textViewNotificationDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.title.text = notification.title
        holder.body.text = notification.body

        // Sử dụng hàm định dạng ngày tháng mới
        holder.date.text = notification.getFormattedCreatedAt()
    }

    override fun getItemCount() = notifications.size

    // Hàm để cập nhật dữ liệu mới cho adapter
    fun updateData(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}