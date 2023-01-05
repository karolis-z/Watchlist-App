package com.myapplications.mywatchlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myapplications.mywatchlist.data.entities.GenreEntity
import com.myapplications.mywatchlist.data.entities.GenreForTitleEntity
import com.myapplications.mywatchlist.data.entities.TitleItemEntity

@Database(
    entities = [
        TitleItemEntity::class,
        GenreForTitleEntity::class,
        GenreEntity::class,
    ],
    version = 1
)
abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun titlesDao(): TitlesDao

    abstract fun genresDao(): GenresDao

}