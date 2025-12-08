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
        val ctx = requireContext()

        // Size
        if (item.sizes.isNotEmpty()) {
            val sizeLabels = item.sizes.map { it.label }
            val sizeAdapter = ArrayAdapter(
                ctx,
                R.layout.item_spinner_brown,      // dùng layout custom
                sizeLabels
            ).also {
                it.setDropDownViewResource(R.layout.item_spinner_brown)
            }
            spSize.adapter = sizeAdapter
            spSize.visibility = View.VISIBLE
        } else {
            spSize.visibility = View.GONE
        }

        // Temperature
        if (item.tempOptions.isNotEmpty()) {
            val tempLabels = item.tempOptions.map { it.label }
            val tempAdapter = ArrayAdapter(
                ctx,
                R.layout.item_spinner_brown,
                tempLabels
            ).also {
                it.setDropDownViewResource(R.layout.item_spinner_brown)
            }
            spTemp.adapter = tempAdapter
            spTemp.visibility = View.VISIBLE
        } else {
            spTemp.visibility = View.GONE
        }

        // Ice level
        if (item.iceLevels.isNotEmpty()) {
            val iceAdapter = ArrayAdapter(
                ctx,
                R.layout.item_spinner_brown,
                item.iceLevels
            ).also {
                it.setDropDownViewResource(R.layout.item_spinner_brown)
            }
            spIceLevel.adapter = iceAdapter
            spIceLevel.visibility = View.VISIBLE
        } else {
            spIceLevel.visibility = View.GONE
        }

        // Sugar level
        if (item.sugarLevels.isNotEmpty()) {
            val sugarAdapter = ArrayAdapter(
                ctx,
                R.layout.item_spinner_brown,
                item.sugarLevels
            ).also {
                it.setDropDownViewResource(R.layout.item_spinner_brown)
            }
            spSugarLevel.adapter = sugarAdapter
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

            // Get selected objects
            val sizeObj = if (item.sizes.isNotEmpty()) {
                item.sizes.getOrNull(spSize.selectedItemPosition)
            } else null

            val tempObj = if (item.tempOptions.isNotEmpty()) {
                item.tempOptions.getOrNull(spTemp.selectedItemPosition)
            } else null

            val iceLevel = if (item.iceLevels.isNotEmpty()) {
                item.iceLevels.getOrNull(spIceLevel.selectedItemPosition) ?: ""
            } else ""

            val sugarLevel = if (item.sugarLevels.isNotEmpty()) {
                item.sugarLevels.getOrNull(spSugarLevel.selectedItemPosition) ?: ""
            } else ""

            // Later: collect chosen topping names here when you have UI for toppings
            val chosenToppings: List<String> = emptyList()

            // Build customizations map (used by CartManager to distinguish lines)
            val customizations = mutableMapOf<String, String>()
            sizeObj?.let { customizations["size"] = it.name }
            tempObj?.let { customizations["temp"] = it.name }
            if (iceLevel.isNotBlank()) customizations["ice"] = iceLevel
            if (sugarLevel.isNotBlank()) customizations["sugar"] = sugarLevel
            if (etNote.text.toString().isNotBlank()) customizations["note"] = etNote.text.toString()
            if (chosenToppings.isNotEmpty()) {
                customizations["toppings"] = chosenToppings.joinToString()
            }

            // ===== Calculate unit price with modifiers =====
            var unitPrice = item.basePrice

            // Size modifier
            if (sizeObj != null) {
                unitPrice += sizeObj.modifier
            }

            // Temp modifier
            if (tempObj != null) {
                unitPrice += tempObj.modifier
            }

            // Toppings modifiers (when implemented)
            if (chosenToppings.isNotEmpty()) {
                chosenToppings.forEach { topName ->
                    val topping = item.toppings.firstOrNull { it.name == topName }
                    if (topping != null) {
                        unitPrice += topping.price
                    }
                }
            }

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
