package com.autorenew.watch.core.database.di

import android.content.Context
import androidx.room.Room
import com.autorenew.watch.core.database.AutoRenewDatabase
import com.autorenew.watch.core.database.dao.SmsLogDao
import com.autorenew.watch.core.database.dao.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AutoRenewDatabase {
        return Room.databaseBuilder(
            context,
            AutoRenewDatabase::class.java,
            "autorenew_watch.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSubscriptionDao(database: AutoRenewDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }

    @Provides
    fun provideSmsLogDao(database: AutoRenewDatabase): SmsLogDao {
        return database.smsLogDao()
    }
}
