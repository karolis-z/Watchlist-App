package com.myapplications.mywatchlist.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.myapplications.mywatchlist.core.util.DateTimeConverters
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
}

class UserPrefsRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPrefsRepository {

    private object PreferencesKeys {
        val GENRES_LAST_UPDATE = longPreferencesKey("genres_last_update_date")
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
            UserPreferences(genresLastUpdate)
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
}

data class UserPreferences(
    val genresLastUpdate: LocalDate?
)