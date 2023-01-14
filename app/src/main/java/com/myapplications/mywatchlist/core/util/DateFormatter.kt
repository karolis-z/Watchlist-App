package com.myapplications.mywatchlist.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateFormatter {

    fun getLocalizedShortDateString(date: LocalDate): String {
        val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        return date.format(dateTimeFormatter)
    }

}