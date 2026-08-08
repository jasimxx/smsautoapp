package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawActivityDao {
    @Query("SELECT * FROM withdraw_activity ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<WithdrawActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: WithdrawActivity): Long

    @Update
    suspend fun updateActivity(activity: WithdrawActivity)

    @Query("SELECT * FROM withdraw_activity WHERE id = :id LIMIT 1")
    suspend fun getActivityById(id: Int): WithdrawActivity?

    @Query("DELETE FROM withdraw_activity")
    suspend fun clearAll()
}
