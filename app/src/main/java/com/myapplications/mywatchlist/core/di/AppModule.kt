package com.myapplications.mywatchlist.core.di

import android.content.Context
import androidx.room.Room
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.data.local.WatchlistDatabase
import com.myapplications.mywatchlist.data.remote.api.MyGsonConverter
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        val gsonConverterFactory = GsonConverterFactory.create(MyGsonConverter.create())
        return Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideCurrencyApi(retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(appContext, WatchlistDatabase::class.java, "watchlist_database")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideTitlesDao(db: WatchlistDatabase) = db.titlesDao()

    @Singleton
    @Provides
    fun provideGenresDao(db: WatchlistDatabase) = db.genresDao()

}