package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.utils.SeasonHelper
import com.example.coffeeshop.utils.Season
import com.example.coffeeshop.utils.WeatherHelper
import com.example.coffeeshop.utils.WeatherType
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.net.URL

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var btnCoffee: Button
    private lateinit var btnChocolate: Button
    private lateinit var btnOthers: Button
    private lateinit var bottomNav: BottomNavigationView

    private var allItems: List<Item> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        initViews()
        setupButtons()
        setupBottomNav()
        loadAllItemsOnce()
    }

    private fun initViews() {
        btnCoffee = findViewById(R.id.btnCoffee)
        btnChocolate = findViewById(R.id.btnChocolate)
        btnOthers = findViewById(R.id.btnOthers)
        bottomNav = findViewById(R.id.bottom_navigation_drink_menu)
    }

    private fun setupButtons() {
        btnCoffee.setOnClickListener { filterAndShow("Coffee", "Specialty Coffee", "Modern Coffee") }
        btnChocolate.setOnClickListener { filterAndShow("Chocolate") }
        btnOthers.setOnClickListener { filterAndShow("all") }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.navigation_drink_menu
        bottomNav.setOnItemSelectedListener {
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

    private fun filterAndShow(vararg categories: String) {
        // TODO: Implement filtering logic
        Toast.makeText(this, "Filter: ${categories.joinToString()}", Toast.LENGTH_SHORT).show()
    }

    private fun loadAllItemsOnce() {
        ItemDAO.getAllItems { success, message, items ->
            runOnUiThread {
                if (success && items != null) {
                    allItems = items
                    Toast.makeText(this, "Đã tải ${items.size} món", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Không thể tải danh sách món: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
