package com.myapplications.mywatchlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myapplications.mywatchlist.data.entities.GenreEntity
import com.myapplications.mywatchlist.data.entities.GenreForTitleEntity
import com.myapplications.mywatchlist.data.entities.TitleItemEntity
import com.myapplications.mywatchlist.data.local.genres.GenresDao
import com.myapplications.mywatchlist.data.local.titles.TitlesDao

@Database(
    entities = [
        TitleItemEntity::class,
        GenreForTitleEntity::class,
        GenreEntity::class,
    ],
    version = 1
)
@TypeConverters(RoomConverters::class)
abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun titlesDao(): TitlesDao

    abstract fun genresDao(): GenresDao

}