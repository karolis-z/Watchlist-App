package com.myapplications.mywatchlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.data.local.details.MovieDao
import com.myapplications.mywatchlist.data.local.details.TvDao
import com.myapplications.mywatchlist.data.local.genres.GenresDao
import com.myapplications.mywatchlist.data.local.titles.TitlesDao

@Database(
    entities = [
        TitleItemEntity::class,
        GenreForTitleEntity::class,
        GenreEntity::class,
        MovieEntity::class,
        GenreForMovieEntity::class,
        CastMemberForMovieEntity::class,
        TvEntity::class,
        GenreForTvEntity::class,
        CastMemberForTvEntity::class,
        YtVideoForTvEntity::class,
        YtVideoForMovieEntity::class,
        TitleItemRecommendedMovieEntity::class,
        TitleItemSimilarMovieEntity::class,
        TitleItemRecommendedTvEntity::class,
        TitleItemSimilarTvEntity::class
    ],
    version = 1
)
@TypeConverters(RoomConverters::class)
abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun titlesDao(): TitlesDao

    abstract fun genresDao(): GenresDao

    abstract fun movieDao(): MovieDao

    abstract fun tvDao(): TvDao

}