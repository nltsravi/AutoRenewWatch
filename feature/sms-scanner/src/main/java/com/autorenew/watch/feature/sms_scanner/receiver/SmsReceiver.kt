package com.autorenew.watch.feature.sms_scanner.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                val messageBody = sms.displayMessageBody
                Log.d("SmsReceiver", "Received SMS from $sender: $messageBody")
                // TODO: Delegate processing to a Worker or background service
            }
        }
    }
}
