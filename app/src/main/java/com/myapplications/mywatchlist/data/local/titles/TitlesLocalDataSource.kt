package com.myapplications.mywatchlist.data.local.titles

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.mappers.toTitleItems
import com.myapplications.mywatchlist.domain.entities.TitleItemFull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TitlesLocalDataSource {
    /**
     * Inserts the given [TitleItemFull] in the local database if it's not saved there already. And if
     * it is - it will update it in case the information has changed.
     */
    suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Deletes the given [TitleItemFull] from the local database.
     */
    suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull)

    /**
     * Returns titles stored in local database.
     * @return list of [TitleItemFull] or null if no [TitleItemFull]s are stored.
     */
    suspend fun getAllBookmarkedTitles(): List<TitleItemFull>?

    /**
     * @return [Boolean] indicating whether a [TitleItemFull] already is saved in local database as
     * watchlisted.
     */
    suspend fun checkIfTitleItemWatchlisted(titleItemFull: TitleItemFull): Boolean

    /**
     * @return a [Flow] of list of [TitleItemFull]s that are watchlisted.
     */
    fun allWatchlistedTitlesFlow(): Flow<List<TitleItemFull>>
}

class TitlesLocalDataSourceImpl @Inject constructor(
    private val titlesDao: TitlesDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesLocalDataSource {

    override suspend fun bookmarkTitleItem(titleItemFull: TitleItemFull) = withContext(dispatcher) {

        // Check if title already exists first. If so - update only
        val titleItemExists = titlesDao.checkIfTitleItemExists(
            type = titleItemFull.type,
            mediaId = titleItemFull.mediaId
        )

        if (titleItemExists) {
            titlesDao.updateTitleItem(titleItemFull = titleItemFull)
        } else {
            /* Handling the logic in Dao because it should happen in a transaction to make sure it
            * both TitleItemEntity and GenreForTitleEntity get saved in the database. Currently
            * 'bookmarking' logic is to simply save a TitleItem  */
            titlesDao.insertTitleItem(titleItemFull = titleItemFull)
        }
    }

    override suspend fun unBookmarkTitleItem(titleItemFull: TitleItemFull) {
        // Just in case, check if title already exists first.
        val titleItemExists = titlesDao.checkIfTitleItemExists(
            type = titleItemFull.type,
            mediaId = titleItemFull.mediaId
        )

        // If exists - deleting.
        if (titleItemExists) {
            titlesDao.deleteTitleItem(titleItemFull)
        }
    }

    override suspend fun getAllBookmarkedTitles(): List<TitleItemFull>? = withContext(dispatcher) {
        val allTitleItemEntities = titlesDao.getAllTitleItems()
        return@withContext allTitleItemEntities?.toTitleItems()
    }

    override fun allWatchlistedTitlesFlow(): Flow<List<TitleItemFull>> {
        return titlesDao.allWatchlistedTitleItems().map {
            it.toTitleItems()
        }
    }

    override suspend fun checkIfTitleItemWatchlisted(titleItemFull: TitleItemFull): Boolean =
        withContext(dispatcher) {
            titlesDao.checkIfTitleItemExists(type = titleItemFull.type, mediaId = titleItemFull.mediaId)
        }
}