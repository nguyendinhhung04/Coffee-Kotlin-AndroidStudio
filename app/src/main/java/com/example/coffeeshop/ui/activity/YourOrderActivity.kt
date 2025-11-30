package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class YourOrderActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnRecently: Button
    private lateinit var btnPastOrders: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_order)

        bottomNavigationView = findViewById(R.id.bottom_navigation_your_order)
        btnRecently = findViewById(R.id.btnRecently)
        btnPastOrders = findViewById(R.id.btnPastOrders)

        setupBottomNavigationView()
        setupOrderFilterButtons()

        // Simulate clicking the Recently button initially
        btnRecently.performClick()
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_your_order // Highlight "Your Order"
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
                    // Already on Your Order, do nothing or re-initialize
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    finish()
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

    private fun setupOrderFilterButtons() {
        btnRecently.setOnClickListener { selectOrderFilterButton(btnRecently) }
        btnPastOrders.setOnClickListener { selectOrderFilterButton(btnPastOrders) }
    }

    private fun selectOrderFilterButton(selectedButton: Button) {
        // Reset all buttons to default state
        btnRecently.setBackgroundResource(R.color.backgroundLight)
        btnRecently.setTextColor(resources.getColor(R.color.dark_brown))
        btnPastOrders.setBackgroundResource(R.color.backgroundLight)
        btnPastOrders.setTextColor(resources.getColor(R.color.dark_brown))

        // Set selected button's state
        selectedButton.setBackgroundResource(R.color.brown)
        selectedButton.setTextColor(resources.getColor(R.color.white))

        // TODO: Filter order items based on selected category
        Toast.makeText(this, "${selectedButton.text} selected", Toast.LENGTH_SHORT).show()
    }
}
