package com.example.coffeeshop.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.R
import android.widget.LinearLayout
import android.widget.Toast
import com.example.coffeeshop.data.dao.ItemDAO
import com.example.coffeeshop.utils.SeasonHelper
import com.example.coffeeshop.utils.Season
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URL
import java.util.Hashtable

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var ivRecommendedDrink1: ImageView
    private lateinit var ivRecommendedDrink2: ImageView
    private lateinit var tvDrinkName1: TextView
    private lateinit var tvDrinkName2: TextView
    private lateinit var tvDrinkPrice1: TextView
    private lateinit var tvDrinkPrice2: TextView
    private lateinit var layoutRecommendedDrinks: LinearLayout
    private lateinit var tvNoSavedAddress: TextView
    private lateinit var tvEditAddress: TextView
    private lateinit var btnPayment: Button
    
    private lateinit var sharedPreferences: SharedPreferences
    private val ADDRESS_KEY = "saved_delivery_address"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        sharedPreferences = getSharedPreferences("CoffeeShopPrefs", MODE_PRIVATE)
        initViews()
        setupBottomNav()
        loadRecommendedDrinksBySeason()
        loadSavedAddress()
    }

    private fun initViews() {
        ivRecommendedDrink1 = findViewById(R.id.ivRecommendedDrink1)
        ivRecommendedDrink2 = findViewById(R.id.ivRecommendedDrink2)
        tvDrinkName1 = findViewById(R.id.tvRecommendedDrinkName1)
        tvDrinkName2 = findViewById(R.id.tvRecommendedDrinkName2)
        tvDrinkPrice1 = findViewById(R.id.tvRecommendedDrinkPrice1)
        tvDrinkPrice2 = findViewById(R.id.tvRecommendedDrinkPrice2)
        layoutRecommendedDrinks = findViewById(R.id.layoutRecommendedDrinks)
        tvNoSavedAddress = findViewById(R.id.tvNoSavedAddress)
        tvEditAddress = findViewById(R.id.tvEditAddress)
        btnPayment = findViewById(R.id.btnPayment)
        
        // Long-click vào phần "Other drinks we recommend" để test mùa
        layoutRecommendedDrinks.setOnLongClickListener {
            showSeasonTestDialog()
            true
        }
        
        // Click vào "Edit" để nhập địa chỉ
        tvEditAddress.setOnClickListener {
            showAddressInputDialog()
        }
        
        // Click vào nút "Thanh toán" để hiển thị QR code
        btnPayment.setOnClickListener {
            showQRPaymentDialog()
        }
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_payment)
        bottomNav.selectedItemId = R.id.navigation_payment
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.navigation_drink_menu -> startActivity(Intent(this, DrinkMenuActivity::class.java))
                R.id.navigation_your_order -> startActivity(Intent(this, YourOrderActivity::class.java))
                R.id.navigation_favorites -> startActivity(Intent(this, FavoritesActivity::class.java))
                R.id.navigation_payment -> {
                    // Already on payment screen
                    true
                }
            }
            if (it.itemId != R.id.navigation_payment) {
                finish()
            }
            true
        }
    }

    private fun loadRecommendedDrinksBySeason() {
        // Xác định mùa hiện tại
        val currentSeason = SeasonHelper.getCurrentSeason()
        val seasonKeywords = SeasonHelper.getSeasonKeywords(currentSeason)
        
        // Lấy món gợi ý theo mùa
        ItemDAO.getRecommendedItemsBySeason(seasonKeywords) { success, message, items ->
            runOnUiThread {
                if (success && items != null && items.isNotEmpty()) {
                    // Hiển thị món đầu tiên
                    if (items.size >= 1) {
                        displayRecommendedDrink(items[0], ivRecommendedDrink1, 0)
                    }
                    
                    // Hiển thị món thứ hai (nếu có)
                    if (items.size >= 2) {
                        displayRecommendedDrink(items[1], ivRecommendedDrink2, 1)
                    }
                } else {
                    // Nếu không tìm thấy món theo mùa, dùng món mặc định
                    loadDefaultRecommendedDrinks()
                }
            }
        }
    }

    private fun displayRecommendedDrink(item: com.example.coffeeshop.data.model.Item, imageView: ImageView, index: Int) {
        // Load ảnh
        if (item.image_url.isNotEmpty()) {
            loadImageFromUrl(imageView, item.image_url)
        } else {
            imageView.setImageResource(R.drawable.placeholder_coffee)
        }
        
        // Cập nhật tên và giá
        when (index) {
            0 -> {
                tvDrinkName1.text = item.name
                tvDrinkPrice1.text = "${item.basePrice.toInt()} VND"
            }
            1 -> {
                tvDrinkName2.text = item.name
                tvDrinkPrice2.text = "${item.basePrice.toInt()} VND"
            }
        }
    }

    private fun loadDefaultRecommendedDrinks() {
        // Fallback: Load ảnh mặc định nếu không tìm thấy món theo mùa
        loadImageFromUrl(
            ivRecommendedDrink1,
            "https://i.imgur.com/qR1sQF7.png"
        )
        loadImageFromUrl(
            ivRecommendedDrink2,
            "https://i.imgur.com/fPcJDwr.png"
        )
    }

    private fun loadImageFromUrl(imageView: ImageView, imageUrl: String) {
        Thread {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                imageView.post {
                    // Nếu load lỗi, giữ nguyên placeholder
                    imageView.setImageResource(R.drawable.placeholder_coffee)
                }
            }
        }.start()
    }

    private fun showSeasonTestDialog() {
        val seasons = arrayOf("Xuân", "Hè", "Thu", "Đông", "Tắt test (dùng mùa thật)")
        val seasonValues = arrayOf(Season.SPRING, Season.SUMMER, Season.FALL, Season.WINTER, null)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn mùa để test")
            .setItems(seasons) { _, which ->
                if (which < 4) {
                    // Set mùa test
                    SeasonHelper.setTestSeason(seasonValues[which])
                    Toast.makeText(this, "Đã set mùa test: ${seasons[which]}", Toast.LENGTH_SHORT).show()
                } else {
                    // Tắt test, dùng mùa thật
                    SeasonHelper.setTestSeason(null)
                    Toast.makeText(this, "Đã tắt test, dùng mùa thật", Toast.LENGTH_SHORT).show()
                }
                // Reload món gợi ý với mùa mới
                loadRecommendedDrinksBySeason()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun loadSavedAddress() {
        val savedAddress = sharedPreferences.getString(ADDRESS_KEY, null)
        if (savedAddress != null && savedAddress.isNotEmpty()) {
            tvNoSavedAddress.text = savedAddress
        } else {
            tvNoSavedAddress.text = "No saved address"
        }
    }

    private fun showAddressInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_address_input, null)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val btnSelectFromMap = dialogView.findViewById<android.widget.Button>(R.id.btnSelectFromMap)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        // Load địa chỉ đã lưu (nếu có)
        val savedAddress = sharedPreferences.getString(ADDRESS_KEY, null)
        if (savedAddress != null && savedAddress.isNotEmpty()) {
            etAddress.setText(savedAddress)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Chọn từ bản đồ
        btnSelectFromMap.setOnClickListener {
            openGoogleMaps()
        }

        // Lưu địa chỉ
        btnSave.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                sharedPreferences.edit().putString(ADDRESS_KEY, address).apply()
                tvNoSavedAddress.text = address
                dialog.dismiss()
                Toast.makeText(this, "Đã lưu địa chỉ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
            }
        }

        // Hủy
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openGoogleMaps() {
        try {
            // Mở Google Maps với chế độ chọn địa điểm
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            // Nếu không có Google Maps, mở trình duyệt với Google Maps web
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps"))
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showQRPaymentDialog() {
        // Tính tổng tiền (có thể lấy từ order thực tế)
        val totalAmount = 32000 // Tạm thời hardcode, có thể lấy từ order thực tế
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_qr_payment, null)
        val ivQRCode = dialogView.findViewById<ImageView>(R.id.ivQRCode)
        val tvPaymentAmount = dialogView.findViewById<TextView>(R.id.tvPaymentAmount)
        val btnCloseQR = dialogView.findViewById<Button>(R.id.btnCloseQR)

        tvPaymentAmount.text = "Tổng tiền: ${totalAmount} VND"

        // Tạo QR code với format VietQR
        // Lưu ý: Để QR code hợp lệ, tài khoản cần được đăng ký với VietQR
        val qrData = generateVietQRCode(totalAmount)
        
        // Debug: Log QR data để kiểm tra
        android.util.Log.d("PaymentActivity", "QR Data: $qrData")
        
        val qrBitmap = generateQRCode(qrData, 500, 500)
        if (qrBitmap != null) {
            ivQRCode.setImageBitmap(qrBitmap)
        } else {
            Toast.makeText(this, "Không thể tạo QR code", Toast.LENGTH_SHORT).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCloseQR.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun generateVietQRCode(amount: Int): String {
        // Format VietQR theo chuẩn EMV QR Code của các ngân hàng Việt Nam
        // Cấu trúc: [ID][Length][Value]
        
        // Thông tin ngân hàng
        val bankCode = "970422" // Mã ngân hàng (970422 = Vietcombank)
        val accountNumber = "1027833894" // Số tài khoản ngân hàng
        val merchantName = "COFFEESHOP" // Tên cửa hàng
        val content = "Thanh toan don hang" // Nội dung chuyển khoản
        
        // Payload Format Indicator (00): 01 = QR Code
        val payloadIndicator = "000201"
        
        // Point of Initiation Method (01): 12 = Static QR Code
        val poiMethod = "010212"
        
        // Merchant Account Information (38)
        // 00 = GUID (A000000727 = VietQR), 01 = Bank code, 02 = Account number
        val guid = "0010A000000727"
        val bankCodeField = "01" + String.format("%02d", bankCode.length) + bankCode
        val accountField = "02" + String.format("%02d", accountNumber.length) + accountNumber
        val merchantAccountInfoValue = guid + bankCodeField + accountField
        val merchantAccountInfo = "38" + String.format("%02d", merchantAccountInfoValue.length) + merchantAccountInfoValue
        
        // Transaction Currency (53): 704 = VND
        val currency = "5303704"
        
        // Transaction Amount (54): Format với 2 chữ số thập phân
        val amountStr = String.format("%.2f", amount.toDouble())
        val transactionAmount = "54" + String.format("%02d", amountStr.length) + amountStr
        
        // Country Code (58): VN
        val countryCode = "5802VN"
        
        // Merchant Name (59)
        val merchantNameField = "59" + String.format("%02d", merchantName.length) + merchantName
        
        // Additional Data Field Template (62)
        // 08 = Purpose of Transaction
        val purposeField = "08" + String.format("%02d", content.length) + content
        val additionalData = "62" + String.format("%02d", purposeField.length) + purposeField
        
        // Ghép tất cả các trường lại (chưa có CRC)
        val qrDataWithoutCRC = payloadIndicator + poiMethod + merchantAccountInfo + 
                               currency + transactionAmount + countryCode + 
                               merchantNameField + additionalData
        
        // Tính CRC-16 (Checksum) - tính trên dữ liệu + "6304"
        val crc = calculateCRC16(qrDataWithoutCRC + "6304")
        val crcField = "6304" + String.format("%04X", crc).uppercase()
        
        // QR Code hoàn chỉnh
        return qrDataWithoutCRC + crcField
    }
    
    private fun calculateCRC16(data: String): Int {
        var crc = 0xFFFF
        val polynomial = 0x1021
        
        val bytes = data.toByteArray(Charsets.ISO_8859_1)
        for (byte in bytes) {
            var b = byte.toInt() and 0xFF
            for (i in 0..7) {
                val bit = (b ushr (7 - i) and 1) == 1
                val c15 = (crc ushr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
            }
        }
        
        crc = crc and 0xFFFF
        return crc
    }
    
    // Phương án dự phòng: Format đơn giản hơn (URL-based) - để test
    private fun generateSimplePaymentQR(amount: Int): String {
        // Format đơn giản dạng URL để các app ngân hàng có thể đọc
        val bankCode = "970422"
        val accountNumber = "1027833894"
        val merchantName = "COFFEESHOP"
        
        // Format: banking://transfer?bank=xxx&account=xxx&amount=xxx&content=xxx
        return "banking://transfer?bank=$bankCode&account=$accountNumber&amount=$amount&content=Thanh%20toan%20don%20hang&merchant=$merchantName"
    }
    
    // Lưu ý: Để QR code hợp lệ với ngân hàng, bạn cần:
    // 1. Đăng ký tài khoản với VietQR tại https://vietqr.net/
    // 2. Hoặc liên hệ ngân hàng để đăng ký dịch vụ VietQR
    // 3. Sau khi đăng ký, bạn sẽ có thông tin chính xác để tạo QR code hợp lệ

    private fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
        return try {
            val hints = Hashtable<EncodeHintType, Any>()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints)

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
