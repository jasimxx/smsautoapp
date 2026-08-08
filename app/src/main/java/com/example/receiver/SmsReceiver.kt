package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Telephony
import com.example.data.AutomationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION || intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MFSAutomation::SmsProcessingWakeLock"
            )
            wakeLock?.acquire(25000L) // Hold wake lock for max 25 seconds for reliable webhook call

            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNotEmpty()) {
                val repository = AutomationRepository(context.applicationContext)
                if (repository.isLiveMode.value) {
                    val fullMessage = StringBuilder()
                    var sender = ""
                    for (msg in messages) {
                        sender = msg.displayOriginatingAddress ?: ""
                        fullMessage.append(msg.messageBody)
                    }
                    if (sender.isNotBlank() && fullMessage.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                repository.processIncomingSms(sender, fullMessage.toString())
                            } finally {
                                if (wakeLock?.isHeld == true) {
                                    wakeLock.release()
                                }
                            }
                        }
                    } else {
                        if (wakeLock?.isHeld == true) wakeLock.release()
                    }
                } else {
                    if (wakeLock?.isHeld == true) wakeLock.release()
                }
            } else {
                if (wakeLock?.isHeld == true) wakeLock.release()
            }
        }
    }
}
