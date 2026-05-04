package com.example.greeting.core.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

import android.util.Log

class CacheCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val imageDir = File(applicationContext.cacheDir, "images")
            if (imageDir.exists() && imageDir.isDirectory) {
                imageDir.listFiles()?.forEach { file ->
                    if (System.currentTimeMillis() - file.lastModified() > MAX_CACHE_AGE_MS) {
                        val deleted = file.delete()
                        if (deleted) {
                            Log.d(TAG, "Deleted old cache file: ${file.name}")
                        } else {
                            Log.e(TAG, "Failed to delete cache file: ${file.name}")
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failure: Error during cache cleanup", e)
            Result.failure()
        }
    }

    private companion object {
        const val TAG = "CacheCleanupWorker"
        const val MAX_CACHE_AGE_MS = 24L * 60 * 60 * 1000
    }
}
