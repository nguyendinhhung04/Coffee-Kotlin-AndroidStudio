package com.example.coffeeshop.data.repo

import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item

object ItemRepository {

    private var cachedItems: List<Item> = emptyList()

    fun setItems(items: List<Item>) {
        cachedItems = items
    }

    fun getItemById(id: String): Item? {
        return cachedItems.find { it._id == id }
    }

    fun refreshItems(callback: (Boolean, String, List<Item>?) -> Unit) {
        ItemDAO.getAllItems { success, message, items ->
            if (success && items != null) {
                cachedItems = items
            }
            callback(success, message, items)
        }
    }

    fun isCached(): Boolean = cachedItems.isNotEmpty()
}
