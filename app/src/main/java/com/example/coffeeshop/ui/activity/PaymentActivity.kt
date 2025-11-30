package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import android.widget.LinearLayout
import android.widget.Toast
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.utils.SeasonHelper
import com.example.coffeeshop.utils.Season
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.net.URL

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var ivRecommendedDrink1: ImageView
    private lateinit var ivRecommendedDrink2: ImageView
    private lateinit var tvDrinkName1: TextView
    private lateinit var tvDrinkName2: TextView
    private lateinit var tvDrinkPrice1: TextView
    private lateinit var tvDrinkPrice2: TextView
    private lateinit var layoutRecommendedDrinks: LinearLayout
    private lateinit var tvNoSavedAddress: TextView
    private lateinit var tvEditAddress: TextView
    
    private lateinit var sharedPreferences: SharedPreferences
    private val ADDRESS_KEY = "saved_delivery_address"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        sharedPreferences = getSharedPreferences("CoffeeShopPrefs", MODE_PRIVATE)
        initViews()
        setupBottomNav()
        loadRecommendedDrinksBySeason()
        loadSavedAddress()
    }

    private fun initViews() {
        ivRecommendedDrink1 = findViewById(R.id.ivRecommendedDrink1)
        ivRecommendedDrink2 = findViewById(R.id.ivRecommendedDrink2)
        tvDrinkName1 = findViewById(R.id.tvRecommendedDrinkName1)
        tvDrinkName2 = findViewById(R.id.tvRecommendedDrinkName2)
        tvDrinkPrice1 = findViewById(R.id.tvRecommendedDrinkPrice1)
        tvDrinkPrice2 = findViewById(R.id.tvRecommendedDrinkPrice2)
        layoutRecommendedDrinks = findViewById(R.id.layoutRecommendedDrinks)
        tvNoSavedAddress = findViewById(R.id.tvNoSavedAddress)
        tvEditAddress = findViewById(R.id.tvEditAddress)
        
        // Long-click vào phần "Other drinks we recommend" để test mùa
        layoutRecommendedDrinks.setOnLongClickListener {
            showSeasonTestDialog()
            true
        }
        
        // Click vào "Edit" để nhập địa chỉ
        tvEditAddress.setOnClickListener {
            showAddressInputDialog()
        }
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

    private fun showSeasonTestDialog() {
        val seasons = arrayOf("Xuân", "Hè", "Thu", "Đông", "Tắt test (dùng mùa thật)")
        val seasonValues = arrayOf(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER, null)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn mùa để test")
            .setItems(seasons) { _, which ->
                if (which < 4) {
                    // Set mùa test
                    SeasonHelper.setTestSeason(seasonValues[which])
                    Toast.makeText(this, "Đã set mùa test: ${seasons[which]}", Toast.LENGTH_SHORT).show()
                } else {
                    // Tắt test, dùng mùa thật
                    SeasonHelper.setTestSeason(null)
                    Toast.makeText(this, "Đã tắt test, dùng mùa thật", Toast.LENGTH_SHORT).show()
                }
                // Reload món gợi ý với mùa mới
                loadRecommendedDrinksBySeason()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun loadSavedAddress() {
        val savedAddress = sharedPreferences.getString(ADDRESS_KEY, null)
        if (savedAddress != null && savedAddress.isNotEmpty()) {
            tvNoSavedAddress.text = savedAddress
        } else {
            tvNoSavedAddress.text = "No saved address"
        }
    }

    private fun showAddressInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_address_input, null)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val btnSelectFromMap = dialogView.findViewById<android.widget.Button>(R.id.btnSelectFromMap)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        // Load địa chỉ đã lưu (nếu có)
        val savedAddress = sharedPreferences.getString(ADDRESS_KEY, null)
        if (savedAddress != null && savedAddress.isNotEmpty()) {
            etAddress.setText(savedAddress)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Chọn từ bản đồ
        btnSelectFromMap.setOnClickListener {
            openGoogleMaps()
        }

        // Lưu địa chỉ
        btnSave.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                sharedPreferences.edit().putString(ADDRESS_KEY, address).apply()
                tvNoSavedAddress.text = address
                dialog.dismiss()
                Toast.makeText(this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }

        // Hủy
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openGoogleMaps() {
        try {
            // Mở Google Maps với chế độ chọn địa điểm
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            // Nếu không có Google Maps, mở trình duyệt với Google Maps web
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
