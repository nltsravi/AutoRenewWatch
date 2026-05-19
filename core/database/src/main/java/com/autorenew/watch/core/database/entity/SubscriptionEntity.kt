package com.autorenew.watch.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val estimatedAmount: Double,
    val interval: String, // Weekly, Monthly, Annual
    val nextRenewalDate: Long,
    val category: String,
    val isActive: Boolean = true
)
