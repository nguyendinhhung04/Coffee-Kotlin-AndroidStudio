
package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.model.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnCoffee: Button
    private lateinit var btnChocolate: Button
    private lateinit var btnOthers: Button
    private lateinit var fabAddOrder1: FloatingActionButton

    private var coffeeItems: List<Item> = emptyList()
    private var chocolateItems: List<Item> = emptyList()
    private var otherItems: List<Item> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        bottomNavigationView = findViewById(R.id.bottom_navigation_drink_menu)
        btnCoffee = findViewById(R.id.btnCoffee)
        btnChocolate = findViewById(R.id.btnChocolate)
        btnOthers = findViewById(R.id.btnOthers)
        fabAddOrder1 = findViewById(R.id.fabAddOrder1)

        setupBottomNavigationView()
        setupFilterButtons()
        fetchAllItems()

        fabAddOrder1.setOnClickListener {
            Toast.makeText(this, "FAB clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAllItems() {
        val apiClient = ApiClient.instance

        apiClient.getCoffeeItems().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    coffeeItems = response.body() ?: emptyList()
                    Log.d("DrinkMenuActivity", "Coffee items fetched: ${coffeeItems.size}")
                    // Initially select coffee
                    selectFilterButton(btnCoffee)
                } else {
                    Toast.makeText(this@DrinkMenuActivity, "Failed to fetch coffee items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Log.e("DrinkMenuActivity", "API call failed", t)
                Toast.makeText(this@DrinkMenuActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        apiClient.getChocolateItems().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    chocolateItems = response.body() ?: emptyList()
                    Log.d("DrinkMenuActivity", "Chocolate items fetched: ${chocolateItems.size}")
                } else {
                    Toast.makeText(this@DrinkMenuActivity, "Failed to fetch chocolate items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Log.e("DrinkMenuActivity", "API call failed", t)
                Toast.makeText(this@DrinkMenuActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        apiClient.getOtherItems().enqueue(object : Callback<List<Item>> {
            override fun onResponse(call: Call<List<Item>>, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    otherItems = response.body() ?: emptyList()
                    Log.d("DrinkMenuActivity", "Other items fetched: ${otherItems.size}")
                } else {
                    Toast.makeText(this@DrinkMenuActivity, "Failed to fetch other items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Item>>, t: Throwable) {
                Log.e("DrinkMenuActivity", "API call failed", t)
                Toast.makeText(this@DrinkMenuActivity, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

        // Filter drink items based on selected category
        when (selectedButton) {
            btnCoffee -> {
                Log.d("DrinkMenuActivity", "Displaying coffee items.")
                Toast.makeText(this, "Displaying ${coffeeItems.size} coffee items.", Toast.LENGTH_SHORT).show()
                // TODO: Update a RecyclerView with coffeeItems
            }
            btnChocolate -> {
                Log.d("DrinkMenuActivity", "Displaying chocolate items.")
                Toast.makeText(this, "Displaying ${chocolateItems.size} chocolate items.", Toast.LENGTH_SHORT).show()
                // TODO: Update a RecyclerView with chocolateItems
            }
            btnOthers -> {
                Log.d("DrinkMenuActivity", "Displaying other items.")
                Toast.makeText(this, "Displaying ${otherItems.size} other items.", Toast.LENGTH_SHORT).show()
                // TODO: Update a RecyclerView with otherItems
            }
        }
    }
}
