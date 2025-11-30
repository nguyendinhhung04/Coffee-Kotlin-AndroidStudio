package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.coffeeshop.R
import com.example.coffeeshop.ui.fragment.LoginFragment
import com.example.coffeeshop.ui.fragment.RegisterFragment
import com.example.coffeeshop.utils.UserSessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLoginTab: Button
    private lateinit var btnSignUpTab: Button
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session manager
        sessionManager = UserSessionManager(this)

        // Kiểm tra nếu đã login thì chuyển thẳng sang MainActivity
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        btnLoginTab = findViewById(R.id.btnLoginTab)
        btnSignUpTab = findViewById(R.id.btnSignUpTab)

        // Hiển thị mặc định form login
        replaceFragment(LoginFragment())
        updateTabUI(isLogin = true)

        btnLoginTab.setOnClickListener {
            replaceFragment(LoginFragment())
            updateTabUI(isLogin = true)
        }

        btnSignUpTab.setOnClickListener {
            replaceFragment(RegisterFragment())
            updateTabUI(isLogin = false)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authFragmentContainer, fragment)
            .commit()
    }

    private fun updateTabUI(isLogin: Boolean) {
        if (isLogin) {
            btnLoginTab.setBackgroundTintList(getColorStateList(R.color.brown))
            btnSignUpTab.setBackgroundTintList(getColorStateList(R.color.brownMedium))
        } else {
            btnLoginTab.setBackgroundTintList(getColorStateList(R.color.brownMedium))
            btnSignUpTab.setBackgroundTintList(getColorStateList(R.color.brown))
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}