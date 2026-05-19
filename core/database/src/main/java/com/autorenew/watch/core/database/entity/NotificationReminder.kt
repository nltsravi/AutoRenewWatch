package com.autorenew.watch.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_reminders")
data class NotificationReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subscriptionId: Long,
    val reminderDate: Long,
    val isDismissed: Boolean = false
)
