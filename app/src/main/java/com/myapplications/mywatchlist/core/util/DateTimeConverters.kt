package com.myapplications.mywatchlist.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

object DateTimeConverters {
    /**
     * Converts [LocalDate] to timestamp at UTC
     */
    fun localDateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().epochSecond
    }

    /**
     * Converts [Long] timestamp at UTC to LocalDate at system default [ZoneId]
     */
    fun timestampToLocalDate(timestamp: Long): LocalDate {
        return Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}