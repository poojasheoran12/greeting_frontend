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

    suspend fun render(template: Template, userProfile: UserProfile): Bitmap = withContext(Dispatchers.IO) {
        val width = 1080
        val height = 1920
        val headerHeight = (height * 0.12).toInt()
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)


        val headerPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), headerPaint)


        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 72f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.05f
        }
        val nameX = width / 2f
        val nameY = (headerHeight / 2f) - ((namePaint.descent() + namePaint.ascent()) / 2f)
        canvas.drawText(userProfile.name, nameX, nameY, namePaint)


        val bgBitmap = fetchBitmap(template.imageUrl)
        if (bgBitmap != null) {
            val destRect = Rect(0, headerHeight, width, height)
            canvas.drawBitmap(bgBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
        }


        userProfile.photoUrl?.let { url ->
            val profileBitmap = fetchBitmap(url)
            if (profileBitmap != null) {
                val size = (width * 0.25).toInt()
                val left = 48
                val top = headerHeight - (size / 2)
                
                val circularBitmap = getCircularBitmap(profileBitmap)
                

                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#4CAF50")
                    style = Paint.Style.STROKE
                    strokeWidth = size * 0.08f
                }
                canvas.drawCircle((left + size/2).toFloat(), (top + size/2).toFloat(), (size/2).toFloat(), borderPaint)


                val imageSize = (size * 0.94).toInt()
                val imageLeft = left + (size - imageSize)/2
                val imageTop = top + (size - imageSize)/2
                val destRectF = RectF(imageLeft.toFloat(), imageTop.toFloat(), (imageLeft + imageSize).toFloat(), (imageTop + imageSize).toFloat())
                canvas.drawBitmap(circularBitmap, null, destRectF, Paint(Paint.FILTER_BITMAP_FLAG))
            }
        }

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
