package com.autorenew.watch.feature.sms_scanner.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autorenew.watch.core.database.dao.SmsLogDao
import com.autorenew.watch.core.database.dao.SubscriptionDao
import com.autorenew.watch.core.database.entity.BillingCycle
import com.autorenew.watch.core.database.entity.SubscriptionEntity
import com.autorenew.watch.feature.sms_scanner.parser.SmsRegexParser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsScannerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun smsLogDao(): SmsLogDao
        fun subscriptionDao(): SubscriptionDao
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                WorkerEntryPoint::class.java
            )
            val smsLogDao = entryPoint.smsLogDao()
            val subscriptionDao = entryPoint.subscriptionDao()

            val unprocessedLogs = smsLogDao.getUnprocessedSmsLogs()
            if (unprocessedLogs.isEmpty()) {
                return@withContext Result.success()
            }

            val processedIds = mutableListOf<Long>()

            for (log in unprocessedLogs) {
                val parsed = SmsRegexParser.parse(log.body, log.sender)
                if (parsed != null && parsed.merchant != null && parsed.amount != null) {
                    // We found a subscription or potential recurring debit
                    val subscription = SubscriptionEntity(
                        merchantName = parsed.merchant,
                        estimatedAmount = parsed.amount,
                        billingCycle = BillingCycle.MONTHLY, // Defaulting for now
                        nextRenewalDate = log.timestamp, // Better logic would estimate next date
                        category = "Unknown",
                        isActive = true
                    )
                    subscriptionDao.insertSubscription(subscription)
                }
                processedIds.add(log.id)
            }

            if (processedIds.isNotEmpty()) {
                smsLogDao.markAsProcessed(processedIds)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SmsScannerWorker", "Error processing SMS logs", e)
            Result.failure()
        }
    }
}

