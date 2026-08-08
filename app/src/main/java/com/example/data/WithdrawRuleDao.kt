package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawRuleDao {
    @Query("SELECT * FROM withdraw_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<WithdrawRule>>

    @Query("SELECT * FROM withdraw_rules WHERE isEnabled = 1")
    suspend fun getEnabledRulesList(): List<WithdrawRule>

    @Query("SELECT * FROM withdraw_rules WHERE id = :id")
    suspend fun getRuleById(id: Int): WithdrawRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: WithdrawRule): Long

    @Update
    suspend fun updateRule(rule: WithdrawRule)

    @Query("DELETE FROM withdraw_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Int)
}
