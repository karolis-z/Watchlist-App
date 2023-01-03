package com.myapplications.mywatchlist.core.di

import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.data.remote.api.MyGsonConverter
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

}