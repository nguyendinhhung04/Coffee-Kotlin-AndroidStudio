package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import java.net.URL

class DrinkMenuActivity : AppCompatActivity() {

    private lateinit var btnCoffee: Button
    private lateinit var btnTea: Button
    private lateinit var btnCake: Button
    private lateinit var btnOthers: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddOrder: FloatingActionButton
    private lateinit var bottomNav: BottomNavigationView

    private val adapter = ItemAdapter { item ->
        Toast.makeText(this, "Đã chọn: ${item.name}", Toast.LENGTH_SHORT).show()
    }

    private var allItems: List<Item> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink_menu)

        initViews()
        setupRecyclerView()
        setupButtons()
        setupBottomNav()
        loadAllItemsOnce()
    }

    private fun initViews() {
        btnCoffee = findViewById(R.id.btnCoffee)
        btnTea = findViewById(R.id.btnTea)
        btnCake = findViewById(R.id.btnCake)
        btnOthers = findViewById(R.id.btnOthers)
        recyclerView = findViewById(R.id.recyclerViewMenu)
        fabAddOrder = findViewById(R.id.fabAddOrder1)
        bottomNav = findViewById(R.id.bottom_navigation_drink_menu)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnCoffee.setOnClickListener { filterAndShow("Coffee", "Specialty Coffee", "Modern Coffee") }
        btnTea.setOnClickListener { filterAndShow("Tea", "Milk Tea") }
        btnCake.setOnClickListener { filterAndShow("Cake") }
        btnOthers.setOnClickListener { filterAndShow("all") }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.navigation_drink_menu
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.navigation_your_order -> startActivity(Intent(this, YourOrderActivity::class.java))
                R.id.navigation_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
            }
            finish()
            true
        }
    }

    private fun loadAllItemsOnce() {
        ItemDAO.getAllItems { success, _, items ->
            if (success && items != null) {
                allItems = items
                runOnUiThread {
                    adapter.submitList(allItems)
                    // Mặc định hiện Cà phê
                    filterAndShow("Coffee", "Specialty Coffee", "Modern Coffee")
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun filterAndShow(vararg categories: String) {
        val filtered = if (categories.contains("all")) {
            allItems
        } else {
            allItems.filter { item ->
                categories.any { cat -> item.category.contains(cat, ignoreCase = true) }
            }
        }

        adapter.submitList(filtered)

        // Highlight nút
        listOf(btnCoffee, btnTea, btnCake, btnOthers).forEach {
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
    }
}
