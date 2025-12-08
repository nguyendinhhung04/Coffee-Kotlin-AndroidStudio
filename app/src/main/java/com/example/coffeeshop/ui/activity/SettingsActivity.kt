package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.utils.UserSessionManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmailOrPhone: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var layoutChangePassword: LinearLayout
    private lateinit var btnLogout: Button

    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sessionManager = UserSessionManager(this)

        initViews()
        loadUserInfo()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()  // cập nhật lại tên/email/phone sau khi EditProfileActivity lưu xong
    }

    private fun initViews() {
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmailOrPhone = findViewById(R.id.tvProfileEmailOrPhone)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        layoutChangePassword = findViewById(R.id.layoutChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadUserInfo() {
        // Adjust these getters according to your UserSessionManager implementation
        val displayName = sessionManager.getDisplayName() ?: "Guest"
        val email = sessionManager.getEmail() ?: ""
        val phone = sessionManager.getPhone() ?: ""

        tvProfileName.text = displayName

        tvProfileEmailOrPhone.text = when {
            email.isNotBlank() && phone.isNotBlank() -> "$email • $phone"
            email.isNotBlank() -> email
            phone.isNotBlank() -> phone
            else -> "No contact info"
        }
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        layoutChangePassword.setOnClickListener {
            // TODO: open ChangePasswordActivity, for now just a Toast
            Toast.makeText(this, "Change password coming soon", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
