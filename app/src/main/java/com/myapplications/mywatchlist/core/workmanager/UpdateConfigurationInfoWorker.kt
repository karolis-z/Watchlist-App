package com.myapplications.mywatchlist.core.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.myapplications.mywatchlist.core.di.IoDispatcher
import com.myapplications.mywatchlist.core.util.Constants.TMDB_BACKDROP_SIZE_W780
import com.myapplications.mywatchlist.core.util.Constants.TMDB_POSTER_SIZE_W500
import com.myapplications.mywatchlist.core.util.Constants.TMDB_PROFILE_SIZE_H632
import com.myapplications.mywatchlist.data.datastore.ApiConfiguration
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepository
import com.myapplications.mywatchlist.data.remote.api.ApiResponse
import com.myapplications.mywatchlist.data.remote.api.TmdbApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

private const val TAG = "UPDATE_CONFIG_WORKER"

@HiltWorker
class UpdateConfigurationInfoWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val api: TmdbApi,
    private val userPrefsRepository: UserPrefsRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(dispatcher) {

        val response = try {
            api.getConfiguration()
        } catch (e: Exception) {
            val error = "Could not get a successful Configuration response from the api"
            Log.e(TAG, "doWork: $error", e)
            return@withContext Result.failure()
        }

        if (response.body() == null || !response.isSuccessful) {
            val error =
                "Configuration request succeeded but body is null or code is not successful."
            Log.e(TAG, "doWork: $error")
            return@withContext Result.failure()
        }

        val configurationBody = response.body() as ApiResponse.ConfigurationResponse
        val apiConfiguration = parseResponseBody(configurationBody)
        if (apiConfiguration == null) {
            val error = "Some of the returned Configuration elements were null. " +
                    "Configuration body: $configurationBody"
            Log.e(TAG, "doWork: $error")
            return@withContext Result.failure()
        } else {
            Log.d(TAG, "Configuration update successful. New configuration = $apiConfiguration")
            userPrefsRepository.updateConfiguration(apiConfiguration)
            return@withContext Result.success()
        }
    }

    private fun parseResponseBody(responseBody: ApiResponse.ConfigurationResponse): ApiConfiguration? {
        if (
            responseBody.backdropSizes == null
            || responseBody.posterSizes == null
            || responseBody.profileSizes == null
            || responseBody.baseUrl == null
        ) {
            return null
        } else {
            /* For each image size, get the default one, or if it doesn't exist - the second one
            (because we don't want the lowest quality), or if it doesn't exist - the first one */
            val backdropsCount = responseBody.backdropSizes.size
            val backdropSize = if (responseBody.backdropSizes.contains(TMDB_BACKDROP_SIZE_W780)) {
                TMDB_BACKDROP_SIZE_W780
            } else {
                if (backdropsCount >= 2) responseBody.backdropSizes[1] else responseBody.backdropSizes[0]
            }

            val postersCount = responseBody.posterSizes.size
            val posterSize = if (responseBody.posterSizes.contains(TMDB_POSTER_SIZE_W500)) {
                TMDB_POSTER_SIZE_W500
            } else {
                if (postersCount >= 2) responseBody.posterSizes[1] else responseBody.posterSizes[0]
            }

            val profilesCount = responseBody.profileSizes.size
            val profileSize = if (responseBody.profileSizes.contains(TMDB_PROFILE_SIZE_H632)) {
                TMDB_PROFILE_SIZE_H632
            } else {
                if (profilesCount >= 2) responseBody.profileSizes[1] else responseBody.profileSizes[0]
            }

            return ApiConfiguration(
                baseImageUrl = responseBody.baseUrl,
                backdropDefaultSize = backdropSize,
                posterDefaultSize = posterSize,
                profileDefaultSize = profileSize
            )
        }
    }
}