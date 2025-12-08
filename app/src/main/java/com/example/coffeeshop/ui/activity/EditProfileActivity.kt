package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.widget.Button
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
    private lateinit var btnSave: Button

    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sessionManager = UserSessionManager(this)

        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSaveProfile)

        loadCurrentData()
        setupListeners()
    }

    private fun loadCurrentData() {
        // Adjust getters to your UserSessionManager
        etFullName.setText(sessionManager.getFullName() ?: sessionManager.getDisplayName() ?: "")
        etPhone.setText(sessionManager.getPhone() ?: "")
        etEmail.setText(sessionManager.getEmail() ?: "")
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (fullName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            if (userId.isNullOrBlank()) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserDAO.updateUser(userId, fullName, email, phone) { success, message ->
                runOnUiThread {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        sessionManager.setFullName(fullName)
                        sessionManager.setPhone(phone)
                        sessionManager.setEmail(email)
                        finish()
                    }
                }
            }
        }
    }
}
