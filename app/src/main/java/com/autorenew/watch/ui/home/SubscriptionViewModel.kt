package com.autorenew.watch.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.autorenew.watch.core.database.dao.SmsLogDao
import com.autorenew.watch.core.database.dao.SubscriptionDao
import com.autorenew.watch.core.database.entity.BillingCycle
import com.autorenew.watch.core.database.entity.SubscriptionEntity
import com.autorenew.watch.feature.sms_scanner.parser.SmsRegexParser
import com.autorenew.watch.feature.sms_scanner.worker.HistoricalSmsScannerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    subscriptionDao: SubscriptionDao,
    private val smsLogDao: SmsLogDao
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    val subscriptions: StateFlow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pastDeductions: StateFlow<List<SubscriptionDeduction>> = smsLogDao.getAllSmsLogs()
        .map { logs ->
            logs.mapNotNull { log ->
                val parsed = SmsRegexParser.parse(log.body, log.sender)
                val merchant = parsed?.merchant
                val amount = parsed?.amount
                if (merchant != null && amount != null) {
                    SubscriptionDeduction(
                        id = log.id,
                        merchantName = merchant,
                        amount = amount,
                        timestamp = log.timestamp,
                        rawBody = log.body
                    )
                } else {
                    null
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val upcomingRenewals: StateFlow<List<UpcomingRenewal>> = subscriptions
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.isActive }
                .map { sub ->
                    val nextDate = calculateNextRenewal(sub.nextRenewalDate, sub.billingCycle, now)
                    UpcomingRenewal(
                        id = sub.id,
                        merchantName = sub.merchantName,
                        amount = sub.estimatedAmount,
                        billingCycle = sub.billingCycle,
                        originalDate = sub.nextRenewalDate,
                        nextRenewalDate = nextDate,
                        daysRemaining = calculateDaysRemaining(nextDate, now)
                    )
                }
                .sortedBy { it.nextRenewalDate }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun scanHistoricalSms(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val workRequest = OneTimeWorkRequestBuilder<HistoricalSmsScannerWorker>()
            .addTag("HistoricalSmsScanner")
            .build()
            
        _isScanning.value = true
        
        workManager.enqueueUniqueWork(
            "HistoricalSmsScannerUnique",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workRequest.id).collect { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.CANCELLED -> {
                            _isScanning.value = false
                        }
                        else -> {
                            _isScanning.value = true
                        }
                    }
                }
            }
        }
    }

    private fun calculateNextRenewal(baseDateMs: Long, billingCycle: BillingCycle, nowMs: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = baseDateMs }
        
        // If base date is in the future, it is already the next renewal date
        if (calendar.timeInMillis > nowMs) {
            return calendar.timeInMillis
        }
        
        var count = 0
        while (calendar.timeInMillis <= nowMs && count < 1000) {
            when (billingCycle) {
                BillingCycle.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                BillingCycle.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingCycle.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
            count++
        }
        return calendar.timeInMillis
    }

    private fun calculateDaysRemaining(nextRenewalDateMs: Long, nowMs: Long): Long {
        val diffMs = nextRenewalDateMs - nowMs
        return if (diffMs <= 0) {
            0L
        } else {
            // Convert to whole days, rounding up
            (diffMs + 24 * 60 * 60 * 1000 - 1) / (24 * 60 * 60 * 1000)
        }
    }
}
