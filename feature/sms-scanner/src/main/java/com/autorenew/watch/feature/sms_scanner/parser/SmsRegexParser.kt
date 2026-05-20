package com.autorenew.watch.feature.sms_scanner.parser

object SmsRegexParser {

    private val amountRegex = Regex("(?i)(?:rs\\.?|inr|₹)\\s?([0-9,]+\\.?[0-9]*)")
    private val dateRegex = Regex("(?i)on\\s(\\d{2}[-/]\\d{2}[-/]\\d{2,4})")
    
    // Subscription / Renewal indicators
    private val subscriptionKeywordsRegex = Regex("(?i)(auto-?debit|auto-?pay|mandate|subscription|nach|sip|renew|renewal|premium|recurring|standing\\s+instruction|auto-?renew)")
    
    // Exclusion keywords: deposits, credits, credit card dues/bills/outstanding/pending
    private val exclusionKeywordsRegex = Regex("(?i)(credited|deposited|refund|received|deposit|cr\\b|credit card|outstanding|due\\b|pending|due date|minimum\\s+amount|bill\\s+due)")

    // Some sample merchant matches
    private val knownMerchants = listOf(
        "Netflix", "Amazon Prime", "Prime Video", "Spotify", "LIC", 
        "YouTube", "Google One", "Google", "Apple", "iCloud", "Disney", 
        "Hotstar", "Microsoft", "Adobe", "Canva", "ChatGPT", "Claude", 
        "SonyLIV", "Zee5", "JioSaavn", "Gaana", "Times Prime", "Medium", 
        "GitHub", "LinkedIn", "Mutual Fund", "SIP", "Broadband", "Wi-Fi"
    )

    data class ParsedTransaction(
        val amount: Double?,
        val dateString: String?,
        val isAutoDebit: Boolean,
        val merchant: String?
    )

    fun parse(smsBody: String, senderId: String): ParsedTransaction? {
        // 1. Check exclusions first
        if (exclusionKeywordsRegex.containsMatchIn(smsBody)) {
            return null
        }

        val amountMatch = amountRegex.find(smsBody)
        val dateMatch = dateRegex.find(smsBody)
        
        val isSubscriptionKeyword = subscriptionKeywordsRegex.containsMatchIn(smsBody)
        
        // Check if there is a known merchant in the SMS body
        var merchant = knownMerchants.find { smsBody.contains(it, ignoreCase = true) }

        val amountStr = amountMatch?.groups?.get(1)?.value?.replace(",", "")
        val amount = amountStr?.toDoubleOrNull()

        if (amount != null) {
            // Only match if it's explicitly marked as a subscription/renewal/auto-debit keyword, 
            // or if it matches a known subscription merchant.
            if (isSubscriptionKeyword || merchant != null) {
                
                // If merchant was not in the hardcoded known list, try to extract it dynamically
                if (merchant == null) {
                    merchant = extractDynamicMerchant(smsBody)
                }

                return ParsedTransaction(
                    amount = amount,
                    dateString = dateMatch?.groups?.get(1)?.value,
                    isAutoDebit = isSubscriptionKeyword,
                    merchant = merchant
                )
            }
        }
        return null
    }

    private fun extractDynamicMerchant(smsBody: String): String {
        // Look for phrases like "for <Merchant>", "towards <Merchant>", "to <Merchant>", "at <Merchant>"
        val prepositions = listOf("for", "towards", "to", "at")
        for (prep in prepositions) {
            val regex = Regex("(?i)\\b$prep\\s+([A-Za-z0-9&\\-\\s]{3,25}?)(?:\\s+rs|\\s+inr|\\s+₹|\\bdebited|\\bon\\b|\\bis\\b|\\bwas\\b|\\bhas\\b|\\bfor\\b|\\.|$)")
            val match = regex.find(smsBody)
            if (match != null) {
                val candidate = match.groups[1]?.value?.trim() ?: ""
                // Validate candidate is not a currency string or empty
                if (candidate.isNotEmpty() && 
                    !candidate.equals("rs", ignoreCase = true) && 
                    !candidate.equals("inr", ignoreCase = true) &&
                    !candidate.equals("my", ignoreCase = true) &&
                    !candidate.equals("your", ignoreCase = true)
                ) {
                    return candidate
                }
            }
        }
        
        // Check for prefix patterns like "Netflix subscription" -> "Netflix"
        val suffixRegex = Regex("(?i)([A-Za-z0-9&\\-\\s]{3,20}?)\\s+(?:subscription|renewal|premium|sip|mandate)")
        val suffixMatch = suffixRegex.find(smsBody)
        if (suffixMatch != null) {
            val candidate = suffixMatch.groups[1]?.value?.trim() ?: ""
            if (candidate.isNotEmpty() && 
                !candidate.equals("monthly", ignoreCase = true) && 
                !candidate.equals("annual", ignoreCase = true) &&
                !candidate.equals("your", ignoreCase = true)
            ) {
                return candidate
            }
        }

        return "Subscription"
    }
}

