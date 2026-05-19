package com.autorenew.watch.feature.sms_scanner.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsRegexParserTest {

    @Test
    fun testParse_AutoDebitNetflix() {
        val sms = "Your auto-debit of Rs. 499.00 for NETFLIX on 15/05/2024 was successful."
        val result = SmsRegexParser.parse(sms, "AD-HDFC")
        
        requireNotNull(result)
        assertEquals(499.00, result.amount)
        assertEquals("15/05/2024", result.dateString)
        assertTrue(result.isAutoDebit)
        assertEquals("NETFLIX", result.merchant)
    }
}
