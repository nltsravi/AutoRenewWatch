package com.autorenew.watch.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantName: String,
    val estimatedAmount: Double,
    val billingCycle: BillingCycle,
    val nextRenewalDate: Long,
    val category: String,
    val isActive: Boolean = true
)
