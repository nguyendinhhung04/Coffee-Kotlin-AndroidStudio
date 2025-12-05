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
import com.example.coffeeshop.ui.activity.AdminDashboardActivity
import com.example.coffeeshop.utils.UserSessionManager
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

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserDAO.checkLogin(username, password) { success, message, user ->
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success && user != null) {
                        // Lưu thông tin user vào session
                        val sessionManager = UserSessionManager(requireContext())
                        val userId = user.optString("_id", "")
                        val username = user.optString("username", "")
                        val role = user.optString("role", "user")
                        val fullName = user.optString("fullName", null)
                        val email = user.optString("email", null)
                        val phone = user.optString("phone", null)
                        
                        sessionManager.saveUserSession(userId, username, role, fullName, email, phone)
                        
                        // Redirect dựa trên role
                        val intent = if (role == "admin") {
                            Intent(requireContext(), AdminDashboardActivity::class.java)
                        } else {
                            Intent(requireContext(), MainActivity::class.java).apply {
                                putExtra("username", username)
                            }
                        }
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }

        tvForgot.setOnClickListener {
            Toast.makeText(requireContext(), "Forgot password clicked!", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}