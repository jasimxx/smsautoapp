package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.regex.Pattern

import android.content.Intent
import android.net.Uri
import com.example.network.WebhookManager

data class ParsedSmsResult(
    val detectedProvider: MfsProvider,
    val amount: Double,
    val trxId: String,
    val isPaymentOrCashIn: Boolean,
    val rawSender: String,
    val rawMessage: String
)

data class ProcessResult(
    val smsLog: SmsLog,
    val activity: WithdrawActivity?,
    val matchedRule: WithdrawRule?,
    val summaryMessage: String,
    val ussdDialCode: String? = null,
    val websiteWebhookStatus: String? = null
)

class AutomationRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val ruleDao = database.withdrawRuleDao()
    private val smsLogDao = database.smsLogDao()
    private val activityDao = database.withdrawActivityDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sms_withdraw_prefs", Context.MODE_PRIVATE)

    val rules: Flow<List<WithdrawRule>> = ruleDao.getAllRules()
    val smsLogs: Flow<List<SmsLog>> = smsLogDao.getAllSmsLogs()
    val activities: Flow<List<WithdrawActivity>> = activityDao.getAllActivities()

    // Mode state
    private val _isLiveMode = MutableStateFlow(prefs.getBoolean("live_mode", false))
    val isLiveMode: StateFlow<Boolean> = _isLiveMode.asStateFlow()

    private val _autoPinVault = MutableStateFlow(prefs.getString("auto_pin_vault", "1234") ?: "1234")
    val autoPinVault: StateFlow<String> = _autoPinVault.asStateFlow()

    private val _bkashSimSlot = MutableStateFlow(prefs.getInt("bkash_sim_slot", 0)) // 0: SIM 1, 1: SIM 2
    val bkashSimSlot: StateFlow<Int> = _bkashSimSlot.asStateFlow()

    private val _nagadSimSlot = MutableStateFlow(prefs.getInt("nagad_sim_slot", 1)) // 1: SIM 2
    val nagadSimSlot: StateFlow<Int> = _nagadSimSlot.asStateFlow()

    private val _rocketSimSlot = MutableStateFlow(prefs.getInt("rocket_sim_slot", 0))
    val rocketSimSlot: StateFlow<Int> = _rocketSimSlot.asStateFlow()

    private val _webhookUrl = MutableStateFlow(prefs.getString("webhook_url", "https://example.com/api/mfs-webhook") ?: "https://example.com/api/mfs-webhook")
    val webhookUrl: StateFlow<String> = _webhookUrl.asStateFlow()

    private val _apiKey = MutableStateFlow(prefs.getString("api_key", "sec_token_998877") ?: "sec_token_998877")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    // Mock balances
    private val _bkashBalance = MutableStateFlow(18500.0)
    val bkashBalance: StateFlow<Double> = _bkashBalance.asStateFlow()

    private val _nagadBalance = MutableStateFlow(12200.0)
    val nagadBalance: StateFlow<Double> = _nagadBalance.asStateFlow()

    private val _rocketBalance = MutableStateFlow(9400.0)
    val rocketBalance: StateFlow<Double> = _rocketBalance.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultRulesIfEmpty()
        }
        if (_isLiveMode.value) {
            try {
                com.example.service.MfsForegroundService.startService(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setLiveMode(enabled: Boolean) {
        prefs.edit().putBoolean("live_mode", enabled).apply()
        _isLiveMode.value = enabled
        try {
            if (enabled) {
                com.example.service.MfsForegroundService.startService(context)
            } else {
                com.example.service.MfsForegroundService.stopService(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setAutoPinVault(pin: String) {
        prefs.edit().putString("auto_pin_vault", pin).apply()
        _autoPinVault.value = pin
    }

    fun setWebhookUrl(url: String) {
        prefs.edit().putString("webhook_url", url).apply()
        _webhookUrl.value = url
    }

    fun setApiKey(key: String) {
        prefs.edit().putString("api_key", key).apply()
        _apiKey.value = key
    }

    fun setBkashSimSlot(slot: Int) {
        prefs.edit().putInt("bkash_sim_slot", slot).apply()
        _bkashSimSlot.value = slot
    }

    fun setNagadSimSlot(slot: Int) {
        prefs.edit().putInt("nagad_sim_slot", slot).apply()
        _nagadSimSlot.value = slot
    }

    fun setRocketSimSlot(slot: Int) {
        prefs.edit().putInt("rocket_sim_slot", slot).apply()
        _rocketSimSlot.value = slot
    }

    fun buildUssdString(provider: String, targetNumber: String, amount: Double, pin: String): String {
        val cleanNumber = targetNumber.filter { it.isDigit() }
        val amtInt = amount.toInt()
        val pinPart = if (pin.isNotBlank()) "*$pin#" else "#"
        return when (provider.uppercase(Locale.ROOT)) {
            "BKASH" -> "*247*1*1*$cleanNumber*$amtInt$pinPart"
            "NAGAD" -> "*167*1*1*$cleanNumber*$amtInt$pinPart"
            "ROCKET" -> "*322*1*1*$cleanNumber*$amtInt$pinPart"
            else -> "*247#*"
        }
    }

    fun getSimSlotForProvider(provider: String): Int {
        return when (provider.uppercase(Locale.ROOT)) {
            "BKASH" -> _bkashSimSlot.value
            "NAGAD" -> _nagadSimSlot.value
            "ROCKET" -> _rocketSimSlot.value
            else -> 0
        }
    }

    fun triggerUssdDialIntent(ussdCode: String, simSlot: Int = 0) {
        try {
            val encodedUssd = Uri.encode(ussdCode)
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedUssd")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (simSlot >= 0) {
                    putExtra("com.android.phone.extra.slot", simSlot)
                    putExtra("simSlot", simSlot)
                    putExtra("slot", simSlot)
                    putExtra("sim_slot", simSlot)
                    putExtra("subscription", simSlot)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(ussdCode)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private suspend fun seedDefaultRulesIfEmpty() {
        val enabledList = ruleDao.getEnabledRulesList()
        if (enabledList.isEmpty()) {
            val defaultBkashRule = WithdrawRule(
                title = "bKash Merchant Cash-Out",
                provider = "BKASH",
                triggerType = "SMS_MATCH",
                thresholdAmount = 2000.0,
                smsKeyword = "Received",
                destinationType = "AGENT",
                destinationDetail = "01711223344 (Agent 1)",
                maxPerTx = 25000.0,
                dailyLimit = 100000.0,
                requiresPinConfirmation = false,
                isEnabled = true
            )
            val defaultNagadRule = WithdrawRule(
                title = "Nagad Auto-Withdraw > 5k",
                provider = "NAGAD",
                triggerType = "THRESHOLD",
                thresholdAmount = 5000.0,
                smsKeyword = "Received",
                destinationType = "BANK",
                destinationDetail = "Dutch-Bangla Bank (A/C: 104-***-891)",
                maxPerTx = 30000.0,
                dailyLimit = 150000.0,
                requiresPinConfirmation = true,
                isEnabled = true
            )
            val defaultRocketRule = WithdrawRule(
                title = "Rocket Daily Batch Rule",
                provider = "ROCKET",
                triggerType = "SCHEDULED",
                thresholdAmount = 1000.0,
                smsKeyword = "Received",
                destinationType = "AGENT",
                destinationDetail = "01999887766 (Rocket Agent)",
                maxPerTx = 20000.0,
                dailyLimit = 80000.0,
                requiresPinConfirmation = false,
                isEnabled = true
            )
            ruleDao.insertRule(defaultBkashRule)
            ruleDao.insertRule(defaultNagadRule)
            ruleDao.insertRule(defaultRocketRule)
        }
    }

    private suspend fun seedSampleLogsIfEmpty() {
        // Only seed initial sample logs if database has none
        // We will do a simple check or let user trigger new ones
    }

    suspend fun addRule(rule: WithdrawRule): Long {
        return ruleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: WithdrawRule) {
        ruleDao.updateRule(rule)
    }

    suspend fun deleteRule(id: Int) {
        ruleDao.deleteRuleById(id)
    }

    suspend fun clearLogsAndActivities() {
        smsLogDao.clearAll()
        activityDao.clearAll()
    }

    fun parseSmsText(sender: String, message: String): ParsedSmsResult {
        val lowerSender = sender.lowercase(Locale.ROOT)
        val lowerMsg = message.lowercase(Locale.ROOT)

        val provider = when {
            lowerSender.contains("bkash") || lowerMsg.contains("bkash") -> MfsProvider.BKASH
            lowerSender.contains("nagad") || lowerMsg.contains("nagad") -> MfsProvider.NAGAD
            lowerSender.contains("rocket") || lowerMsg.contains("rocket") || lowerMsg.contains("dbbl") -> MfsProvider.ROCKET
            else -> MfsProvider.BKASH
        }

        // Amount extraction regex for BDT/Tk amounts
        var extractedAmount = 0.0
        val amountPattern = Pattern.compile("(?:tk|bdt|amount|received|sum)\\s*:?\\s*([0-9,]+\\.?[0-9]*)", Pattern.CASE_INSENSITIVE)
        val matcher = amountPattern.matcher(message)
        if (matcher.find()) {
            val rawAmt = matcher.group(1)?.replace(",", "") ?: "0"
            extractedAmount = rawAmt.toDoubleOrNull() ?: 0.0
        } else {
            // fallback look for isolated number after "received"
            val numPattern = Pattern.compile("([0-9,]+\\.[0-9]{2})")
            val numMatcher = numPattern.matcher(message)
            if (numMatcher.find()) {
                val rawAmt = numMatcher.group(1)?.replace(",", "") ?: "0"
                extractedAmount = rawAmt.toDoubleOrNull() ?: 0.0
            }
        }

        // TrxID extraction
        var trxId = "TRX" + System.currentTimeMillis().toString().takeLast(8)
        val trxPattern = Pattern.compile("(?:trxid|txn\\s*id|txnid|id)[:\\s]*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE)
        val trxMatcher = trxPattern.matcher(message)
        if (trxMatcher.find()) {
            trxId = trxMatcher.group(1) ?: trxId
        }

        val isPayment = lowerMsg.contains("received") || lowerMsg.contains("cash in") || lowerMsg.contains("credited") || lowerMsg.contains("deposit")

        return ParsedSmsResult(
            detectedProvider = provider,
            amount = extractedAmount,
            trxId = trxId,
            isPaymentOrCashIn = isPayment,
            rawSender = sender,
            rawMessage = message
        )
    }

    suspend fun processIncomingSms(sender: String, message: String): ProcessResult {
        val parsed = parseSmsText(sender, message)
        val enabledRules = ruleDao.getEnabledRulesList()

        val matchingRule = enabledRules.firstOrNull { rule ->
            rule.provider.equals(parsed.detectedProvider.name, ignoreCase = true) &&
                    (rule.triggerType == "SMS_MATCH" || rule.triggerType == "THRESHOLD") &&
                    parsed.amount >= rule.thresholdAmount &&
                    (rule.smsKeyword.isBlank() || message.contains(rule.smsKeyword, ignoreCase = true))
        }

        if (matchingRule == null) {
            val log = SmsLog(
                sender = sender,
                rawMessage = message,
                detectedProvider = parsed.detectedProvider.name,
                extractedAmount = parsed.amount,
                extractedTrxId = parsed.trxId,
                status = "IGNORED"
            )
            smsLogDao.insertSmsLog(log)
            return ProcessResult(
                smsLog = log,
                activity = null,
                matchedRule = null,
                summaryMessage = "SMS logged, but no active rule matched threshold/keywords."
            )
        }

        // Rule matched! Check limits
        if (parsed.amount > matchingRule.maxPerTx) {
            val log = SmsLog(
                sender = sender,
                rawMessage = message,
                detectedProvider = parsed.detectedProvider.name,
                extractedAmount = parsed.amount,
                extractedTrxId = parsed.trxId,
                status = "FAILED"
            )
            val activity = WithdrawActivity(
                ruleId = matchingRule.id,
                ruleTitle = matchingRule.title,
                provider = matchingRule.provider,
                incomingAmount = parsed.amount,
                withdrawnAmount = 0.0,
                feeAmount = 0.0,
                netPayout = 0.0,
                destination = matchingRule.destinationDetail,
                status = "SKIPPED",
                failureReason = "Exceeds max per tx limit (${matchingRule.maxPerTx} BDT)",
                trxId = parsed.trxId
            )
            smsLogDao.insertSmsLog(log)
            activityDao.insertActivity(activity)
            return ProcessResult(
                smsLog = log,
                activity = activity,
                matchedRule = matchingRule,
                summaryMessage = "Matched rule '${matchingRule.title}' but skipped: Amount exceeds max limit."
            )
        }

        val feePercent = parsed.detectedProvider.defaultFeePercentage
        val feeAmount = (parsed.amount * feePercent) / 100.0
        val netPayout = parsed.amount - feeAmount

        // Auto Deposit Process
        val depositWebhookResult = WebhookManager.sendDepositWebhook(
            webhookUrl = _webhookUrl.value,
            apiKey = _apiKey.value,
            trxId = parsed.trxId,
            amount = parsed.amount,
            senderNumber = sender,
            provider = parsed.detectedProvider.name
        )

        // Automatically credit balance on deposit received
        when (parsed.detectedProvider) {
            MfsProvider.BKASH -> _bkashBalance.value += parsed.amount
            MfsProvider.NAGAD -> _nagadBalance.value += parsed.amount
            MfsProvider.ROCKET -> _rocketBalance.value += parsed.amount
        }

        // Generate USSD code for manual cash-out
        val ussdCode = buildUssdString(
            provider = matchingRule.provider,
            targetNumber = matchingRule.destinationDetail,
            amount = parsed.amount,
            pin = _autoPinVault.value
        )

        // Manual Withdrawal required - status PENDING_MANUAL
        val activityStatus = "PENDING_MANUAL"

        val activity = WithdrawActivity(
            ruleId = matchingRule.id,
            ruleTitle = matchingRule.title,
            provider = matchingRule.provider,
            incomingAmount = parsed.amount,
            withdrawnAmount = parsed.amount,
            feeAmount = feeAmount,
            netPayout = netPayout,
            destination = matchingRule.destinationDetail,
            status = activityStatus,
            failureReason = null,
            trxId = parsed.trxId
        )

        val log = SmsLog(
            sender = sender,
            rawMessage = message,
            detectedProvider = parsed.detectedProvider.name,
            extractedAmount = parsed.amount,
            extractedTrxId = parsed.trxId,
            status = "PROCESSED"
        )

        smsLogDao.insertSmsLog(log)
        val activityId = activityDao.insertActivity(activity).toInt()

        // Trigger Notification with Loud Sound Alert for Withdrawal Request
        com.example.util.NotificationHelper.showWithdrawRequestNotification(
            context = context,
            title = "🚨 নতুন উইথড্র রিকোয়েস্ট এসেছে!",
            message = "৳${String.format(Locale.US, "%,.0f", parsed.amount)} (${matchingRule.provider}) - টার্গেট: ${matchingRule.destinationDetail}. অ্যাপে এক্সেপ্ট করুন।",
            activityId = activityId
        )

        val statusMsg = "✅ অটো-ডিপোজিট কনফার্মড! 🚨 উইথড্র রিকোয়েস্ট এসেছে (ম্যানুয়াল এক্সেপ্ট প্রয়োজন)।"

        return ProcessResult(
            smsLog = log,
            activity = activity.copy(id = activityId),
            matchedRule = matchingRule,
            summaryMessage = statusMsg,
            ussdDialCode = ussdCode,
            websiteWebhookStatus = depositWebhookResult.second
        )
    }

    suspend fun executeManualWithdrawal(activityId: Int) {
        val activity = activityDao.getActivityById(activityId) ?: return
        if (activity.status == "COMPLETED") return

        val ussdCode = buildUssdString(
            provider = activity.provider,
            targetNumber = activity.destination,
            amount = activity.withdrawnAmount,
            pin = _autoPinVault.value
        )

        // Send Withdraw Confirmation Webhook
        val withdrawWebhookResult = WebhookManager.sendWithdrawWebhook(
            webhookUrl = _webhookUrl.value,
            apiKey = _apiKey.value,
            trxId = activity.trxId,
            amount = activity.withdrawnAmount,
            fee = activity.feeAmount,
            netPayout = activity.netPayout,
            destinationNumber = activity.destination,
            provider = activity.provider,
            ussdDialed = "MANUAL_SEND ($ussdCode)",
            status = "COMPLETED"
        )

        // Update Database Activity Status to COMPLETED
        val updatedActivity = activity.copy(status = "COMPLETED")
        activityDao.updateActivity(updatedActivity)
    }
}
