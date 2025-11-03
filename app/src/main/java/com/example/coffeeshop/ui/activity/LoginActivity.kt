package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import com.example.coffeeshop.data.dao.UserDAO
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    // Hardcoded user info
    private val hardcodedUsername = "admin"
    private val hardcodedPassword = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgot = findViewById<TextView>(R.id.tvForgot)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Check if login matches the hardcoded user
            if (username == hardcodedUsername && password == hardcodedPassword) {
                val user = JSONObject().apply {
                    put("username", username)
                }

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", user.optString("username"))
                startActivity(intent)
                finish()
            } else {
                // 🔹 Otherwise, fallback to your DAO login check
                UserDAO.checkLogin(username, password) { success, message, user ->
                    runOnUiThread {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                        if (success && user != null) {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("username", user.optString("username"))
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Forgot password clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}
