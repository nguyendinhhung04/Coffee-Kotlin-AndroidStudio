package com.example.coffeeshop.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.R
import com.example.coffeeshop.data.api.ApiClient
import com.example.coffeeshop.data.api.OrderApi
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.data.model.buildOrderDTOFromCart
import com.example.coffeeshop.ui.activity.YourOrderActivity
import com.example.coffeeshop.ui.adapter.CartItemAdapter
import com.example.coffeeshop.utils.CartManager
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min
import kotlin.math.ceil

class CartBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var btnClear: Button
    private lateinit var tvEmptyCart: TextView
    private lateinit var cartAdapter: CartItemAdapter

    private lateinit var cbUsePoints: CheckBox
    private lateinit var tvAvailablePoints: TextView
    
    // Các view cho phần nhập điểm
    private lateinit var llInputPoints: LinearLayout
    private lateinit var etPointsToUse: EditText
    private lateinit var btnMinusPoint: Button
    private lateinit var btnPlusPoint: Button
    private lateinit var tvMaxPointsHint: TextView
    private lateinit var tvPointConversionRate: TextView
    
    private lateinit var llDiscount: LinearLayout
    private lateinit var tvDiscountAmount: TextView

    private lateinit var sessionManager: UserSessionManager
    private val orderApi: OrderApi = ApiClient.orderApi

    private var onCheckoutClick: (() -> Unit)? = null
    
    // Biến lưu trạng thái hiện tại
    private var usedPointAmount = 0
    private var discountByPointAmount = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = UserSessionManager(requireContext())

        initViews(view)
        setupRecyclerView()
        setupPointsSystem()
        // Gọi updateCartUI sau khi đã setup xong các view và listener
        updateCartUI()

        btnCheckout.setOnClickListener {
            handleCheckout()
        }

        btnClear.setOnClickListener {
            CartManager.clearCart()
            updateCartUI()
            Toast.makeText(context, "Đã xóa giỏ hàng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews(view: View) {
        rvCartItems = view.findViewById(R.id.rvCartItems)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTotal = view.findViewById(R.id.tvTotal)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        btnClear = view.findViewById(R.id.btnClear)
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart)
        
        cbUsePoints = view.findViewById(R.id.cbUsePoints)
        tvAvailablePoints = view.findViewById(R.id.tvAvailablePoints)
        
        llInputPoints = view.findViewById(R.id.llInputPoints)
        etPointsToUse = view.findViewById(R.id.etPointsToUse)
        btnMinusPoint = view.findViewById(R.id.btnMinusPoint)
        btnPlusPoint = view.findViewById(R.id.btnPlusPoint)
        tvMaxPointsHint = view.findViewById(R.id.tvMaxPointsHint)
        tvPointConversionRate = view.findViewById(R.id.tvPointConversionRate)
        
        llDiscount = view.findViewById(R.id.llDiscount)
        tvDiscountAmount = view.findViewById(R.id.tvDiscountAmount)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartItemAdapter(
            CartManager.getAllItems(),
            onPlusClick = { cartItem -> changeQuantity(cartItem, +1) },
            onMinusClick = { cartItem -> changeQuantity(cartItem, -1) },
            onDeleteClick = { cartItem -> deleteItem(cartItem) },
            onEditClick = { cartItem -> editCartItem(cartItem) }
        )

        rvCartItems.layoutManager = LinearLayoutManager(context)
        rvCartItems.adapter = cartAdapter
    }

    private fun changeQuantity(cartItem: CartItem, delta: Int) {
        val newQty = cartItem.quantity + delta
        CartManager.updateQuantity(cartItem.item._id, cartItem.customizations, newQty)
        refreshCart()
    }

    private fun deleteItem(cartItem: CartItem) {
        CartManager.removeItem(cartItem.item._id, cartItem.customizations)
        refreshCart()
        Toast.makeText(context, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
    }

    private fun editCartItem(cartItem: CartItem) {
        val dialog = ItemCustomizationDialogFragment.newInstance(cartItem.item)
        dialog.setOnAddToCartListener { newCartItem ->
            CartManager.removeItem(cartItem.item._id, cartItem.customizations)
            CartManager.addItem(newCartItem)
            refreshCart()
        }
        dialog.show(parentFragmentManager, "edit_cart_item_dialog")
    }

    private fun refreshCart() {
        cartAdapter.updateItems(CartManager.getAllItems())
        updateCartUI()
    }

    private fun setupPointsSystem() {
        val availablePoints = sessionManager.getLoyaltyPoints()
        tvAvailablePoints.text = "Sử dụng điểm (hiện có: $availablePoints)"

        // 1. Xử lý khi click vào CheckBox
        cbUsePoints.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                llInputPoints.visibility = View.VISIBLE
                tvPointConversionRate.visibility = View.VISIBLE
                
                // Tính toán số điểm tối đa có thể dùng cho đơn hàng này
                val subtotal = CartManager.getSubtotal()
                val maxPointsNeeded = ceil(subtotal / 10000.0).toInt()
                val maxPointsCanUse = min(availablePoints, maxPointsNeeded)
                
                tvMaxPointsHint.text = "/ $maxPointsCanUse điểm"
                
                // Mặc định điền max số điểm
                etPointsToUse.setText(maxPointsCanUse.toString())
                etPointsToUse.setSelection(etPointsToUse.text.length)
            } else {
                llInputPoints.visibility = View.GONE
                tvPointConversionRate.visibility = View.GONE
                
                usedPointAmount = 0
                discountByPointAmount = 0.0
                calculateTotal()
            }
        }

        // 2. Xử lý khi nhập số điểm
        etPointsToUse.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!cbUsePoints.isChecked) return
                
                val inputStr = s.toString()
                if (inputStr.isNotEmpty()) {
                    try {
                        val pointsInput = inputStr.toInt()
                        
                        val subtotal = CartManager.getSubtotal()
                        val maxPointsNeeded = ceil(subtotal / 10000.0).toInt()
                        val maxPointsCanUse = min(availablePoints, maxPointsNeeded)

                        if (pointsInput > maxPointsCanUse) {
                            // Nếu nhập quá lố, reset về max
                            etPointsToUse.setText(maxPointsCanUse.toString())
                            etPointsToUse.setSelection(etPointsToUse.text.length)
                            usedPointAmount = maxPointsCanUse
                        } else {
                            usedPointAmount = pointsInput
                        }
                    } catch (e: NumberFormatException) {
                        usedPointAmount = 0
                    }
                } else {
                    usedPointAmount = 0
                }
                calculateTotal()
            }
        })

        // 3. Xử lý nút tăng giảm điểm
        btnPlusPoint.setOnClickListener {
            val current = try { etPointsToUse.text.toString().toInt() } catch (e: Exception) { 0 }
            
            val subtotal = CartManager.getSubtotal()
            val maxPointsNeeded = ceil(subtotal / 10000.0).toInt()
            val maxPointsCanUse = min(availablePoints, maxPointsNeeded)

            if (current < maxPointsCanUse) {
                etPointsToUse.setText((current + 1).toString())
                etPointsToUse.setSelection(etPointsToUse.text.length)
            }
        }

        btnMinusPoint.setOnClickListener {
            val current = try { etPointsToUse.text.toString().toInt() } catch (e: Exception) { 0 }
            if (current > 0) {
                etPointsToUse.setText((current - 1).toString())
                etPointsToUse.setSelection(etPointsToUse.text.length)
            }
        }
    }

    private fun updateCartUI() {
        val items = CartManager.getAllItems()
        val subtotal = CartManager.getSubtotal()
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        if (items.isEmpty()) {
            tvEmptyCart.visibility = View.VISIBLE
            rvCartItems.visibility = View.GONE
            btnCheckout.isEnabled = false
            btnClear.isEnabled = false
            tvSubtotal.text = "0 VND"
            tvTotal.text = "0 VND"
            
            // Disable phần điểm
            cbUsePoints.isEnabled = false
            cbUsePoints.isChecked = false
            llInputPoints.visibility = View.GONE
        } else {
            tvEmptyCart.visibility = View.GONE
            rvCartItems.visibility = View.VISIBLE
            btnCheckout.isEnabled = true
            btnClear.isEnabled = true
            cbUsePoints.isEnabled = true

            tvSubtotal.text = formatter.format(subtotal)
            
            // Nếu đang check dùng điểm, cập nhật lại gợi ý max points (vì tổng tiền có thể đã đổi)
            if (cbUsePoints.isChecked) {
                val availablePoints = sessionManager.getLoyaltyPoints()
                val maxPointsNeeded = ceil(subtotal / 10000.0).toInt()
                val maxPointsCanUse = min(availablePoints, maxPointsNeeded)
                
                tvMaxPointsHint.text = "/ $maxPointsCanUse điểm"
                
                // Kiểm tra lại nếu số điểm đang nhập vượt quá giới hạn mới
                if (usedPointAmount > maxPointsCanUse) {
                    etPointsToUse.setText(maxPointsCanUse.toString())
                } else {
                    // Trigger tính toán lại
                    calculateTotal()
                }
            } else {
                calculateTotal()
            }
        }
    }

    private fun calculateTotal() {
        val subtotal = CartManager.getSubtotal()
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        var total = subtotal

        if (cbUsePoints.isChecked && usedPointAmount > 0) {
            val discount = usedPointAmount * 10000.0
            
            // Đảm bảo discount không vượt quá subtotal
            val finalDiscount = if (discount > subtotal) subtotal else discount
            
            discountByPointAmount = finalDiscount
            total -= finalDiscount

            tvDiscountAmount.text = "-${formatter.format(finalDiscount)}"
            llDiscount.visibility = View.VISIBLE
        } else {
            discountByPointAmount = 0.0
            llDiscount.visibility = View.GONE
        }

        if (total < 0) total = 0.0
        tvTotal.text = formatter.format(total)
    }

    private fun handleCheckout() {
        if (CartManager.isEmpty()) {
            Toast.makeText(context, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            if (userId.isNullOrEmpty()) {
                Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val address = mapOf(
                "fullName" to sessionManager.getFullName().orEmpty(),
                "phone" to sessionManager.getPhone().orEmpty(),
                "street" to "",
                "ward" to "",
                "district" to "",
                "city" to "TP. Hồ Chí Minh"
            )

            val orderDTO = buildOrderDTOFromCart(
                userId = userId,
                status = "Pending",
                paymentMethod = "COD",
                deliveryAddress = address,
                usedPointAmount = usedPointAmount,
                discountByPointAmount = discountByPointAmount
            )

            try {
                val response = orderApi.createOrder(orderDTO)
                if (response.isSuccessful) {
                    // Trừ điểm trong session tạm thời để hiển thị đúng khi quay lại
                    val currentPoints = sessionManager.getLoyaltyPoints()
                    sessionManager.saveLoyaltyPoints(currentPoints - usedPointAmount)

                    CartManager.clearCart()
                    onCheckoutClick?.invoke()
                    dismiss()
                    val intent = Intent(requireContext(), YourOrderActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Lỗi server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setOnCheckoutClickListener(listener: () -> Unit) {
        onCheckoutClick = listener
    }

    companion object {
        fun newInstance(): CartBottomSheetFragment = CartBottomSheetFragment()
    }
}
