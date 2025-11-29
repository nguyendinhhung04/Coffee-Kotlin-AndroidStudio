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
import com.google.android.material.button.MaterialButton

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

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
                Toast.makeText(requireContext(), "Login successful (Hardcoded)!", Toast.LENGTH_SHORT).show()
                navigateToMain(username)
            } else {
                // 🧩 2. Fallback to API check via DAO
                // Note: Callback has 4 params: success, message, user, token
                UserDAO.checkLogin(username, password) { success, message, user, token ->
                    if (isAdded) { // Check if fragment is still attached
                        requireActivity().runOnUiThread {
                            if (success) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                                // Có thể lưu token vào SharedPreferences ở đây nếu cần
                                // val savedToken = token

                                val remoteUsername = user?.optString("username") ?: username
                                navigateToMain(remoteUsername)
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

    private fun navigateToMain(username: String) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        requireActivity().finish()
    }
}