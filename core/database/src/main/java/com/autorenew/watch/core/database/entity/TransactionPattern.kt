package com.autorenew.watch.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_patterns")
data class TransactionPattern(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parsedSenderId: String,
    val rawSmsSnippet: String,
    val loggedDate: Long,
    val associatedAmount: Double
)
