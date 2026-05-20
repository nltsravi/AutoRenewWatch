package com.autorenew.watch.feature.sms_scanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.autorenew.watch.core.database.dao.SmsLogDao
import com.autorenew.watch.core.database.entity.RawSmsLogEntity
import com.autorenew.watch.feature.sms_scanner.worker.SmsScannerWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsLogDao: SmsLogDao

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val pendingResult = goAsync()
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            scope.launch {
                try {
                    for (sms in messages) {
                        val sender = sms.displayOriginatingAddress ?: ""
                        val messageBody = sms.displayMessageBody ?: ""
                        
                        val logEntity = RawSmsLogEntity(
                            sender = sender,
                            body = messageBody,
                            timestamp = sms.timestampMillis
                        )
                        smsLogDao.insertSmsLog(logEntity)
                    }
                    
                    // Trigger WorkManager
                    val workRequest = OneTimeWorkRequestBuilder<SmsScannerWorker>().build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                    
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error processing SMS", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
