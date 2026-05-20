package com.autorenew.watch.ui.home

import com.autorenew.watch.core.database.entity.BillingCycle

data class UpcomingRenewal(
    val id: Long,
    val merchantName: String,
    val amount: Double,
    val billingCycle: BillingCycle,
    val originalDate: Long,
    val nextRenewalDate: Long,
    val daysRemaining: Long
)
