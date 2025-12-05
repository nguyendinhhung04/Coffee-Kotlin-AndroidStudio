package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item
import com.example.coffeeshop.data.model.Size
import com.example.coffeeshop.data.model.TempOption
import com.example.coffeeshop.data.model.Topping
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale

class AdminAddMenuItemActivity : AppCompatActivity() {

    private lateinit var etItemName: TextInputEditText
    private lateinit var etItemCategory: TextInputEditText
    private lateinit var etItemImageUrl: TextInputEditText
    private lateinit var etItemBasePrice: TextInputEditText
    private lateinit var etItemDescription: TextInputEditText
    private lateinit var etItemSizes: TextInputEditText
    private lateinit var etItemTempOptions: TextInputEditText
    private lateinit var etItemIceLevels: TextInputEditText
    private lateinit var etItemSugarLevels: TextInputEditText
    private lateinit var etItemToppings: TextInputEditText
    private lateinit var btnAddItem: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_add_menu_item)

        initViews()
        btnAddItem.setOnClickListener { createNewItem() }
    }

    private fun initViews() {
        etItemName = findViewById(R.id.etItemName)
        etItemCategory = findViewById(R.id.etItemCategory)
        etItemImageUrl = findViewById(R.id.etItemImageUrl)
        etItemBasePrice = findViewById(R.id.etItemBasePrice)
        etItemDescription = findViewById(R.id.etItemDescription)
        etItemSizes = findViewById(R.id.etItemSizes)
        etItemTempOptions = findViewById(R.id.etItemTempOptions)
        etItemIceLevels = findViewById(R.id.etItemIceLevels)
        etItemSugarLevels = findViewById(R.id.etItemSugarLevels)
        etItemToppings = findViewById(R.id.etItemToppings)
        btnAddItem = findViewById(R.id.btnAddItem)
    }

    private fun createNewItem() {
        val name = etItemName.text.toString().trim()
        val category = etItemCategory.text.toString().trim()
        val imageUrl = etItemImageUrl.text.toString().trim()
        val basePriceString = etItemBasePrice.text.toString().trim()
        val description = etItemDescription.text.toString().trim()
        val sizesString = etItemSizes.text.toString().trim()
        val tempOptionsString = etItemTempOptions.text.toString().trim()
        val iceLevelsString = etItemIceLevels.text.toString().trim()
        val sugarLevelsString = etItemSugarLevels.text.toString().trim()
        val toppingsString = etItemToppings.text.toString().trim()

        if (name.isEmpty() || category.isEmpty() || basePriceString.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ các trường bắt buộc (Tên, Danh mục, Giá cơ bản).", Toast.LENGTH_LONG).show()
            return
        }

        val basePrice = basePriceString.toDoubleOrNull()
        if (basePrice == null) {
            Toast.makeText(this, "Giá cơ bản không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        val sizes = parseSizesString(sizesString)
        val tempOptions = parseTempOptionsString(tempOptionsString)
        val iceLevels = parseStringList(iceLevelsString)
        val sugarLevels = parseStringList(sugarLevelsString)
        val toppings = parseToppingsString(toppingsString)

        val newItem = Item(
            _id = null, // Backend will generate this
            name = name,
            categories = listOf(category), // Convert single category to list
            image_url = imageUrl,
            basePrice = basePrice,
            description = description,
            sizes = sizes,
            tempOptions = tempOptions,
            iceLevels = iceLevels,
            sugarLevels = sugarLevels,
            toppings = toppings,
            isActive = true
        )

        ItemDAO.createItem(newItem) { success, message, item ->
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

    private fun parseSizesString(sizesString: String): List<Size> {
        if (sizesString.isEmpty()) return emptyList()
        return sizesString.split(",").mapNotNull { part ->
            val segments = part.split(":")
            if (segments.size == 2) {
                val name = segments[0].trim()
                val modifier = segments[1].trim().toDoubleOrNull()
                if (modifier != null) {
                    Size(name, modifier, name.uppercase(Locale.ROOT)) // Label can be same as name for simplicity
                } else null
            } else null
        }
    }

    private fun parseTempOptionsString(tempOptionsString: String): List<TempOption> {
        if (tempOptionsString.isEmpty()) return emptyList()
        return tempOptionsString.split(",").mapNotNull { part ->
            val segments = part.split(":")
            if (segments.size == 2) {
                val name = segments[0].trim()
                val modifier = segments[1].trim().toDoubleOrNull()
                if (modifier != null) {
                    TempOption(name, modifier, name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }) // Capitalize first letter
                } else null
            } else null
        }
    }

    private fun parseToppingsString(toppingsString: String): List<Topping> {
        if (toppingsString.isEmpty()) return emptyList()
        return toppingsString.split(",").mapNotNull { part ->
            val segments = part.split(":")
            if (segments.size == 2) {
                val name = segments[0].trim()
                val price = segments[1].trim().toDoubleOrNull()
                if (price != null) {
                    Topping(name, price)
                } else null
            } else null
        }
    }

    private fun parseStringList(listString: String): List<String> {
        if (listString.isEmpty()) return emptyList()
        return listString.split(",").map { it.trim() }
    }
}
