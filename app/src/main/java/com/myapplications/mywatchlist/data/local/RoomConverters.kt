package com.myapplications.mywatchlist.data.local

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.*

private const val TAG = "ROOM_CONVERTERS"

class RoomConverters {

    @TypeConverter
    fun localDateToTimeStamp(date: LocalDate?): Long? {
        return date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.epochSecond
    }

    @TypeConverter
    fun timestampToLocalDate(timeStamp: Long?): LocalDate? {
        return if (timeStamp == null) {
            return null
        } else {
            Instant.ofEpochSecond(timeStamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    @TypeConverter
    fun localDateTimeToTimeStamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.epochSecond
    }

    @TypeConverter
    fun timestampToLocalDateTime(timeStamp: Long?): LocalDateTime? {
        return if (timeStamp == null) {
            return null
        } else {
            Instant.ofEpochSecond(timeStamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    }

    @TypeConverter
    fun stringListToString(stringList: List<String>?): String? {
        return if (stringList == null) {
            null
        } else {
            Gson().toJson(stringList)
        }
    }

    @TypeConverter
    fun stringToListOfString(value: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            Gson().fromJson(value, type)
        } catch (e: Exception) {
            Log.e(TAG, "stringToListOfString: could not convert this " +
                    "json string to list of strings: $value", e)
            null
        }
    }

}