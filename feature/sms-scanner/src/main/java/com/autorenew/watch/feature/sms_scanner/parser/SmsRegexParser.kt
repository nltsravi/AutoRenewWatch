package com.autorenew.watch.feature.sms_scanner.parser

object SmsRegexParser {

    private val amountRegex = Regex("(?i)(?:rs\\.?|inr|₹)\\s?([0-9,]+\\.?[0-9]*)")
    private val dateRegex = Regex("(?i)on\\s(\\d{2}[-/]\\d{2}[-/]\\d{2,4})")
    private val autoDebitRegex = Regex("(?i)(auto-?debit|mandate|subscription|nach)")
    
    // Some sample merchant matches
    private val knownMerchants = listOf("NETFLIX", "SPOTIFY", "LIC", "AMAZON PRIME")

    data class ParsedTransaction(
        val amount: Double?,
        val dateString: String?,
        val isAutoDebit: Boolean,
        val merchant: String?
    )

    fun parse(smsBody: String, senderId: String): ParsedTransaction? {
        val amountMatch = amountRegex.find(smsBody)
        val dateMatch = dateRegex.find(smsBody)
        val isAutoDebit = autoDebitRegex.containsMatchIn(smsBody)
        
        val amountStr = amountMatch?.groups?.get(1)?.value?.replace(",", "")
        val amount = amountStr?.toDoubleOrNull()
        
        val merchant = knownMerchants.find { smsBody.contains(it, ignoreCase = true) }

        if (amount != null || isAutoDebit) {
            return ParsedTransaction(
                amount = amount,
                dateString = dateMatch?.groups?.get(1)?.value,
                isAutoDebit = isAutoDebit,
                merchant = merchant
            )
        }
        return null
    }
}
