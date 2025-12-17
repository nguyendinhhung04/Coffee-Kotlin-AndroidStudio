package com.example.coffeeshop.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
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
    private lateinit var llDiscount: LinearLayout
    private lateinit var tvDiscountAmount: TextView

    private lateinit var sessionManager: UserSessionManager
    private val orderApi: OrderApi = ApiClient.orderApi

    private var onCheckoutClick: (() -> Unit)? = null
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
        updateCartUI()
        setupPointsSystem()

        btnCheckout.setOnClickListener {
            if (CartManager.isEmpty()) {
                Toast.makeText(context, "Giỏ hàng trống", Toast.LENGTH_SHORT).show()
            } else {
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
                            CartManager.clearCart()
                            onCheckoutClick?.invoke()
                            dismiss()
                            val intent = Intent(requireContext(), YourOrderActivity::class.java)
                            startActivity(intent)
                        } else {

                            Toast.makeText(context, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
        } else {
            tvEmptyCart.visibility = View.GONE
            rvCartItems.visibility = View.VISIBLE
            btnCheckout.isEnabled = true
            btnClear.isEnabled = true

            tvSubtotal.text = formatter.format(subtotal)
            calculateTotal(subtotal)
        }
    }

    private fun setupPointsSystem() {
        val availablePoints = sessionManager.getLoyaltyPoints()
        tvAvailablePoints.text = "Sử dụng điểm (hiện có: $availablePoints)"

        cbUsePoints.setOnCheckedChangeListener { _, isChecked ->
            calculateTotal(CartManager.getSubtotal())
        }
    }

    private fun calculateTotal(subtotal: Double) {
        val availablePoints = sessionManager.getLoyaltyPoints()
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        var total = subtotal

        if (cbUsePoints.isChecked) {
            val maxDiscountFromPoints = availablePoints * 10000.0
            val discount = subtotal.coerceAtMost(maxDiscountFromPoints)

            usedPointAmount = (discount / 10000.0).toInt()
            discountByPointAmount = discount

            total -= discount

            tvDiscountAmount.text = "-${formatter.format(discount)}"
            llDiscount.visibility = View.VISIBLE
        } else {
            usedPointAmount = 0
            discountByPointAmount = 0.0
            llDiscount.visibility = View.GONE
        }

        tvTotal.text = formatter.format(total)
    }

    fun setOnCheckoutClickListener(listener: () -> Unit) {
        onCheckoutClick = listener
    }

    companion object {
        fun newInstance(): CartBottomSheetFragment = CartBottomSheetFragment()
    }
}