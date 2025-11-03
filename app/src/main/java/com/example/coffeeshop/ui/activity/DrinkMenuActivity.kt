package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnCoffee: Button
    private lateinit var btnChocolate: Button
    private lateinit var btnOthers: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        bottomNavigationView = findViewById(R.id.bottom_navigation_drink_menu)
        btnCoffee = findViewById(R.id.btnCoffee)
        btnChocolate = findViewById(R.id.btnChocolate)
        btnOthers = findViewById(R.id.btnOthers)

        setupBottomNavigationView()
        setupFilterButtons()

        // Simulate clicking the Coffee button initially
        btnCoffee.performClick()
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_drink_menu // Highlight "Drink Menu"
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_drink_menu -> {
                    // Already on Drink Menu, do nothing or re-initialize
                    true
                }
                R.id.navigation_your_order -> {
                    startActivity(Intent(this, YourOrderActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFilterButtons() {
        btnCoffee.setOnClickListener { selectFilterButton(btnCoffee) }
        btnChocolate.setOnClickListener { selectFilterButton(btnChocolate) }
        btnOthers.setOnClickListener { selectFilterButton(btnOthers) }
    }

    private fun selectFilterButton(selectedButton: Button) {
        // Reset all buttons to default state
        btnCoffee.setBackgroundResource(R.color.backgroundLight)
        btnCoffee.setTextColor(resources.getColor(R.color.dark_brown))
        btnChocolate.setBackgroundResource(R.color.backgroundLight)
        btnChocolate.setTextColor(resources.getColor(R.color.dark_brown))
        btnOthers.setBackgroundResource(R.color.backgroundLight)
        btnOthers.setTextColor(resources.getColor(R.color.dark_brown))

        // Set selected button's state
        selectedButton.setBackgroundResource(R.color.brown)
        selectedButton.setTextColor(resources.getColor(R.color.white))

        // TODO: Filter drink items based on selected category
        Toast.makeText(this, "${selectedButton.text} selected", Toast.LENGTH_SHORT).show()
    }
}
