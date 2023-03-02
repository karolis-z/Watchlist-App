package com.myapplications.mywatchlist.ui

import androidx.work.*
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.workmanager.UpdateConfigurationInfoWorker
import com.myapplications.mywatchlist.data.ApiGetGenresExceptions
import com.myapplications.mywatchlist.domain.repositories.GenresRepository
import com.myapplications.mywatchlist.domain.result.BasicResult
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "APP_STARTUP"

class WatchlistAppStartup @Inject constructor(
    private val genresRepository: GenresRepository,
    private val workManager: WorkManager
) {

    /**
     * Initializes an update of list of Genres from the TMDB Api which are needed to be able to map
     * genre ids received from the same api on every fetch of information about titles.
     */
    suspend fun updateGenres(): BasicResult {
        return when (val result = genresRepository.updateGenresFromApi()) {
            is BasicResult.Failure -> {
                if (result.exception is ApiGetGenresExceptions.NoConnectionException) {
                    BasicResult.Failure(exception = result.exception)
                } else {
                    BasicResult.Failure(exception = null)
                }
            }
            is BasicResult.Success -> BasicResult.Success(null)
        }
    }

    /**
     * Creates a periodic [WorkRequest] that updated the Configuration details from the api. If this
     * work had already been scheduled, then the ExistingPeriodicWorkPolicy.KEEP will ensure the
     * existing work request is kept and not replaced.
     */
    fun launchPeriodicConfigurationUpdateWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest =
            PeriodicWorkRequestBuilder<UpdateConfigurationInfoWorker>(3, TimeUnit.DAYS)
                .setConstraints(constraints)
                .addTag("UPDATE_CONFIGURATION")
                .build()
        workManager.enqueueUniquePeriodicWork(
            /* uniqueWorkName = */ Constants.PERIODIC_WORK_REQUEST_UPDATE_CONFIGURATION,
            /* existingPeriodicWorkPolicy = */ ExistingPeriodicWorkPolicy.KEEP,
            /* periodicWork = */ workRequest
        )
    }

}