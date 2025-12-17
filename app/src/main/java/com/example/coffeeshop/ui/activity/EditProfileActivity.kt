package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.UserDAO
import com.example.coffeeshop.utils.UserSessionManager

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etStreet: EditText
    private lateinit var etWard: EditText
    private lateinit var etDistrict: EditText
    private lateinit var etCity: EditText
    private lateinit var cbDefaultAddress: CheckBox
    private lateinit var btnSave: Button

    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = UserSessionManager(this)

        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etStreet = findViewById(R.id.etStreet)
        etWard = findViewById(R.id.etWard)
        etDistrict = findViewById(R.id.etDistrict)
        etCity = findViewById(R.id.etCity)
        cbDefaultAddress = findViewById(R.id.cbDefaultAddress)
        btnSave = findViewById(R.id.btnSaveProfile)

        loadCurrentData()
        setupListeners()
    }

    private fun loadCurrentData() {
        // Thông tin cơ bản
        etFullName.setText(sessionManager.getFullName() ?: sessionManager.getDisplayName())
        etPhone.setText(sessionManager.getPhone() ?: "")
        etEmail.setText(sessionManager.getEmail() ?: "")

        // Địa chỉ (lấy từ SharedPreferences)
        etStreet.setText(sessionManager.getStreet() ?: "")
        etWard.setText(sessionManager.getWard() ?: "")
        etDistrict.setText(sessionManager.getDistrict() ?: "")
        etCity.setText(sessionManager.getCity() ?: "")

        // Checkbox địa chỉ mặc định
        cbDefaultAddress.isChecked = sessionManager.isDefaultAddress()
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val street = etStreet.text.toString().trim()
            val ward = etWard.text.toString().trim()
            val district = etDistrict.text.toString().trim()
            val city = etCity.text.toString().trim()
            val isDefault = cbDefaultAddress.isChecked

            if (fullName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            if (userId.isNullOrBlank()) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tùy UserDAO: thêm tham số address + isDefaultAddress
            UserDAO.updateUser(
                userId = userId,
                fullName = fullName,
                email = email,
                phone = phone,
                street = street,
                ward = ward,
                district = district,
                city = city,
                isDefaultAddress = isDefault
            ) { success, message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        sessionManager.setFullName(fullName)
                        sessionManager.setPhone(phone)
                        sessionManager.setEmail(email)
                        sessionManager.setStreet(street)
                        sessionManager.setWard(ward)
                        sessionManager.setDistrict(district)
                        sessionManager.setCity(city)
                        sessionManager.setDefaultAddress(isDefault)
                        finish()
                    }
                }
            }
        }
    }
}
