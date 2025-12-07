package com.example.coffeeshop.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.UserDAO
import com.example.coffeeshop.ui.activity.MainActivity
import com.example.coffeeshop.utils.UserSessionManager
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {

    private lateinit var sessionManager: UserSessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize session manager
        sessionManager = UserSessionManager(requireContext())

        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.btnLogin)
        val tvForgot = view.findViewById<TextView>(R.id.tvForgot)

        // 🧠 Hardcoded users list (Backdoor for testing)
        val hardcodedUsers = mapOf(
            "vietdung" to "123456",
            "hung36" to "password",
            "admin" to "admin123"
        )

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🧩 1. Check hardcoded user first
            if (hardcodedUsers.containsKey(username) && hardcodedUsers[username] == password) {
                // Lưu session cho hardcoded user
                sessionManager.saveUserSession(
                    userId = "hardcoded_$username",
                    username = username,
                    fullName = username.capitalize(),
                    email = "$username@coffee.com",
                    phone = "",
                    token = "hardcoded_token"
                )

                Toast.makeText(requireContext(), "Login successful (Hardcoded)!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                // 🧩 2. Fallback to API check via DAO
                UserDAO.checkLogin(username, password) { success, message, user, token ->
                    if (isAdded) { // Check if fragment is still attached
                        requireActivity().runOnUiThread {
                            if (success && user != null) {
                                // Lưu thông tin user vào session
                                sessionManager.saveUserSession(
                                    userId = user.optString("_id", ""),
                                    username = user.optString("username", username),
                                    fullName = user.optString("fullName", ""),
                                    email = user.optString("email", ""),
                                    phone = user.optString("phone", ""),
                                    token = token
                                )
                                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                                navigateToMain()
                            } else {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

        tvForgot.setOnClickListener {
            Toast.makeText(requireContext(), "Forgot password clicked!", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
