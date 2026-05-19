package com.autorenew.watch.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autorenew.watch.core.database.entity.TransactionPattern
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionPatternDao {
    @Query("SELECT * FROM transaction_patterns")
    fun getAllPatterns(): Flow<List<TransactionPattern>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: TransactionPattern): Long
}
