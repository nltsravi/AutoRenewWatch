package com.autorenew.watch.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.autorenew.watch.core.database.entity.RawSmsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM raw_sms_logs ORDER BY timestamp DESC")
    fun getAllSmsLogs(): Flow<List<RawSmsLogEntity>>

    @Query("SELECT * FROM raw_sms_logs WHERE isProcessed = 0 ORDER BY timestamp ASC")
    suspend fun getUnprocessedSmsLogs(): List<RawSmsLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(smsLog: RawSmsLogEntity): Long

    @Update
    suspend fun updateSmsLog(smsLog: RawSmsLogEntity)

    @Query("UPDATE raw_sms_logs SET isProcessed = 1 WHERE id IN (:ids)")
    suspend fun markAsProcessed(ids: List<Long>)
}
