package com.example.greeting.core.utils

import android.content.Context
import android.graphics.*
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GreetingBitmapRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val imageLoader = ImageLoader(context)

    private val REF_WIDTH = 1080f
    private val REF_HEIGHT = 1920f

    suspend fun render(template: Template, userProfile: UserProfile): Bitmap = withContext(Dispatchers.IO) {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background
        val bgBitmap = fetchBitmap(template.imageUrl)
        if (bgBitmap != null) {
            canvas.drawBitmap(bgBitmap, null, Rect(0, 0, width, height), Paint(Paint.FILTER_BITMAP_FLAG))
        }

        // 2. Draw Profile Image with Green Border
        userProfile.photoUrl?.let { url ->
            val profileBitmap = fetchBitmap(url)
            if (profileBitmap != null) {
                val rawSize = if (template.photoSlot.size <= 0f) 220f else template.photoSlot.size
                val size = (rawSize / REF_WIDTH) * width
                val x = (template.photoSlot.x / REF_WIDTH) * width
                val y = (template.photoSlot.y / REF_HEIGHT) * height

                val circularBitmap = getCircularBitmap(profileBitmap)
                
                // Draw Green Border
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4CAF50")
                    style = Paint.Style.STROKE
                    strokeWidth = size * 0.05f
                }
                canvas.drawCircle(x, y, size / 2f, borderPaint)

                // Draw Image
                val destRect = RectF(x - size/2, y - size/2, x + size/2, y + size/2)
                canvas.drawBitmap(circularBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
            }
        }

        // 3. Draw Name
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            letterSpacing = 0.05f
        }
        
        val textX = (template.textSlot.x / REF_WIDTH) * width
        val textY = (template.textSlot.y / REF_HEIGHT) * height
        
        val fontMetrics = textPaint.fontMetrics
        val verticalCenterOffset = (fontMetrics.ascent + fontMetrics.descent) / 2
        
        canvas.drawText(userProfile.name, textX, textY - verticalCenterOffset, textPaint)

        bitmap
    }

    private suspend fun fetchBitmap(url: String): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = imageLoader.execute(request)
        return (result as? SuccessResult)?.drawable?.let { drawable ->
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun getCircularBitmap(srcBitmap: Bitmap): Bitmap {
        val size = Math.min(srcBitmap.width, srcBitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        
        val srcRect = Rect(
            (srcBitmap.width - size) / 2,
            (srcBitmap.height - size) / 2,
            (srcBitmap.width + size) / 2,
            (srcBitmap.height + size) / 2
        )
        canvas.drawBitmap(srcBitmap, srcRect, Rect(0, 0, size, size), paint)
        return output
    }
}
