package com.example.greeting.core.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.greeting.domain.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ProfilePhotoUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uid = inputData.getString(KEY_UID) ?: return Result.failure()
        
        Log.d(TAG, "Starting photo sync for user: $uid")
        
        return try {
            userRepository.syncProfilePhoto(uid).fold(
                onSuccess = {
                    Log.d(TAG, "Successfully synced photo for $uid")
                    Result.success()
                },
                onFailure = { e ->
                    Log.e(TAG, "Failed to sync photo for $uid: ${e.message}")
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Critical worker failure for $uid", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "ProfilePhotoUploadWorker"
        const val KEY_UID = "key_uid"
    }
}
