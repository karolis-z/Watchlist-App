package com.myapplications.mywatchlist.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.core.util.DateTimeConverters
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepositoryImpl.PreferencesKeys.BACKDROP_DEFAULT_SIZE
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepositoryImpl.PreferencesKeys.IMAGES_BASE_URL
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepositoryImpl.PreferencesKeys.POSTER_DEFAULT_SIZE
import com.myapplications.mywatchlist.data.datastore.UserPrefsRepositoryImpl.PreferencesKeys.PROFILE_DEFAULT_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

interface UserPrefsRepository {

    /**
     * Returns a flow of [UserPreferences]
     */
    val userPreferencesFlow: Flow<UserPreferences>

    /**
     * Returns the date when Genres were last updated or null if they never were.
     */
    suspend fun getGenresLastUpdateDate(): LocalDate?

    /**
     * Set a new date for when genres were last updated
     */
    suspend fun updateGenresUpdateDate(newDate: LocalDate)

    /**
     * Updates the various images sizes and the base url to be retrieved from the api whenever
     * an image is needed
     */
    suspend fun updateConfiguration(apiConfiguration: ApiConfiguration)

    /**
     * Retrieves the api Configuration data to be used when downloading images from the api
     * @return [ApiConfiguration]
     */
    suspend fun getApiConfiguration(): ApiConfiguration
}

class UserPrefsRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPrefsRepository {

    private object PreferencesKeys {
        val GENRES_LAST_UPDATE = longPreferencesKey("genres_last_update_date")
        val IMAGES_BASE_URL = stringPreferencesKey("images_base_url")
        val BACKDROP_DEFAULT_SIZE = stringPreferencesKey("backdrop_default_size")
        val POSTER_DEFAULT_SIZE = stringPreferencesKey("poster_default_size")
        val PROFILE_DEFAULT_SIZE = stringPreferencesKey("profile_default_size")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val genresLastUpdateInPrefs = preferences[PreferencesKeys.GENRES_LAST_UPDATE]
            val genresLastUpdate = if (genresLastUpdateInPrefs == null) {
                null
            } else {
                DateTimeConverters.timestampToLocalDate(genresLastUpdateInPrefs)
            }
            val baseImageUrl: String = preferences[IMAGES_BASE_URL] ?: Constants.TMDB_IMAGES_BASE_URL
            val backdropDefaultSize: String = preferences[BACKDROP_DEFAULT_SIZE] ?: Constants.TMDB_BACKDROP_SIZE_W780
            val posterDefaultSize: String = preferences[POSTER_DEFAULT_SIZE] ?: Constants.TMDB_POSTER_SIZE_W500
            val profileDefaultSize: String = preferences[PROFILE_DEFAULT_SIZE] ?: Constants.TMDB_PROFILE_SIZE_H632
            val apiConfiguration = ApiConfiguration(
                baseImageUrl = baseImageUrl,
                backdropDefaultSize = backdropDefaultSize,
                posterDefaultSize = posterDefaultSize,
                profileDefaultSize = profileDefaultSize
            )
            UserPreferences(genresLastUpdate = genresLastUpdate, apiConfiguration = apiConfiguration)
        }

    override suspend fun updateGenresUpdateDate(newDate: LocalDate) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GENRES_LAST_UPDATE] =
                DateTimeConverters.localDateToTimestamp(newDate)
        }
    }

    override suspend fun getGenresLastUpdateDate(): LocalDate? {
        return userPreferencesFlow.first().genresLastUpdate
    }

    override suspend fun updateConfiguration(apiConfiguration: ApiConfiguration) {
        dataStore.edit { preferences ->
            preferences[IMAGES_BASE_URL] = apiConfiguration.baseImageUrl
            preferences[BACKDROP_DEFAULT_SIZE] = apiConfiguration.backdropDefaultSize
            preferences[POSTER_DEFAULT_SIZE] = apiConfiguration.posterDefaultSize
            preferences[PROFILE_DEFAULT_SIZE] = apiConfiguration.profileDefaultSize
        }
    }

    override suspend fun getApiConfiguration(): ApiConfiguration {
        return userPreferencesFlow.first().apiConfiguration
    }
}

data class UserPreferences(
    val genresLastUpdate: LocalDate?,
    val apiConfiguration: ApiConfiguration
)

data class ApiConfiguration(
    val baseImageUrl: String,
    val backdropDefaultSize: String,
    val posterDefaultSize: String,
    val profileDefaultSize: String
)