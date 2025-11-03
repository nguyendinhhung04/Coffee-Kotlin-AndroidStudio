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
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.putExtra("username", user.optString("username"))
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