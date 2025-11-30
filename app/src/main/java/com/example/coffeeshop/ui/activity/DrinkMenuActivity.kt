package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
<<<<<<< Updated upstream
=======
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.utils.SeasonHelper
import com.example.coffeeshop.utils.Season
import com.example.coffeeshop.utils.WeatherHelper
import com.example.coffeeshop.utils.WeatherType
>>>>>>> Stashed changes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnCoffee: Button
<<<<<<< Updated upstream
    private lateinit var btnChocolate: Button
    private lateinit var btnOthers: Button
    private lateinit var fabAddOrder1: FloatingActionButton
=======
    private lateinit var btnTea: Button
    private lateinit var btnCake: Button
    private lateinit var btnSeasonal: Button
    private lateinit var btnOthers: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddOrder: FloatingActionButton
    private lateinit var bottomNav: BottomNavigationView

    private val adapter = ItemAdapter { item ->
        Toast.makeText(this, "Đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
    }

    private var allItems: List<Item> = emptyList()
    private var useWeatherMode = false // false = dùng mùa, true = dùng thời tiết
>>>>>>> Stashed changes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

<<<<<<< Updated upstream
        bottomNavigationView = findViewById(R.id.bottom_navigation_drink_menu)
        btnCoffee = findViewById(R.id.btnCoffee)
        btnChocolate = findViewById(R.id.btnChocolate)
=======
        initViews()
        setupRecyclerView()
        setupButtons()
        setupBottomNav()
        updateSeasonalButtonText()
        loadAllItemsOnce()
    }

    private fun initViews() {
        btnCoffee = findViewById(R.id.btnCoffee)
        btnTea = findViewById(R.id.btnTea)
        btnCake = findViewById(R.id.btnCake)
        btnSeasonal = findViewById(R.id.btnSeasonal)
>>>>>>> Stashed changes
        btnOthers = findViewById(R.id.btnOthers)
        fabAddOrder1 = findViewById(R.id.fabAddOrder1)

        setupBottomNavigationView()
        setupFilterButtons()

        // Simulate clicking the Coffee button initially
        btnCoffee.performClick()

        fabAddOrder1.setOnClickListener {
            Toast.makeText(this, "Coffee selected", Toast.LENGTH_SHORT).show()
        }
    }

<<<<<<< Updated upstream
    private fun setupBottomNavigationView() {
        bottomNavigationView.selectedItemId = R.id.navigation_drink_menu // Highlight "Drink Menu"
        bottomNavigationView.setOnItemSelectedListener {
=======
    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnCoffee.setOnClickListener { filterAndShow("Coffee", "Specialty Coffee", "Modern Coffee") }
        btnTea.setOnClickListener { filterAndShow("Tea", "Milk Tea") }
        btnCake.setOnClickListener { filterAndShow("Cake") }
        btnSeasonal.setOnClickListener { showSeasonalRecommendations() }
        btnSeasonal.setOnLongClickListener { 
            showSeasonTestDialog()
            true 
        }
        btnOthers.setOnClickListener { filterAndShow("all") }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.navigation_drink_menu
        bottomNav.setOnItemSelectedListener {
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
        // Set selected button's state
        selectedButton.setBackgroundResource(R.color.brown)
        selectedButton.setTextColor(resources.getColor(R.color.white))

        // TODO: Filter drink items based on selected category
        Toast.makeText(this, "${selectedButton.text} selected", Toast.LENGTH_SHORT).show()
=======
        // Highlight nút
        listOf(btnCoffee, btnTea, btnCake, btnSeasonal, btnOthers).forEach {
            it.setBackgroundResource(R.color.backgroundLight)
            it.setTextColor(resources.getColor(R.color.dark_brown, theme))
        }
        when {
            categories.any { it.contains("Coffee", ignoreCase = true) } -> {
                btnCoffee.setBackgroundResource(R.color.brown)
                btnCoffee.setTextColor(resources.getColor(R.color.white, theme))
            }
            categories.any { it.contains("Tea", ignoreCase = true) } -> {
                btnTea.setBackgroundResource(R.color.brown)
                btnTea.setTextColor(resources.getColor(R.color.white, theme))
            }
            categories.any { it.contains("Cake", ignoreCase = true) } -> {
                btnCake.setBackgroundResource(R.color.brown)
                btnCake.setTextColor(resources.getColor(R.color.white, theme))
            }
            else -> {
                btnOthers.setBackgroundResource(R.color.brown)
                btnOthers.setTextColor(resources.getColor(R.color.white, theme))
            }
        }
    }

    private fun updateSeasonalButtonText() {
        if (useWeatherMode) {
            val weather = WeatherHelper.getCurrentWeatherType()
            if (weather != null) {
                btnSeasonal.text = WeatherHelper.getWeatherName(weather)
            } else {
                btnSeasonal.text = "Thời tiết"
            }
        } else {
            val currentSeason = SeasonHelper.getCurrentSeason()
            val seasonName = SeasonHelper.getSeasonName(currentSeason)
            btnSeasonal.text = "Mùa $seasonName"
        }
    }

    private fun showSeasonalRecommendations() {
        // Highlight nút Gợi ý
        listOf(btnCoffee, btnTea, btnCake, btnSeasonal, btnOthers).forEach {
            it.setBackgroundResource(R.color.backgroundLight)
            it.setTextColor(resources.getColor(R.color.dark_brown, theme))
        }
        btnSeasonal.setBackgroundResource(R.color.brown)
        btnSeasonal.setTextColor(resources.getColor(R.color.white, theme))

        if (useWeatherMode) {
            // Dùng thời tiết
            val weather = WeatherHelper.getCurrentWeatherType()
            if (weather != null) {
                val weatherKeywords = WeatherHelper.getWeatherKeywords(weather)
                val weatherName = WeatherHelper.getWeatherName(weather)
                
                ItemDAO.getRecommendedItemsByWeather(weatherKeywords) { success, message, items ->
                    runOnUiThread {
                        if (success && items != null && items.isNotEmpty()) {
                            adapter.submitList(items)
                            Toast.makeText(this, "Gợi ý thời tiết ${weatherName}: ${items.size} món", Toast.LENGTH_SHORT).show()
                        } else {
                            adapter.submitList(allItems)
                            Toast.makeText(this, "Không có món gợi ý cho thời tiết ${weatherName}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // Chưa có thời tiết, load từ API
                WeatherHelper.getCurrentWeather("Ho Chi Minh") { weather, message ->
                    runOnUiThread {
                        if (weather != null) {
                            val weatherKeywords = WeatherHelper.getWeatherKeywords(weather)
                            val weatherName = WeatherHelper.getWeatherName(weather)
                            
                            ItemDAO.getRecommendedItemsByWeather(weatherKeywords) { success, msg, items ->
                                runOnUiThread {
                                    if (success && items != null && items.isNotEmpty()) {
                                        adapter.submitList(items)
                                        Toast.makeText(this, "Gợi ý thời tiết ${weatherName}: ${items.size} món", Toast.LENGTH_SHORT).show()
                                    } else {
                                        adapter.submitList(allItems)
                                        Toast.makeText(this, "Không có món gợi ý", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            updateSeasonalButtonText()
                        } else {
                            Toast.makeText(this, "Không thể lấy thời tiết: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            // Dùng mùa
            val currentSeason = SeasonHelper.getCurrentSeason()
            val seasonKeywords = SeasonHelper.getSeasonKeywords(currentSeason)
            val seasonName = SeasonHelper.getSeasonName(currentSeason)

            ItemDAO.getRecommendedItemsBySeason(seasonKeywords) { success, message, items ->
                runOnUiThread {
                    if (success && items != null && items.isNotEmpty()) {
                        adapter.submitList(items)
                        Toast.makeText(this, "Gợi ý mùa ${seasonName}: ${items.size} món", Toast.LENGTH_SHORT).show()
                    } else {
                        adapter.submitList(allItems)
                        Toast.makeText(this, "Không có món gợi ý cho mùa ${seasonName}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSeasonTestDialog() {
        val options = arrayOf(
            "Dùng Mùa (Xuân/Hè/Thu/Đông)",
            "Dùng Thời Tiết (Nắng/Mưa/Lạnh...)",
            "Test Mùa",
            "Test Thời Tiết"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn chế độ gợi ý")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // Dùng mùa thật
                        useWeatherMode = false
                        SeasonHelper.setTestSeason(null)
                        updateSeasonalButtonText()
                        showSeasonalRecommendations()
                        Toast.makeText(this, "Đã chuyển sang gợi ý theo mùa", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // Dùng thời tiết thật
                        useWeatherMode = true
                        WeatherHelper.setTestWeather(null)
                        updateSeasonalButtonText()
                        showSeasonalRecommendations()
                        Toast.makeText(this, "Đã chuyển sang gợi ý theo thời tiết", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        // Test mùa
                        useWeatherMode = false
                        showSeasonTestOptions()
                    }
                    3 -> {
                        // Test thời tiết
                        useWeatherMode = true
                        showWeatherTestOptions()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showSeasonTestOptions() {
        val seasons = arrayOf("Xuân", "Hè", "Thu", "Đông", "Tắt test (dùng mùa thật)")
        val seasonValues = arrayOf(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER, null)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn mùa để test")
            .setItems(seasons) { _, which ->
                if (which < 4) {
                    SeasonHelper.setTestSeason(seasonValues[which])
                    Toast.makeText(this, "Đã set mùa test: ${seasons[which]}", Toast.LENGTH_SHORT).show()
                } else {
                    SeasonHelper.setTestSeason(null)
                    Toast.makeText(this, "Đã tắt test, dùng mùa thật", Toast.LENGTH_SHORT).show()
                }
                updateSeasonalButtonText()
                showSeasonalRecommendations()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showWeatherTestOptions() {
        val weathers = arrayOf("Nắng nóng", "Mưa", "Lạnh", "Nhiều mây", "Gió", "Tắt test (dùng thời tiết thật)")
        val weatherValues = arrayOf(WeatherType.SUNNY, WeatherType.RAINY, WeatherType.COLD, WeatherType.CLOUDY, WeatherType.WINDY, null)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn thời tiết để test")
            .setItems(weathers) { _, which ->
                if (which < 5) {
                    WeatherHelper.setTestWeather(weatherValues[which])
                    Toast.makeText(this, "Đã set thời tiết test: ${weathers[which]}", Toast.LENGTH_SHORT).show()
                } else {
                    WeatherHelper.setTestWeather(null)
                    Toast.makeText(this, "Đã tắt test, dùng thời tiết thật", Toast.LENGTH_SHORT).show()
                }
                updateSeasonalButtonText()
                showSeasonalRecommendations()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

// ============================= ITEM ADAPTER =============================
class ItemAdapter(private val onClick: (Item) -> Unit) :
    ListAdapter<Item, ItemAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drink_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvDrinkName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvDrinkPrice)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivDrinkImage)

        fun bind(item: Item) {
            tvName.text = item.name
            tvPrice.text = "${item.basePrice.toInt()}đ"

            // Load ảnh bằng Thread
            Thread {
                try {
                    val bitmap = BitmapFactory.decodeStream(URL(item.image_url).openStream())
                    itemView.post { ivImage.setImageBitmap(bitmap) }
                } catch (e: Exception) {
                    itemView.post { ivImage.setImageResource(R.drawable.ic_error) }
                }
            }.start()

            itemView.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item) = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: Item, newItem: Item) = oldItem == newItem
>>>>>>> Stashed changes
    }
}
