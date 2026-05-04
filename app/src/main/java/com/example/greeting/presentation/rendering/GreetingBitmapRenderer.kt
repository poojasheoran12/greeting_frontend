package com.example.greeting.presentation.rendering

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

    suspend fun render(template: Template, userProfile: UserProfile): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val templateWidth = 1080
            val templateHeight = 1920
            val headerHeight = 240 
            
            val totalWidth = templateWidth
            val totalHeight = templateHeight + headerHeight
            
            val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val barPaint = Paint().apply { color = Color.parseColor("#1A1A1A") }
            canvas.drawRect(0f, 0f, totalWidth.toFloat(), headerHeight.toFloat(), barPaint)

            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 90f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(userProfile.name, totalWidth / 2f, headerHeight / 2f + 30f, namePaint)

            val bgBitmap = fetchBitmap(template.imageUrl)
            if (bgBitmap != null) {
                val destRect = Rect(0, headerHeight, totalWidth, totalHeight)
                canvas.drawBitmap(bgBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
            }

            userProfile.photoUrl?.let { url ->
                val profileBitmap = fetchBitmap(url)
                if (profileBitmap != null) {
                    val size = 340f 
                    val margin = 60f
                    val centerX = margin + size / 2
                    val centerY = headerHeight.toFloat()
                    
                    val circularBitmap = getCircularBitmap(profileBitmap)
                    
                    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
                    canvas.drawCircle(centerX, centerY, size / 2 + 15, whitePaint)
                    
                    val destRectF = RectF(centerX - size/2, centerY - size/2, centerX + size/2, centerY + size/2)
                    canvas.drawBitmap(circularBitmap, null, destRectF, Paint(Paint.FILTER_BITMAP_FLAG))
                }
            }

            Result.success(bitmap)
        } catch (e: OutOfMemoryError) {
            Result.failure(Exception("Not enough memory to generate the greeting card.", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
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
