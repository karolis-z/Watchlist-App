package com.myapplications.mywatchlist.core.util

import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {

    /**
     * Return a [String] representing the provided [amount] in US Dollars but formatted based on
     * current locale's currency formatting settings.
     */
    fun getUsdAmountInLocalCurrencyFormat(amount: Long): String {
        val usdCurrency = Currency.getInstance("USD")
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 0
            currency = usdCurrency
        }
        return formatter.format(amount)
    }

}