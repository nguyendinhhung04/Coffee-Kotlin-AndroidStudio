package com.example.coffeeshop.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.data.model.Item // <-- THÊM IMPORT NÀY

class ItemFragment : Fragment() {

    private val TAG = "ItemFragment" // Tag để log lỗi cho dễ debug

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item, container, false)

        // Gọi hàm để lấy dữ liệu khi view được tạo
        fetchAllItems()

        return view
    }

    /**
     * Hàm lấy tất cả item từ DAO và xử lý kết quả.
     */
    private fun fetchAllItems() {
        // Sử dụng callback đã được tùy chỉnh từ ItemDAO
        ItemDAO.getAllItems { success, items, message ->
            // Luôn đảm bảo rằng việc cập nhật UI được thực hiện trên Main Thread
            // requireActivity().runOnUiThread là cách an toàn để làm điều này trong Fragment
            requireActivity().runOnUiThread {
                if (success && items != null) {
                    // Thành công: items là một List<Item> đã sẵn sàng để sử dụng
                    Toast.makeText(
                        requireContext(),
                        "Successfully loaded ${items} items.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // TODO: Cập nhật dữ liệu vào RecyclerView Adapter của bạn ở đây
                    // ví dụ: itemAdapter.submitList(items)

                    // Log để kiểm tra dữ liệu
                    items.forEach { Log.d(TAG, "Item: ${it}") }

                } else {
                    // Thất bại: Hiển thị thông báo lỗi
                    Toast.makeText(
                        requireContext(),
                        "Error fetching items: $message",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e(TAG, "Failed to fetch items: $message")
                }
            }
        }
    }
}