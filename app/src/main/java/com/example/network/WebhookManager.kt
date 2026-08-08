package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object WebhookManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    fun generateSecureApiKey(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        val hex = bytes.joinToString("") { "%02x".format(it) }
        return "sec_live_$hex"
    }

    private fun computeHmacSha256(secret: String, message: String): String {
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(secretKey)
            val hmacBytes = mac.doFinal(message.toByteArray(Charsets.UTF_8))
            hmacBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("WebhookManager", "HMAC calculation failed", e)
            ""
        }
    }

    suspend fun sendDepositWebhook(
        webhookUrl: String,
        apiKey: String,
        trxId: String,
        amount: Double,
        senderNumber: String,
        provider: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank()) {
            return@withContext Pair(true, "Simulated Webhook: No custom URL set. Deposit reported successfully.")
        }

        try {
            val timestamp = System.currentTimeMillis()
            val json = JSONObject().apply {
                put("event", "DEPOSIT_RECEIVED")
                put("trx_id", trxId)
                put("amount", amount)
                put("sender_number", senderNumber)
                put("provider", provider)
                put("timestamp", timestamp)
            }

            val payload = json.toString()
            val mediaType = "application/json".toMediaType()
            val body = payload.toRequestBody(mediaType)

            val signature = if (apiKey.isNotBlank()) computeHmacSha256(apiKey, "$timestamp.$payload") else ""

            val requestBuilder = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .header("Content-Type", "application/json")
                .header("User-Agent", "MFS-Withdraw-Automation/2.0 (Android; Secure-Webhook)")

            if (apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
                requestBuilder.header("X-API-Key", apiKey)
                requestBuilder.header("X-Timestamp", timestamp.toString())
                if (signature.isNotBlank()) {
                    requestBuilder.header("X-Signature", "sha256=$signature")
                }
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    Pair(true, "Website Webhook Confirmed! HTTP ${response.code}")
                } else {
                    Pair(false, "Website Webhook Failed: HTTP ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("WebhookManager", "Failed to send deposit webhook", e)
            Pair(false, "Webhook Network Error: ${e.localizedMessage}")
        }
    }

    suspend fun sendWithdrawWebhook(
        webhookUrl: String,
        apiKey: String,
        trxId: String,
        amount: Double,
        fee: Double,
        netPayout: Double,
        destinationNumber: String,
        provider: String,
        ussdDialed: String,
        status: String
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank()) {
            return@withContext Pair(true, "Simulated Webhook: No custom URL set. Withdraw reported successfully.")
        }

        try {
            val timestamp = System.currentTimeMillis()
            val json = JSONObject().apply {
                put("event", "WITHDRAW_EXECUTED")
                put("trx_id", trxId)
                put("amount", amount)
                put("fee", fee)
                put("net_payout", netPayout)
                put("destination_number", destinationNumber)
                put("provider", provider)
                put("ussd_command", ussdDialed)
                put("status", status)
                put("timestamp", timestamp)
            }

            val payload = json.toString()
            val mediaType = "application/json".toMediaType()
            val body = payload.toRequestBody(mediaType)

            val signature = if (apiKey.isNotBlank()) computeHmacSha256(apiKey, "$timestamp.$payload") else ""

            val requestBuilder = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .header("Content-Type", "application/json")
                .header("User-Agent", "MFS-Withdraw-Automation/2.0 (Android; Secure-Webhook)")

            if (apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
                requestBuilder.header("X-API-Key", apiKey)
                requestBuilder.header("X-Timestamp", timestamp.toString())
                if (signature.isNotBlank()) {
                    requestBuilder.header("X-Signature", "sha256=$signature")
                }
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    Pair(true, "Website Withdrawal Confirmed! HTTP ${response.code}")
                } else {
                    Pair(false, "Website Webhook Failed: HTTP ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("WebhookManager", "Failed to send withdraw webhook", e)
            Pair(false, "Webhook Network Error: ${e.localizedMessage}")
        }
    }
}
