package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "withdraw_activity")
data class WithdrawActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleId: Int,
    val ruleTitle: String,
    val provider: String,
    val incomingAmount: Double,
    val withdrawnAmount: Double,
    val feeAmount: Double,
    val netPayout: Double,
    val destination: String,
    val status: String, // COMPLETED, PENDING_PIN, SKIPPED, FAILED
    val failureReason: String? = null,
    val trxId: String,
    val timestamp: Long = System.currentTimeMillis()
)
