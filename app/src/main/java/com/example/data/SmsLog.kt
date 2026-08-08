package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_logs")
data class SmsLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val rawMessage: String,
    val detectedProvider: String, // BKASH, NAGAD, ROCKET, UNKNOWN
    val extractedAmount: Double,
    val extractedTrxId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // PROCESSED, IGNORED, FAILED
)
