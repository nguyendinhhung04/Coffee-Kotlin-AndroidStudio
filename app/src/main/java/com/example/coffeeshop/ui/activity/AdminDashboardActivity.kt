package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var btnManageOrders: Button
    private lateinit var btnManageMenuItems: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        btnManageOrders = findViewById(R.id.btnManageOrders)
        btnManageMenuItems = findViewById(R.id.btnManageMenuItems)

        btnManageOrders.setOnClickListener {
            val intent = Intent(this, AdminOrderListActivity::class.java)
            startActivity(intent)
        }

        btnManageMenuItems.setOnClickListener {
            val intent = Intent(this, AdminManageMenuItemsActivity::class.java)
            startActivity(intent)
        }
    }
}
