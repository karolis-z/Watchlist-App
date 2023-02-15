package com.myapplications.mywatchlist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.myapplications.mywatchlist.data.entities.*
import com.myapplications.mywatchlist.data.entities.cached.*
import com.myapplications.mywatchlist.data.local.details.MovieDao
import com.myapplications.mywatchlist.data.local.details.TvDao
import com.myapplications.mywatchlist.data.local.genres.GenresDao
import com.myapplications.mywatchlist.data.local.titles.TitlesDao
import com.myapplications.mywatchlist.data.local.titles.cache.*

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
        TitleItemSimilarTvEntity::class,

        TitleItemCacheDiscoverMovie::class,
        TitleItemCacheDiscoverTV::class,
        TitleItemCachePopularMovie::class,
        TitleItemCachePopularTV::class,
        TitleItemCacheSearchAll::class,
        TitleItemCacheSearchMovie::class,
        TitleItemCacheSearchTV::class,
        TitleItemCacheTopRatedMovie::class,
        TitleItemCacheTopRatedTV::class,
        TitleItemCacheTrending::class,
        TitleItemCacheUpcomingMovie::class,
        GenreForCacheItemDiscoverMovie::class,
        GenreForCacheItemDiscoverTV::class,
        GenreForCacheItemPopularMovie::class,
        GenreForCacheItemPopularTV::class,
        GenreForCacheItemSearchAll::class,
        GenreForCacheItemSearchMovie::class,
        GenreForCacheItemSearchTV::class,
        GenreForCacheItemTopRatedMovie::class,
        GenreForCacheItemTopRatedTV::class,
        GenreForCacheItemTrending::class,
        GenreForCacheItemUpcomingMovie::class,
        RemoteKeyDiscoverMovie::class,
        RemoteKeyDiscoverTV::class,
        RemoteKeyPopularMovie::class,
        RemoteKeyPopularTV::class,
        RemoteKeySearchAll::class,
        RemoteKeySearchMovie::class,
        RemoteKeySearchTV::class,
        RemoteKeyTopRatedMovie::class,
        RemoteKeyTopRatedTV::class,
        RemoteKeyTrending::class,
        RemoteKeyUpcomingMovie::class,
    ],
    version = 1
)
@TypeConverters(RoomConverters::class)
abstract class WatchlistDatabase : RoomDatabase() {

    abstract fun titlesDao(): TitlesDao
    abstract fun genresDao(): GenresDao
    abstract fun movieDao(): MovieDao
    abstract fun tvDao(): TvDao

    abstract fun discoverMoviesCacheDao(): DiscoverMovieCacheDao
    abstract fun discoverTvCacheDao(): DiscoverTVCacheDao
    abstract fun popularMoviesCacheDao(): PopularMoviesCacheDao
    abstract fun popularTvCacheDao(): PopularTVCacheDao
    abstract fun searchAllCacheDao(): SearchAllCacheDao
    abstract fun searchMoviesCacheDao(): SearchMoviesCacheDao
    abstract fun searchTvCacheDao(): SearchTVCacheDao
    abstract fun topRatedMoviesCacheDao(): TopRatedMoviesCacheDao
    abstract fun topRatedTvCacheDao(): TopRatedTVCacheDao
    abstract fun trendingCacheDao(): TrendingCacheDao
    abstract fun upcomingMoviesDao(): UpcomingMoviesCacheDao

}