package com.myapplications.mywatchlist.data.local.titles

import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.data.mappers.toTitleItems
import com.myapplications.mywatchlist.domain.entities.TitleItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TitlesLocalDataSource {
    /**
     * Inserts the given [TitleItem] in the local database if it's not saved there already. And if
     * it is - it will update it in case the information has changed.
     */
    suspend fun bookmarkTitleItem(titleItem: TitleItem)

    /**
     * Deletes the given [TitleItem] from the local database.
     */
    suspend fun unBookmarkTitleItem(titleItem: TitleItem)

    /**
     * Returns titles stored in local database.
     * @return list of [TitleItem] or null if no [TitleItem]s are stored.
     */
    suspend fun getAllBookmarkedTitles(): List<TitleItem>?

    /**
     * @return [Boolean] indicating whether a [TitleItem] already is saved in local database as
     * watchlisted.
     */
    suspend fun checkIfTitleItemWatchlisted(titleItem: TitleItem): Boolean

    /**
     * @return a [Flow] of list of [TitleItem]s that are watchlisted.
     */
    fun allWatchlistedTitlesFlow(): Flow<List<TitleItem>>
}

class TitlesLocalDataSourceImpl @Inject constructor(
    private val titlesDao: TitlesDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : TitlesLocalDataSource {

    override suspend fun bookmarkTitleItem(titleItem: TitleItem) = withContext(dispatcher) {

        // Check if title already exists first. If so - update only
        val titleItemExists = titlesDao.checkIfTitleItemExists(
            type = titleItem.type,
            mediaId = titleItem.mediaId
        )

        if (titleItemExists) {
            titlesDao.updateTitleItem(titleItem = titleItem)
        } else {
            /* Handling the logic in Dao because it should happen in a transaction to make sure it
            * both TitleItemEntity and GenreForTitleEntity get saved in the database. Currently
            * 'bookmarking' logic is to simply save a TitleItem  */
            titlesDao.insertTitleItem(titleItem = titleItem)
        }
    }

    override suspend fun unBookmarkTitleItem(titleItem: TitleItem) {
        // Just in case, check if title already exists first.
        val titleItemExists = titlesDao.checkIfTitleItemExists(
            type = titleItem.type,
            mediaId = titleItem.mediaId
        )

        // If exists - deleting.
        if (titleItemExists) {
            titlesDao.deleteTitleItem(titleItem)
        }
    }

    override suspend fun getAllBookmarkedTitles(): List<TitleItem>? = withContext(dispatcher) {
        val allTitleItemEntities = titlesDao.getAllTitleItems()
        return@withContext allTitleItemEntities?.toTitleItems()
    }

    override fun allWatchlistedTitlesFlow(): Flow<List<TitleItem>> {
        return titlesDao.allWatchlistedTitleItems().map {
            it.toTitleItems()
        }
    }

    override suspend fun checkIfTitleItemWatchlisted(titleItem: TitleItem): Boolean =
        withContext(dispatcher) {
            titlesDao.checkIfTitleItemExists(type = titleItem.type, mediaId = titleItem.mediaId)
        }
}