package com.example.coffeeshop.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ItemFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item, container, false)

        // 🟢 Example: Add new item
        val newItem = JSONObject().apply {
            put("name", "Latte")
            put("category", "Coffee")
            put("image", "https://example.com/latte.jpg")
            put("price", 49000)
            put("description", "Smooth and creamy latte ☕")
        }

        ItemDAO.createItem(newItem, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful)
                        Toast.makeText(requireContext(), "✅ Item added!", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(requireContext(), "❌ Failed: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // 🔵 Example: Fetch all items
        ItemDAO.getAllItems(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Fetch failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val arr = JSONArray(json)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Loaded ${arr.length()} items", Toast.LENGTH_SHORT).show()
                }
            }
        })

        return view
    }
}
