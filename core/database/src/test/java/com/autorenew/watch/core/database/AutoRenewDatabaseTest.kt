package com.autorenew.watch.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.autorenew.watch.core.database.dao.SmsLogDao
import com.autorenew.watch.core.database.dao.SubscriptionDao
import com.autorenew.watch.core.database.entity.BillingCycle
import com.autorenew.watch.core.database.entity.RawSmsLogEntity
import com.autorenew.watch.core.database.entity.SubscriptionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoRenewDatabaseTest {
    private lateinit var db: AutoRenewDatabase
    private lateinit var subscriptionDao: SubscriptionDao
    private lateinit var smsLogDao: SmsLogDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AutoRenewDatabase::class.java
        ).build()
        subscriptionDao = db.subscriptionDao()
        smsLogDao = db.smsLogDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadSubscription() = runBlocking {
        val subscription = SubscriptionEntity(
            merchantName = "Netflix",
            estimatedAmount = 649.0,
            billingCycle = BillingCycle.MONTHLY,
            nextRenewalDate = 1680000000L,
            category = "Entertainment"
        )
        val id = subscriptionDao.insertSubscription(subscription)
        val allSubscriptions = subscriptionDao.getAllSubscriptions().first()
        
        assertTrue(allSubscriptions.size == 1)
        assertEquals("Netflix", allSubscriptions[0].merchantName)
        assertEquals(BillingCycle.MONTHLY, allSubscriptions[0].billingCycle)
    }

    @Test
    fun insertAndReadSmsLog() = runBlocking {
        val smsLog = RawSmsLogEntity(
            sender = "AD-HDFC",
            body = "Your a/c no. XX is debited for Rs.649.00 on 20-05-26 by Netflix",
            timestamp = 1680000000L
        )
        val id = smsLogDao.insertSmsLog(smsLog)
        
        val unprocessed = smsLogDao.getUnprocessedSmsLogs()
        assertTrue(unprocessed.size == 1)
        
        smsLogDao.markAsProcessed(listOf(id))
        
        val unprocessedAfter = smsLogDao.getUnprocessedSmsLogs()
        assertTrue(unprocessedAfter.isEmpty())
    }
}
