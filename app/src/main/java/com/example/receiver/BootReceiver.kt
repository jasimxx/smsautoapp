package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AutomationRepository
import com.example.service.MfsForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val repo = AutomationRepository(context.applicationContext)
            if (repo.isLiveMode.value) {
                MfsForegroundService.startService(context.applicationContext)
            }
        }
    }
}
