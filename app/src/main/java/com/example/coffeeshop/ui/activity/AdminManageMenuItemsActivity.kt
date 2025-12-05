package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R

class AdminManageMenuItemsActivity : AppCompatActivity() {

    private lateinit var btnAddMenuItem: Button
    private lateinit var btnAddComboItem: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_manage_menu_items)

        btnAddMenuItem = findViewById(R.id.btnAddMenuItem)
        btnAddComboItem = findViewById(R.id.btnAddComboItem)

        btnAddMenuItem.setOnClickListener {
            val intent = Intent(this, AdminAddMenuItemActivity::class.java)
            startActivity(intent)
        }

        btnAddComboItem.setOnClickListener {
            val intent = Intent(this, AdminAddComboActivity::class.java)
            startActivity(intent)
        }
    }
}
