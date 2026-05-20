package com.autorenew.watch.ui.home

data class SubscriptionDeduction(
    val id: Long,
    val merchantName: String,
    val amount: Double,
    val timestamp: Long,
    val rawBody: String
)
