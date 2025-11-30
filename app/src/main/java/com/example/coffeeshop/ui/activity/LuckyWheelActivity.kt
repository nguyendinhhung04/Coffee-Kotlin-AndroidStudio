package com.example.coffeeshop.ui.activity

import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.coffeeshop.R
import kotlin.random.Random

class LuckyWheelActivity : AppCompatActivity() {

    private lateinit var wheelView: com.example.coffeeshop.ui.widget.LuckyWheelView
    private lateinit var btnSpin: Button
    private lateinit var cvResult: CardView
    private lateinit var tvResultPrize: TextView
    private lateinit var btnUsePrize: Button
    private lateinit var sharedPreferences: SharedPreferences

    private val prizes = listOf(
        Prize("Giảm 20.000 VND", "DISCOUNT_20K", 20000),
        Prize("Túi Cối", "BAG", 0),
        Prize("Voucher giảm 10%", "VOUCHER_10", 10),
        Prize("Voucher giảm 20%", "VOUCHER_20", 20),
        Prize("Giảm 20K", "DISCOUNT_20K_2", 20000),
        Prize("Hộp Trang Sức", "JEWELRY_BOX", 0)
    )

    private var isSpinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lucky_wheel)

        sharedPreferences = getSharedPreferences("CoffeeShopPrefs", MODE_PRIVATE)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        wheelView = findViewById(R.id.wheelView)
        btnSpin = findViewById(R.id.btnSpin)
        cvResult = findViewById(R.id.cvResult)
        tvResultPrize = findViewById(R.id.tvResultPrize)
        btnUsePrize = findViewById(R.id.btnUsePrize)
    }

    private fun setupClickListeners() {
        btnSpin.setOnClickListener {
            if (!isSpinning) {
                spinWheel()
            }
        }

        btnUsePrize.setOnClickListener {
            // Lưu phần thưởng và quay lại
            finish()
        }
    }

    private fun spinWheel() {
        if (isSpinning) return

        isSpinning = true
        btnSpin.isEnabled = false
        cvResult.visibility = View.GONE

        // Chọn phần thưởng ngẫu nhiên (có thể điều chỉnh xác suất)
        val selectedPrize = selectRandomPrize()
        
        // Tính góc quay (mỗi phần thưởng chiếm 60 độ - 360/6)
        // Mũi tên ở trên cùng (0 độ), phần thưởng đầu tiên cũng bắt đầu từ trên
        val prizeIndex = prizes.indexOf(selectedPrize)
        val sweepAngle = 360f / prizes.size
        // Góc giữa của phần thưởng (tính từ trên, theo chiều kim đồng hồ)
        val prizeCenterAngle = prizeIndex * sweepAngle + sweepAngle / 2f
        // Offset ngẫu nhiên trong vùng phần thưởng (tránh viền)
        val randomOffset = (Random.nextFloat() - 0.5f) * sweepAngle * 0.6f
        val finalAngle = prizeCenterAngle + randomOffset
        
        // Quay nhiều vòng + góc cuối cùng
        // Cần quay ngược lại để phần thưởng đến đúng vị trí mũi tên (0 độ)
        val currentRot = wheelView.rotation % 360f
        val totalRotation = currentRot + 360f * 5 + (360f - finalAngle) // Quay 5 vòng rồi dừng

        // Animation quay
        val animator = ObjectAnimator.ofFloat(wheelView, "rotation", currentRot, totalRotation)
        animator.duration = 3000 // 3 giây
        animator.interpolator = DecelerateInterpolator()
        animator.start()

        // Sau khi quay xong, hiển thị kết quả
        Handler(Looper.getMainLooper()).postDelayed({
            showPrize(selectedPrize)
            isSpinning = false
            btnSpin.isEnabled = true
        }, 3000)
    }

    private fun selectRandomPrize(): Prize {
        // Xác suất phân bổ cho 6 phần thưởng
        val random = Random.nextFloat()
        return when {
            random < 0.20f -> prizes[0] // Giảm 20.000
            random < 0.35f -> prizes[1] // Túi Cối
            random < 0.50f -> prizes[2] // 10%
            random < 0.65f -> prizes[3] // 20%
            random < 0.80f -> prizes[4] // Giảm 20K
            else -> prizes[5] // Hộp Trang Sức
        }
    }

    private fun showPrize(prize: Prize) {
        tvResultPrize.text = "Bạn đã nhận được: ${prize.name}"
        cvResult.visibility = View.VISIBLE

        // Lưu phần thưởng vào SharedPreferences
        if (prize.code != "NO_PRIZE") {
            sharedPreferences.edit().apply {
                putString("current_reward_code", prize.code)
                putString("current_reward_name", prize.name)
                putInt("current_reward_discount", prize.discount)
                putLong("reward_expiry", System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) // 7 ngày
                apply()
            }
        }
    }

    data class Prize(
        val name: String,
        val code: String,
        val discount: Int // 0 = free, hoặc % giảm giá
    )
}

