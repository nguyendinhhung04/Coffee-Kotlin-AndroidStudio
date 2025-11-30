package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.utils.SeasonHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.URL

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var ivRecommendedDrink1: ImageView
    private lateinit var ivRecommendedDrink2: ImageView
    private lateinit var tvDrinkName1: TextView
    private lateinit var tvDrinkName2: TextView
    private lateinit var tvDrinkPrice1: TextView
    private lateinit var tvDrinkPrice2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        initViews()
        setupBottomNav()
        loadRecommendedDrinksBySeason()
    }

    private fun initViews() {
        ivRecommendedDrink1 = findViewById(R.id.ivRecommendedDrink1)
        ivRecommendedDrink2 = findViewById(R.id.ivRecommendedDrink2)
        tvDrinkName1 = findViewById(R.id.tvRecommendedDrinkName1)
        tvDrinkName2 = findViewById(R.id.tvRecommendedDrinkName2)
        tvDrinkPrice1 = findViewById(R.id.tvRecommendedDrinkPrice1)
        tvDrinkPrice2 = findViewById(R.id.tvRecommendedDrinkPrice2)
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_payment)
        bottomNav.selectedItemId = R.id.navigation_payment
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.navigation_drink_menu -> startActivity(Intent(this, DrinkMenuActivity::class.java))
                R.id.navigation_your_order -> startActivity(Intent(this, YourOrderActivity::class.java))
                R.id.navigation_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
                R.id.navigation_payment -> {
                    // Already on payment screen
                    true
                }
            }
            if (it.itemId != R.id.navigation_payment) {
                finish()
            }
            true
        }
    }

    private fun loadRecommendedDrinksBySeason() {
        // Xác định mùa hiện tại
        val currentSeason = SeasonHelper.getCurrentSeason()
        val seasonKeywords = SeasonHelper.getSeasonKeywords(currentSeason)
        
        // Lấy món gợi ý theo mùa
        ItemDAO.getRecommendedItemsBySeason(seasonKeywords) { success, message, items ->
            runOnUiThread {
                if (success && items != null && items.isNotEmpty()) {
                    // Hiển thị món đầu tiên
                    if (items.size >= 1) {
                        displayRecommendedDrink(items[0], ivRecommendedDrink1, 0)
                    }
                    
                    // Hiển thị món thứ hai (nếu có)
                    if (items.size >= 2) {
                        displayRecommendedDrink(items[1], ivRecommendedDrink2, 1)
                    }
                } else {
                    // Nếu không tìm thấy món theo mùa, dùng món mặc định
                    loadDefaultRecommendedDrinks()
                }
            }
        }
    }

    private fun displayRecommendedDrink(item: com.example.coffeeshop.data.model.Item, imageView: ImageView, index: Int) {
        // Load ảnh
        if (item.image_url.isNotEmpty()) {
            loadImageFromUrl(imageView, item.image_url)
        } else {
            imageView.setImageResource(R.drawable.placeholder_coffee)
        }
        
        // Cập nhật tên và giá
        when (index) {
            0 -> {
                tvDrinkName1.text = item.name
                tvDrinkPrice1.text = "Rp ${item.basePrice.toInt()}"
            }
            1 -> {
                tvDrinkName2.text = item.name
                tvDrinkPrice2.text = "Rp ${item.basePrice.toInt()}"
            }
        }
    }

    private fun loadDefaultRecommendedDrinks() {
        // Fallback: Load ảnh mặc định nếu không tìm thấy món theo mùa
        loadImageFromUrl(
            ivRecommendedDrink1,
            "https://i.imgur.com/qR1sQF7.png"
        )
        loadImageFromUrl(
            ivRecommendedDrink2,
            "https://i.imgur.com/fPcJDwr.png"
        )
    }

    private fun loadImageFromUrl(imageView: ImageView, imageUrl: String) {
        Thread {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                imageView.post {
                    // Nếu load lỗi, giữ nguyên placeholder
                    imageView.setImageResource(R.drawable.placeholder_coffee)
                }
            }
        }.start()
    }
}
