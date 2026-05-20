package com.autorenew.watch.core.database

import androidx.room.TypeConverter
import com.autorenew.watch.core.database.entity.BillingCycle

class Converters {
    @TypeConverter
    fun toBillingCycle(value: String): BillingCycle {
        return enumValueOf<BillingCycle>(value)
    }

    @TypeConverter
    fun fromBillingCycle(billingCycle: BillingCycle): String {
        return billingCycle.name
    }
}
