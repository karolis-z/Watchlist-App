package com.myapplications.mywatchlist.data.local.details

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.ApiGetDetailsException
import com.myapplications.mywatchlist.data.mappers.toMovie
import com.myapplications.mywatchlist.data.mappers.toMovieEntity
import com.myapplications.mywatchlist.data.mappers.toTv
import com.myapplications.mywatchlist.data.mappers.toTvEntity
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV
import com.myapplications.mywatchlist.domain.entities.Title
import com.myapplications.mywatchlist.domain.entities.TitleType
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface LocalDetailsDataSource {
    /**
     * Inserts the given [Title] in the local database if it's not saved there already. And if
     * it is - it will update it in case the information has changed.
     */
    suspend fun bookmarkTitle(title: Title)

    /**
     * Deletes the given [Title] from the local database.
     */
    suspend fun unBookmarkTitle(title: Title)

    /**
     * Retrieve a [Title] from the local database and a return as a [ResultOf.Success] if found, and
     * [ResultOf.Failure] if not found
     */
    suspend fun getTitle(titleId: Long, titleType: TitleType): ResultOf<Title>
}

private const val TAG = "LOCAL_DETAILS_DATASRC"

class LocalDetailsDataSourceImpl @Inject constructor(
    private val movieDao: MovieDao,
    private val tvDao: TvDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : LocalDetailsDataSource {

    override suspend fun bookmarkTitle(title: Title) = withContext(dispatcher) {
        when (title){
            is Movie -> {
                movieDao.insertMovie(title.copy(isWatchlisted = true))
                return@withContext
            }
            is TV -> {
                tvDao.insertTv(title.copy(isWatchlisted = true))
                return@withContext
            }
            else -> {
                Log.e(
                    TAG, "bookmarkTitle: the provided title was neither Movie nor TV type. " +
                            "Unable to save this title in the local database: $title")
                return@withContext
            }
        }
    }

    override suspend fun unBookmarkTitle(title: Title) = withContext(dispatcher)  {
        when (title){
            is Movie -> {
                movieDao.deleteMovieEntity(title.toMovieEntity())
                return@withContext
            }
            is TV -> {
                tvDao.deleteTvEntity(title.toTvEntity())
                return@withContext
            }
            else -> {
                Log.e(TAG, "unBookmarkTitle: the provided title was neither Movie nor TV type." +
                            " Unable to delete this title from the database: $title")
                return@withContext
            }
        }
    }

    override suspend fun getTitle(titleId: Long, titleType: TitleType): ResultOf<Title> =
        withContext(dispatcher) {
            val title = try {
                when (titleType) {
                    TitleType.MOVIE -> movieDao.getMovie(titleId).toMovie()
                    TitleType.TV -> tvDao.getTv(titleId).toTv()
                }
            } catch (e: Exception) {
                val error = "Did not find a title with given id in the local database"
                Log.e(TAG, "getTitle: $error. Reason: $e", e)
                return@withContext ResultOf.Failure(
                    message = error,
                    throwable = ApiGetDetailsException.NoConnectionException(error, e)
                )
            }
            return@withContext ResultOf.Success(data = title)
        }
}