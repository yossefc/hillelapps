package com.example.hillelapps.presentation

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.confirmationDao()
        val unsyncedConfirmations = dao.getUnsyncedConfirmations()

        val firebaseDatabase = Firebase.database
        val reference = firebaseDatabase.getReference("confirmations")

        for (confirmation in unsyncedConfirmations) {
            try {
                reference.push().setValue(confirmation.timestamp).await()
                dao.markAsSynced(confirmation.id)
            } catch (e: Exception) {
                Log.e("SyncWorker", "Failed to sync confirmation", e)
                return Result.retry()
            }
        }

        return Result.success()
    }
}