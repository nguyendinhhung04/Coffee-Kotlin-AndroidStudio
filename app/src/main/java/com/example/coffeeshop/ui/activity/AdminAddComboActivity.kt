package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Combo
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.OrderItem
import com.example.coffeeshop.data.model.Topping
import com.example.coffeeshop.ui.adapter.ItemListAdapter
import com.example.coffeeshop.ui.adapter.OrderItemAdapter
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class AdminAddComboActivity : AppCompatActivity() {

    private lateinit var etComboName: TextInputEditText
    private lateinit var etComboDescription: TextInputEditText
    private lateinit var etComboImageUrl: TextInputEditText
    private lateinit var etComboBasePrice: TextInputEditText
    private lateinit var rvAvailableItems: RecyclerView
    private lateinit var rvSelectedItems: RecyclerView
    private lateinit var tvSelectedItemsCount: TextView
    private lateinit var btnAddCombo: Button

    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var selectedItemsAdapter: OrderItemAdapter
    private val selectedComboItems = mutableListOf<OrderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_combo)

        initViews()
        setupRecyclerView()
        loadAvailableItems()
        btnAddCombo.setOnClickListener { createNewCombo() }
    }

    private fun initViews() {
        etComboName = findViewById(R.id.etComboName)
        etComboDescription = findViewById(R.id.etComboDescription)
        etComboImageUrl = findViewById(R.id.etComboImageUrl)
        etComboBasePrice = findViewById(R.id.etComboBasePrice)
        rvAvailableItems = findViewById(R.id.rvAvailableItems)
        rvSelectedItems = findViewById(R.id.rvSelectedItems)
        tvSelectedItemsCount = findViewById(R.id.tvSelectedItemsCount)
        btnAddCombo = findViewById(R.id.btnAddCombo)
    }

    private fun setupRecyclerView() {
        itemListAdapter = ItemListAdapter(emptyList()) { item ->
            // When an item is clicked, add it to the selectedComboItems
            addOrUpdateComboItem(item)
        }
        rvAvailableItems.apply {
            layoutManager = LinearLayoutManager(this@AdminAddComboActivity)
            adapter = itemListAdapter
            isNestedScrollingEnabled = false // To allow scrolling of parent ScrollView
        }

        selectedItemsAdapter = OrderItemAdapter(emptyList())
        rvSelectedItems.apply {
            layoutManager = LinearLayoutManager(this@AdminAddComboActivity)
            adapter = selectedItemsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun loadAvailableItems() {
        ItemDAO.getAllItems { success, message, items ->
            runOnUiThread {
                if (success && items != null) {
                    itemListAdapter.updateItems(items)
                } else {
                    Toast.makeText(this, "Không thể tải danh sách món: $message", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addOrUpdateComboItem(item: Item) {
        val existingOrderItem = selectedComboItems.find { it.productId == item._id }
        if (existingOrderItem != null) {
            // For simplicity, just increment quantity. A more complex UI would allow full customization.
            val updatedOrderItem = existingOrderItem.copy(quantity = existingOrderItem.quantity + 1)
            val index = selectedComboItems.indexOf(existingOrderItem)
            selectedComboItems[index] = updatedOrderItem
            Toast.makeText(this, "Đã cập nhật số lượng ${item.name} trong combo.", Toast.LENGTH_SHORT).show()
        } else {
            // Create a basic OrderItem from the selected Item
            val newOrderItem = OrderItem(
                productId = item._id!!,
                productName = item.name,
                quantity = 1,
                finalUnitPrice = item.basePrice,
                sizeChosen = item.sizes.firstOrNull()?.name ?: "",
                tempChosen = item.tempOptions.firstOrNull()?.name ?: "",
                iceLevel = item.iceLevels.firstOrNull() ?: "N/A",
                sugarLevel = item.sugarLevels.firstOrNull() ?: "N/A",
                chosenToppings = emptyList(),
                itemNote = ""
            )
            selectedComboItems.add(newOrderItem)
            Toast.makeText(this, "Đã thêm ${item.name} vào combo.", Toast.LENGTH_SHORT).show()
        }
        updateSelectedItemsDisplay()
    }

    private fun updateSelectedItemsDisplay() {
        selectedItemsAdapter.updateOrderItems(selectedComboItems)
        tvSelectedItemsCount.text = "Đã chọn: ${selectedComboItems.size} món"
        if (selectedComboItems.isNotEmpty()) {
            rvSelectedItems.visibility = View.VISIBLE
        } else {
            rvSelectedItems.visibility = View.GONE
        }
    }

    private fun createNewCombo() {
        val name = etComboName.text.toString().trim()
        val description = etComboDescription.text.toString().trim()
        val imageUrl = etComboImageUrl.text.toString().trim()
        val basePriceString = etComboBasePrice.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || imageUrl.isEmpty() || basePriceString.isEmpty() || selectedComboItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin combo và chọn ít nhất một món.", Toast.LENGTH_LONG).show()
            return
        }

        val basePrice = basePriceString.toDoubleOrNull()
        if (basePrice == null) {
            Toast.makeText(this, "Giá cơ bản không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        val newCombo = Combo(
            _id = null,
            name = name,
            description = description,
            image_url = imageUrl,
            basePrice = basePrice,
            items = selectedComboItems.toList(),
            isActive = true
        )

        ItemDAO.createCombo(newCombo) { success, message, combo ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    finish() // Close activity after successful creation
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
