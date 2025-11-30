package com.example.coffeeshop.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.coffeeshop.R
import com.example.coffeeshop.data.model.CartItem
import com.example.coffeeshop.data.model.Item

class ItemCustomizationDialogFragment : DialogFragment() {

    private lateinit var item: Item
    private var onAddToCartListener: ((CartItem) -> Unit)? = null

    // Views
    private lateinit var tvItemName: TextView
    private lateinit var tvItemPrice: TextView
    private lateinit var spSize: Spinner
    private lateinit var spTemp: Spinner
    private lateinit var spIceLevel: Spinner
    private lateinit var spSugarLevel: Spinner
    private lateinit var etQuantity: EditText
    private lateinit var etNote: EditText
    private lateinit var btnAddToCart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            item = it.getSerializable("item") as Item
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_customization_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSpinners()
        setupListeners()
    }

    private fun initViews(view: View) {
        tvItemName = view.findViewById(R.id.tvItemName)
        tvItemPrice = view.findViewById(R.id.tvItemPrice)
        spSize = view.findViewById(R.id.spSize)
        spTemp = view.findViewById(R.id.spTemp)
        spIceLevel = view.findViewById(R.id.spIceLevel)
        spSugarLevel = view.findViewById(R.id.spSugarLevel)
        etQuantity = view.findViewById(R.id.etQuantity)
        etNote = view.findViewById(R.id.etNote)
        btnAddToCart = view.findViewById(R.id.btnAddToCart)

        tvItemName.text = item.name
        tvItemPrice.text = "${item.basePrice.toInt()} VND"
        etQuantity.setText("1")
    }

    private fun setupSpinners() {
        // Size
        if (item.sizes.isNotEmpty()) {
            val sizeLabels = item.sizes.map { it.label }
            spSize.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                sizeLabels
            )
            spSize.visibility = View.VISIBLE
        } else {
            spSize.visibility = View.GONE
        }

        // Temperature
        if (item.tempOptions.isNotEmpty()) {
            val tempLabels = item.tempOptions.map { it.label }
            spTemp.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                tempLabels
            )
            spTemp.visibility = View.VISIBLE
        } else {
            spTemp.visibility = View.GONE
        }

        // Ice level
        if (item.iceLevels.isNotEmpty()) {
            spIceLevel.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                item.iceLevels
            )
            spIceLevel.visibility = View.VISIBLE
        } else {
            spIceLevel.visibility = View.GONE
        }

        // Sugar level
        if (item.sugarLevels.isNotEmpty()) {
            spSugarLevel.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                item.sugarLevels
            )
            spSugarLevel.visibility = View.VISIBLE
        } else {
            spSugarLevel.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        btnAddToCart.setOnClickListener {
            val quantity = etQuantity.text.toString().toIntOrNull() ?: 1
            if (quantity <= 0) {
                Toast.makeText(context, "Vui lòng nhập số lượng hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sizeChosen = if (item.sizes.isNotEmpty()) {
                item.sizes.getOrNull(spSize.selectedItemPosition)?.name ?: ""
            } else ""

            val tempChosen = if (item.tempOptions.isNotEmpty()) {
                item.tempOptions.getOrNull(spTemp.selectedItemPosition)?.name ?: ""
            } else ""

            val iceLevel = if (item.iceLevels.isNotEmpty()) {
                item.iceLevels.getOrNull(spIceLevel.selectedItemPosition) ?: ""
            } else ""

            val sugarLevel = if (item.sugarLevels.isNotEmpty()) {
                item.sugarLevels.getOrNull(spSugarLevel.selectedItemPosition) ?: ""
            } else ""

            // Build customizations map
            val customizations = mutableMapOf<String, String>()
            if (sizeChosen.isNotBlank()) customizations["size"] = sizeChosen
            if (tempChosen.isNotBlank()) customizations["temp"] = tempChosen
            if (iceLevel.isNotBlank()) customizations["ice"] = iceLevel
            if (sugarLevel.isNotBlank()) customizations["sugar"] = sugarLevel
            if (etNote.text.toString().isNotBlank()) customizations["note"] = etNote.text.toString()

            val unitPrice = item.basePrice  // extend later with modifiers if needed
            val cartItem = CartItem(
                item = item,
                quantity = quantity,
                customizations = customizations,
                price = unitPrice
            )

            onAddToCartListener?.invoke(cartItem)
            dismiss()
        }
    }

    fun setOnAddToCartListener(listener: (CartItem) -> Unit) {
        onAddToCartListener = listener
    }

    companion object {
        fun newInstance(item: Item): ItemCustomizationDialogFragment {
            return ItemCustomizationDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("item", item)
                }
            }
        }
    }
}
