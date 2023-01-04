package com.myapplications.mywatchlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [],
    version = 1
)
abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun titlesDao(): TitlesDao

}