package com.example.coffeeshop.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.coffeeshop.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CheckInActivity : AppCompatActivity() {

    private lateinit var ivCameraPreview: ImageView
    private lateinit var ivCapturedImage: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnRetake: Button
    private lateinit var btnShare: Button
    private lateinit var llActionButtons: LinearLayout
    private lateinit var cvCameraPreview: androidx.cardview.widget.CardView

    private var capturedBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private val CAMERA_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_CODE = 101
    private val SHARE_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_in)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        ivCameraPreview = findViewById(R.id.ivCameraPreview)
        ivCapturedImage = findViewById(R.id.ivCapturedImage)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnRetake = findViewById(R.id.btnRetake)
        btnShare = findViewById(R.id.btnShare)
        llActionButtons = findViewById(R.id.llActionButtons)
        cvCameraPreview = findViewById(R.id.cvCameraPreview)
    }

    private fun setupClickListeners() {
        btnTakePhoto.setOnClickListener {
            checkCameraPermission()
        }

        btnRetake.setOnClickListener {
            retakePhoto()
        }

        btnShare.setOnClickListener {
            shareToSocialMedia()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Lưu ảnh vào file để có thể share
        val photoFile = File(getExternalFilesDir(null), "checkin_photo.jpg")
        imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Cần quyền camera để chụp ảnh check-in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            // Đọc ảnh từ file đã lưu
            if (imageUri != null) {
                try {
                    capturedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    showCapturedImage()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Lỗi khi đọc ảnh", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == SHARE_REQUEST_CODE) {
            // Sau khi share, chuyển sang vòng quay may mắn
            Toast.makeText(this, "Cảm ơn bạn đã chia sẻ! Hãy quay vòng quay may mắn", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LuckyWheelActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showCapturedImage() {
        ivCapturedImage.setImageBitmap(capturedBitmap)
        ivCapturedImage.visibility = android.view.View.VISIBLE
        cvCameraPreview.visibility = android.view.View.GONE
        llActionButtons.visibility = android.view.View.VISIBLE
    }

    private fun retakePhoto() {
        ivCapturedImage.visibility = android.view.View.GONE
        cvCameraPreview.visibility = android.view.View.VISIBLE
        llActionButtons.visibility = android.view.View.GONE
        capturedBitmap = null
    }

    private fun shareToSocialMedia() {
        if (capturedBitmap == null || imageUri == null) {
            Toast.makeText(this, "Vui lòng chụp ảnh trước", Toast.LENGTH_SHORT).show()
            return
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "Tôi đang check-in tại CoffeeShop! ☕️ #CoffeeShop #CheckIn")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Chia sẻ ảnh check-in lên mạng xã hội")
        startActivityForResult(chooser, SHARE_REQUEST_CODE)
    }
}

