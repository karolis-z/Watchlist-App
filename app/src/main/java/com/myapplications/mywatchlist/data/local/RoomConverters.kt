package com.myapplications.mywatchlist.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class RoomConverters {

    @TypeConverter
    fun localDateToTimeStamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().epochSecond
    }

    @TypeConverter
    fun timestampToLocalDate(timeStamp: Long): LocalDate {
        return Instant.ofEpochSecond(timeStamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }

}