package com.autorenew.watch.feature.sms_scanner.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autorenew.watch.core.database.dao.SmsLogDao
import com.autorenew.watch.core.database.dao.SubscriptionDao
import com.autorenew.watch.core.database.entity.BillingCycle
import com.autorenew.watch.core.database.entity.RawSmsLogEntity
import com.autorenew.watch.core.database.entity.SubscriptionEntity
import com.autorenew.watch.feature.sms_scanner.parser.SmsRegexParser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoricalSmsScannerWorker(
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

            val contentResolver = applicationContext.contentResolver
            val uri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("_id", "address", "body", "date")
            
            val cursor = contentResolver.query(uri, projection, null, null, "date DESC")
            var newSubscriptionsFound = 0
            
            cursor?.use {
                val addressIdx = it.getColumnIndexOrThrow("address")
                val bodyIdx = it.getColumnIndexOrThrow("body")
                val dateIdx = it.getColumnIndexOrThrow("date")
                
                while (it.moveToNext()) {
                    val sender = it.getString(addressIdx) ?: ""
                    val body = it.getString(bodyIdx) ?: ""
                    val timestamp = it.getLong(dateIdx)
                    
                    val parsed = SmsRegexParser.parse(body, sender)
                    if (parsed != null && parsed.merchant != null && parsed.amount != null) {
                        // Check if we already have this subscription
                        val existing = subscriptionDao.getSubscriptionByMerchant(parsed.merchant)
                        if (existing != null) {
                            if (timestamp > existing.nextRenewalDate) {
                                val updated = existing.copy(
                                    estimatedAmount = parsed.amount,
                                    nextRenewalDate = timestamp
                                )
                                subscriptionDao.updateSubscription(updated)
                            }
                        } else {
                            val subscription = SubscriptionEntity(
                                merchantName = parsed.merchant,
                                estimatedAmount = parsed.amount,
                                billingCycle = BillingCycle.MONTHLY,
                                nextRenewalDate = timestamp,
                                category = "Unknown",
                                isActive = true
                            )
                            subscriptionDao.insertSubscription(subscription)
                            newSubscriptionsFound++
                        }
                        
                        // Save matching raw SMS log
                        val smsLog = RawSmsLogEntity(
                            sender = sender,
                            body = body,
                            timestamp = timestamp,
                            isProcessed = true
                        )
                        smsLogDao.insertSmsLog(smsLog)
                    }
                }
            }
            
            Log.d("HistoricalScanner", "Finished scanning inbox. Found $newSubscriptionsFound new subscriptions.")
            Result.success()
        } catch (e: Exception) {
            Log.e("HistoricalScanner", "Error scanning historical SMS", e)
            Result.failure()
        }
    }
}
