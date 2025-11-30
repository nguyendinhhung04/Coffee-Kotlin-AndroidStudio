package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class FavoritesActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        bottomNavigationView = findViewById(R.id.bottom_navigation_favorites)

        setupBottomNavigationView()
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_favorites // Highlight "Favorites"
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_drink_menu -> {
                    startActivity(Intent(this, DrinkMenuActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_your_order -> {
                    startActivity(Intent(this, YourOrderActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_favorites -> {
                    // Already on Favorites, do nothing or re-initialize
                    true
                }
                R.id.navigation_payment -> {
                    startActivity(Intent(this, PaymentActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
