package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MfsProvider(val displayName: String, val defaultFeePercentage: Double, val brandColorHex: Long) {
    BKASH("bKash", 1.85, 0xE2136EL),
    NAGAD("Nagad", 1.25, 0xF7921EL),
    ROCKET("Rocket", 1.80, 0x8C3494L);

    companion object {
        fun fromString(str: String): MfsProvider {
            return when (str.uppercase()) {
                "BKASH" -> BKASH
                "NAGAD" -> NAGAD
                "ROCKET" -> ROCKET
                else -> BKASH
            }
        }
    }
}

enum class TriggerType(val title: String) {
    THRESHOLD("Balance Threshold"),
    SMS_MATCH("SMS Payment Received"),
    SCHEDULED("Daily/Weekly Batch")
}

enum class DestinationType(val label: String) {
    AGENT("Agent Cash-Out Number"),
    BANK("Bank Account Transfer")
}

@Entity(tableName = "withdraw_rules")
data class WithdrawRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val provider: String, // BKASH, NAGAD, ROCKET
    val triggerType: String, // THRESHOLD, SMS_MATCH, SCHEDULED
    val thresholdAmount: Double = 1000.0,
    val smsKeyword: String = "Received",
    val destinationType: String = "AGENT", // AGENT, BANK
    val destinationDetail: String = "01711000000",
    val maxPerTx: Double = 25000.0,
    val dailyLimit: Double = 100000.0,
    val requiresPinConfirmation: Boolean = false,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
