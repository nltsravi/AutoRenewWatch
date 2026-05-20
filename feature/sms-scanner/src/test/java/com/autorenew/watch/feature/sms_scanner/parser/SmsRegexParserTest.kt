package com.autorenew.watch.feature.sms_scanner.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals("Netflix", result.merchant) // Note: parser uses knownMerchants which is "Netflix" now
    }

    @Test
    fun testParse_StandardDebit() {
        val sms = "Your a/c no. XX is debited for Rs.649.00 on 20-05-26 by Netflix"
        val result = SmsRegexParser.parse(sms, "AD-BANK")
        
        requireNotNull(result)
        assertEquals(649.00, result.amount)
        assertEquals("20-05-26", result.dateString)
        assertFalse(result.isAutoDebit) // Debited is standard debit, not auto debit, unless keywords match
        assertEquals("Netflix", result.merchant)
    }

    @Test
    fun testParse_SubscriptionKeyword() {
        val sms = "INR 149 spent on Amazon Prime on 10/12/23"
        val result = SmsRegexParser.parse(sms, "AD-ICICI")
        
        requireNotNull(result)
        assertEquals(149.00, result.amount)
        assertEquals("10/12/23", result.dateString)
        assertFalse(result.isAutoDebit) 
        assertEquals("Amazon Prime", result.merchant)
    }
}

