package com.autorenew.watch.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autorenew.watch.core.database.dao.SubscriptionDao
import com.autorenew.watch.core.database.dao.TransactionPatternDao
import com.autorenew.watch.core.database.entity.NotificationReminder
import com.autorenew.watch.core.database.entity.SubscriptionEntity
import com.autorenew.watch.core.database.entity.TransactionPattern
import com.autorenew.watch.core.database.entity.UserSettingsEntity

@Database(
    entities = [
        SubscriptionEntity::class,
        TransactionPattern::class,
        NotificationReminder::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AutoRenewDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun transactionPatternDao(): TransactionPatternDao
}
