package com.myapplications.mywatchlist.data.repositories

import android.util.Log
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.mappers.toTitleItemFull
import com.myapplications.mywatchlist.domain.entities.*
import com.myapplications.mywatchlist.domain.repositories.DetailsRepository
import com.myapplications.mywatchlist.domain.repositories.TitleItemsRepository
import com.myapplications.mywatchlist.domain.repositories.TitlesManager
import com.myapplications.mywatchlist.domain.result.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "TITLES_MANAGER"

class TitlesManagerImpl @Inject constructor(
    private val detailsRepository: DetailsRepository,
    private val titleItemsRepository: TitleItemsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesManager {

    override suspend fun searchTitles(query: String): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            titleItemsRepository.searchTitles(query)
        }

    override suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {
        val titleResult =
            detailsRepository.getTitle(mediaId = titleItemFull.mediaId, type = titleItemFull.type)
        when (titleResult) {
            is ResultOf.Failure -> {
                // If not successful - not bookmarking either the Title nor the TitleItem
                Log.e(
                    TAG, "bookmarkTitleItem: did not succeed in fetching a Title " +
                            "with id:${titleItemFull.mediaId} of type:${titleItemFull.type}. " +
                            "Reason: ${titleResult.message}", titleResult.throwable
                )
                return@withContext
            }
            is ResultOf.Success -> {
                detailsRepository.bookmarkTitle(titleResult.data)
                titleItemsRepository.bookmarkTitleItem(titleItemFull)
            }
        }
    }

    override suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {
        val titleResult =
            detailsRepository.getTitle(mediaId = titleItemFull.mediaId, type = titleItemFull.type)
        when (titleResult) {
            is ResultOf.Failure -> {
                /* If not successful - not unbookmarking Title, but still unbookmarking TitleItem
                * because for some reason the Title was not bookmarked. Logging error, because that
                * should not happen */
                Log.e(
                    TAG, "unBookmarkTitleItem: did not succeed in fetching a Title " +
                            "with id:${titleItemFull.mediaId} of type:${titleItemFull.type}. " +
                            "Reason: ${titleResult.message}", titleResult.throwable
                )
                titleItemsRepository.unBookmarkTitleItem(titleItemFull)
            }
            is ResultOf.Success -> {
                detailsRepository.unBookmarkTitle(titleResult.data)
                titleItemsRepository.unBookmarkTitleItem(titleItemFull)
            }
        }
    }

    override suspend fun getWatchlistedTitles(): List<TitleItemFull>? = withContext(dispatcher) {
        titleItemsRepository.getWatchlistedTitles()
    }

    override fun allWatchlistedTitleItems(): Flow<List<TitleItemFull>> {
        return titleItemsRepository.allWatchlistedTitleItems()
    }

    override suspend fun getTrendingTitles(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            titleItemsRepository.getTrendingTitles()
        }

    override suspend fun getPopularTitles(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            titleItemsRepository.getPopularTitles()
        }

    override suspend fun getTopRatedTitles(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            titleItemsRepository.getTopRatedTitles()
        }

    override suspend fun getUpcomingMovies(): ResultOf<List<TitleItemFull>> =
        withContext(dispatcher) {
            titleItemsRepository.getUpcomingMovies()
        }

    override suspend fun getTitle(mediaId: Long, type: TitleType): ResultOf<Title> =
        withContext(dispatcher) {
            detailsRepository.getTitle(mediaId = mediaId, type = type)
        }

    override suspend fun bookmarkTitle(title: Title) = withContext(dispatcher) {
        detailsRepository.bookmarkTitle(title)
        when (title) {
            is Movie -> titleItemsRepository.bookmarkTitleItem(title.toTitleItemFull())
            is TV -> titleItemsRepository.bookmarkTitleItem(title.toTitleItemFull())
        }
    }

    override suspend fun unBookmarkTitle(title: Title) = withContext(dispatcher) {
        detailsRepository.unBookmarkTitle(title)
        when (title) {
            is Movie -> titleItemsRepository.unBookmarkTitleItem(title.toTitleItemFull())
            is TV -> titleItemsRepository.unBookmarkTitleItem(title.toTitleItemFull())
        }
    }
}