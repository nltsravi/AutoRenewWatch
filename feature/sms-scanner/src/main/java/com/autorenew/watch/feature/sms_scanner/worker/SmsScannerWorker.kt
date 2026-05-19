package com.autorenew.watch.feature.sms_scanner.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsScannerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // TODO: Read historical SMS from Telephony.Sms.Inbox
            // Apply regex patterns and populate database
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
