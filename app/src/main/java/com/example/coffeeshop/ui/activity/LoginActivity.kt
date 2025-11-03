package com.example.coffeeshop.ui.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.coffeeshop.R
import com.example.coffeeshop.ui.fragment.LoginFragment
import com.example.coffeeshop.ui.fragment.RegisterFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLoginTab: Button
    private lateinit var btnSignUpTab: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLoginTab = findViewById(R.id.btnLoginTab)
        btnSignUpTab = findViewById(R.id.btnSignUpTab)

        // Hiển thị mặc định form login
        replaceFragment(LoginFragment())

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
}