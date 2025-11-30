package com.example.coffeeshop.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.coffeeshop.R

class LuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    
    private val prizes = listOf(
        "Giảm 20.000",
        "Túi Cối",
        "Giảm Giá 10%",
        "Giảm Giá 20%",
        "Giảm 20K",
        "Hộp Trang Sức"
    )
    
    private val colors = listOf(
        Color.parseColor("#E53935"), // Đỏ
        Color.parseColor("#FFFFFF"), // Trắng
        Color.parseColor("#E53935"), // Đỏ
        Color.parseColor("#FFFFFF"), // Trắng
        Color.parseColor("#E53935"), // Đỏ
        Color.parseColor("#FFFFFF")  // Trắng
    )
    
    private val goldColor = Color.parseColor("#FFD700")
    private val lightColor = Color.parseColor("#FFEB3B")

    init {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 28f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 8f
        borderPaint.color = goldColor
        
        lightPaint.color = lightColor
        lightPaint.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = minOf(w, h) / 2f - 20f
        // Điều chỉnh text size theo radius
        textPaint.textSize = radius * 0.12f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Lưu trạng thái canvas
        canvas.save()
        // Không rotate canvas ở đây, để View tự xử lý rotation
        
        // Vẽ viền vàng với đèn nhỏ
        drawGoldenBorder(canvas)
        
        // Vẽ các phần thưởng
        val sweepAngle = 360f / prizes.size
        prizes.forEachIndexed { index, prize ->
            val startAngle = index * sweepAngle - 90f // Bắt đầu từ trên
            
            // Vẽ phần màu
            paint.color = colors[index]
            paint.style = Paint.Style.FILL
            val rect = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            
            // Vẽ viền giữa các phần
            paint.color = Color.parseColor("#8B4513")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            
            // Vẽ text phần thưởng
            val textAngle = startAngle + sweepAngle / 2f
            val textRadius = radius * 0.7f
            val textX = centerX + textRadius * kotlin.math.cos(Math.toRadians(textAngle.toDouble())).toFloat()
            val textY = centerY + textRadius * kotlin.math.sin(Math.toRadians(textAngle.toDouble())).toFloat()
            
            // Xoay text theo hướng của phần
            canvas.save()
            canvas.translate(textX, textY)
            canvas.rotate(textAngle + 90f)
            textPaint.color = if (colors[index] == Color.parseColor("#FFFFFF")) Color.BLACK else Color.WHITE
            canvas.drawText(prize, 0f, 0f, textPaint)
            canvas.restore()
        }
        
        // Khôi phục trạng thái canvas
        canvas.restore()
        
        // Vẽ nút "QUAY" ở giữa
        drawCenterButton(canvas)
    }
    
    private fun drawGoldenBorder(canvas: Canvas) {
        // Vẽ viền vàng
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 12f
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
        
        // Vẽ đèn nhỏ xung quanh viền
        val lightCount = 24
        val lightRadius = 8f
        for (i in 0 until lightCount) {
            val angle = (360f / lightCount * i) * Math.PI / 180
            val lightX = centerX + (radius + 6f) * kotlin.math.cos(angle).toFloat()
            val lightY = centerY + (radius + 6f) * kotlin.math.sin(angle).toFloat()
            canvas.drawCircle(lightX, lightY, lightRadius, lightPaint)
        }
    }
    
    private fun drawCenterButton(canvas: Canvas) {
        val buttonRadius = radius * 0.25f
        
        // Vẽ nền nút vàng
        paint.color = goldColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, buttonRadius, paint)
        
        // Vẽ viền nút
        paint.color = Color.parseColor("#FFA500")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, buttonRadius, paint)
        
        // Vẽ text "QUAY"
        textPaint.color = Color.WHITE
        textPaint.textSize = buttonRadius * 0.6f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("QUAY", centerX, centerY + textPaint.textSize / 3, textPaint)
    }
    
}

