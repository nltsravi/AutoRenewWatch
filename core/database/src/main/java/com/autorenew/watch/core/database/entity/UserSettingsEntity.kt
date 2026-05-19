package com.autorenew.watch.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val notificationsEnabled: Boolean = true,
    val currencySymbol: String = "₹"
)
