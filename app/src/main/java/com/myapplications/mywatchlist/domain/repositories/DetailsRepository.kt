package com.myapplications.mywatchlist.domain.repositories

import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.result.ResultOf

interface DetailsRepository {

    suspend fun getMovie(id: Long): ResultOf<Movie>

    suspend fun getTv(id: Long): ResultOf<TV>
}