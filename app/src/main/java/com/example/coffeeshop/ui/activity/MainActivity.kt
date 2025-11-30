package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val username = intent.getStringExtra("username")

        if (username != null) {
            tvGreeting.text = "Good day, $username"
        } else {
            tvGreeting.text = "Good day, John Smith"
        }

        // Setup menu 3 gạch ngang
        val ivMenu = findViewById<ImageView>(R.id.ivMenu)
        ivMenu.setOnClickListener {
            showMenuPopup(it)
        }

        // Ẩn nút Check-in cũ trong Best Seller card
        val btnCheckIn = findViewById<Button>(R.id.btnCheckIn)
        btnCheckIn.visibility = android.view.View.GONE

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    // Already on Home, do nothing or re-initialize
                    true
                }
                R.id.navigation_drink_menu -> {
                    val intent = Intent(this, DrinkMenuActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_your_order -> {
                    val intent = Intent(this, YourOrderActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_favorites -> {
                    val intent = Intent(this, FavoritesActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        // Đặt mục Home được chọn mặc định
        bottomNavigationView.selectedItemId = R.id.navigation_home

    }

    private fun showMenuPopup(view: android.view.View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_check_in -> {
                    val intent = Intent(this, CheckInActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_settings -> {
                    Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_about -> {
                    Toast.makeText(this, "Giới thiệu", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
}
