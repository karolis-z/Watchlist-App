package com.myapplications.mywatchlist.data.remote.api

import android.util.Log
import com.google.gson.*
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.data.entities.MovieApiModel
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.entities.TitleTypeApiModel
import com.myapplications.mywatchlist.data.entities.TvApiModel
import com.myapplications.mywatchlist.domain.entities.*
import java.lang.reflect.Type
import java.time.LocalDate

private const val TAG = "GSON_CONVERTER"

object MyGsonConverter {

    fun create() : Gson = GsonBuilder().apply {
        registerTypeAdapter(ApiResponse::class.java, MyJsonDeserializer())
        setLenient()
    }.create()

    private class MyJsonDeserializer : JsonDeserializer<ApiResponse> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): ApiResponse {

            val mJson = try {
                json?.asJsonObject as JsonObject
            } catch (e: Exception) {
                val error = "Could not convert json to a JsonObject. Reason: $e"
                Log.e(TAG, "deserialize: $error", e)
                // Returning a response which should be checked for totalResults before proceeding
                return emptyResponse()
            }

            // Check if this is deserialization of Genres, Movie, Tv or Search/Trending
            return if (mJson.get("title") != null){
                handleMovieResponse(mJson)
            } else if (mJson.get("name") != null) {
                handleTvResponse(mJson)
            } else if (mJson.get("genres") != null) {
                handleGenresResponse(mJson)
            } else if (mJson.get("change_keys") != null) {
                handleConfigurationResponse(mJson)
            } else {
                handleSearchOrTrendingResponse(mJson)
            }
        }

        /**
         * Handles the Api's response for getConfiguration request
         */
        private fun handleConfigurationResponse(mJson: JsonObject): ApiResponse {
            val response = try {
                val imgJson = mJson.get("images").asJsonObject
                ApiResponse.ConfigurationResponse(
                    baseUrl = getNullableStringProperty(imgJson, "secure_base_url"),
                    backdropSizes = getNullableStringList(imgJson, "backdrop_sizes"),
                    posterSizes = getNullableStringList(imgJson, "poster_sizes"),
                    profileSizes = getNullableStringList(imgJson, "profile_sizes")
                )
            } catch (e: Exception) {
                val error = "Could not parse the JsonObject into a ConfigurationResponse. Json = $mJson"
                Log.e(TAG, "handleConfigurationResponse: $error", e)
                ApiResponse.ConfigurationResponse(null, null, null, null)
            }
            return response
        }

        /**
         * Handles the Api's response for the getMovie request
         */
        private fun handleMovieResponse(mJson: JsonObject): ApiResponse {
            val response = try {
                val movie = try {
                    MovieApiModel(
                        id = mJson.get("id").asLong,
                        name = mJson.get("title").asString,
                        imdbId = getNullableStringProperty(mJson,"imdb_id"),
                        overview = getNullableStringProperty(mJson, "overview").takeIf {
                            it?.isNotEmpty() == true
                        },
                        tagline = getNullableStringProperty(mJson, "tagline").takeIf {
                            it?.isNotEmpty() == true
                        },
                        posterLinkEnding = getImageLink(mJson, "poster_path"),
                        backdropLinkEnding = getImageLink(mJson, "backdrop_path"),
                        genres = getMovieOrTvGenres(mJson),
                        cast = getTvOrMovieCastMembers(mJson),
                        videos = getYoutubeVideoLinks(mJson),
                        status = getMovieStatus(mJson.get("status").asString),
                        releaseDate = getReleaseDate(mJson),
                        revenue = mJson.get("revenue").asLong,
                        runtime = getNullableIntProperty(mJson, "runtime"),
                        voteCount = mJson.get("vote_count").asLong,
                        voteAverage = mJson.get("vote_average").asDouble
                    )
                } catch (e: Exception) {
                    val error = "Could not parse the JsonObject into a MovieApiModel. Json = $mJson"
                    Log.e(TAG, "handleMovieResponse: $error", e)
                    null
                }
                ApiResponse.MovieResponse(movie)
            } catch (e: Exception) {
                val error = "Something went wrong handling the Movie response for this json: $mJson"
                Log.e(TAG, "handleMovieResponse: $error", e)
                ApiResponse.MovieResponse(null)
            }
            return response
        }

        /**
         * Handles the Api's response for the getTv request
         */
        private fun handleTvResponse(mJson: JsonObject): ApiResponse {
            val response = try {
                val tv = try {
                    TvApiModel(
                        id = mJson.get("id").asLong,
                        name = mJson.get("name").asString,
                        overview = getNullableStringProperty(mJson, "overview").takeIf {
                            it?.isNotEmpty() == true
                        },
                        tagline = getNullableStringProperty(mJson, "tagline").takeIf {
                            it?.isNotEmpty() == true
                        },
                        posterLinkEnding = getImageLink(mJson, "poster_path"),
                        backdropLinkEnding = getImageLink(mJson, "backdrop_path"),
                        genres = getMovieOrTvGenres(mJson),
                        cast = getTvOrMovieCastMembers(mJson),
                        videos = getYoutubeVideoLinks(mJson),
                        status = getTvStatus(mJson.get("status").asString),
                        releaseDate = getReleaseDate(mJson),
                        lastAirDate = getLastAirDate(mJson),
                        numberOfSeasons = mJson.get("number_of_seasons").asInt,
                        numberOfEpisodes = mJson.get("number_of_episodes").asInt,
                        voteCount = mJson.get("vote_count").asLong,
                        voteAverage = mJson.get("vote_average").asDouble
                    )
                } catch (e: Exception) {
                    val error = "Could not parse the JsonObject into a TvApiModel. Json = $mJson"
                    Log.e(TAG, "handleMovieResponse: $error", e)
                    null
                }
                ApiResponse.TvResponse(tv)
            } catch (e: Exception) {
                val error = "Something went wrong handling the Tv response for this json: $mJson"
                Log.e(TAG, "handleTvResponse: $error", e)
                ApiResponse.TvResponse(null)
            }
            return response
        }

        /**
         * Handles the response if it is for the [TmdbApi.getTvGenres] or [TmdbApi.getMovieGenres] query.
         * @param mJson the root [JsonObject] received from the request.
         * @return [ApiResponse.GenresResponse]
         */
        private fun handleGenresResponse(mJson: JsonObject): ApiResponse.GenresResponse {
            val genresJsonArray = try {
                mJson.get("genres").asJsonArray
            } catch (e: Exception) {
                val error = "Could not parse genres response as a JsonArray."
                Log.e(TAG, "handleGenresResponse: $error", e)
                // Returning a null as a list to indicate unsuccessful response
                // TODO: Consider changing this pattern to return a Success/Failure object
                return ApiResponse.GenresResponse(null)
            }

            val genresList = mutableListOf<Genre>()
            genresJsonArray.forEachIndexed { index, jsonElement ->
                try {
                    val genreJson = jsonElement.asJsonObject
                    val genre = Genre(
                        id = genreJson.get("id").asLong,
                        name = genreJson.get("name").asString
                    )
                    genresList.add(genre)
                } catch (e: Exception) {
                    val error = "Could not parse genresJsonArray element #$index to a Genre object"
                    Log.e(TAG, "handleGenresResponse: $error", e)
                }
            }
            // If list has at least one element - return it. Else - return null.
            return if (genresList.isNotEmpty()) {
                ApiResponse.GenresResponse(genres = genresList)
            } else {
                ApiResponse.GenresResponse(genres = null)
            }
        }

        /**
         * Handles the response if it is for the [TmdbApi.search] query.
         * @param mJson the root [JsonObject] received from the request.
         * @return [ApiResponse.TitlesListResponse]
         */
        private fun handleSearchOrTrendingResponse(mJson: JsonObject): ApiResponse.TitlesListResponse {
            val page = mJson.get("page").asInt
            val pageCount = mJson.get("total_pages").asInt
            val resultCount = mJson.get("total_results").asInt
            // If nothing found by the query, return an 'empty' response
            if (resultCount == 0) {
                return emptyResponse()
            }

            val titleItems = mutableListOf<TitleItemApiModel>()
            val results = mJson.get("results").asJsonArray
            Log.d(TAG, "deserialize: resultCount = $resultCount")
            Log.d(TAG, "deserialize: results.size = ${results.size()}")
            results.forEachIndexed { index, jsonElement ->
                try {
                    val resultJson = jsonElement.asJsonObject
                    val mediaTypeString = resultJson.get("media_type").asString
                    if (mediaTypeString == "movie" || mediaTypeString == "tv") {
                        val titleItem = TitleItemApiModel(
                            id = 0,
                            name = getName(resultJson),
                            type = getMediaType(resultJson),
                            mediaId = resultJson.get("id").asLong,
                            overview = getOverview(resultJson),
                            posterLinkEnding = getImageLink(
                                resultJsonObject = resultJson,
                                propertyName = "poster_path"
                            ),
                            genres = resultJson.get("genre_ids").asJsonArray.map { it.asInt },
                            releaseDate = getReleaseDate(resultJson),
                            voteCount = resultJson.get("vote_count").asLong,
                            voteAverage = resultJson.get("vote_average").asDouble
                        )
                        Log.d(TAG, "deserialize: titleItem = $titleItem")
                        titleItems.add(titleItem)
                    }
                } catch (e: Exception) {
                    val error = "Could not parse element #$index. Reason: $e"
                    Log.e(TAG, "deserialize: $error", e)
                }
            }

            // If at least 1 result parsing did not fail - returning a non empty SearchApiResponse
            return if (titleItems.isNotEmpty()){
                ApiResponse.TitlesListResponse(
                    page = page,
                    titleItems = titleItems,
                    totalPages = pageCount,
                    totalResults = resultCount
                )
            } else {
                emptyResponse()
            }
        }

        /**
         * Get list of genres' ids from the Movie or TV JsonObject
         */
        private fun getMovieOrTvGenres(resultJsonObject: JsonObject): List<Int> {
            return try {
                resultJsonObject.get("genres").asJsonArray.map {
                    it.asJsonObject.get("id").asInt
                }
            } catch (e: Exception) {
                val error = "Could not parse array of genres. The list of genres will be empty."
                Log.e(TAG, "getMovieOrTvGenres: $error", e)
                emptyList()
            }
        }

        /**
         * Get list of [CastMember] or null from a Movie's or TV's JsonObject.
         */
        private fun getTvOrMovieCastMembers(resultJsonObject: JsonObject): List<CastMember>? {
            val castJsonArray = resultJsonObject.get("credits").asJsonObject.get("cast").asJsonArray
            val castList = mutableListOf<CastMember>()
            castJsonArray.forEachIndexed { index, jsonElement ->
                val castMember = try {
                    CastMember(
                        id = jsonElement.asJsonObject.get("id").asLong,
                        name = jsonElement.asJsonObject.get("name").asString,
                        character = jsonElement.asJsonObject.get("character").asString,
                        pictureLink = getImageLink(jsonElement.asJsonObject, "profile_path")
                    )
                } catch (e: Exception) {
                    val error = "Could not parse cast member jsonElement #$index. Json = $jsonElement"
                    Log.e(TAG, "handleMovieResponse: $error", e)
                }
                if (castMember is CastMember) {
                    castList.add(castMember)
                }
            }
            return if (castList.isEmpty()) {
                null
            } else {
                castList
            }
        }

        /**
         * Returns a [String] value from provided [resultJsonObject]'s [propertyName]
         * or null if not found.
         */
        private fun getNullableStringProperty(
            resultJsonObject: JsonObject,
            propertyName: String
        ): String? {
            val jsonObject = resultJsonObject.get(propertyName)
            return if (jsonObject.isJsonNull) {
                Log.d(TAG, "getNullableStringProperty. JsonObject: $resultJsonObject " +
                        "did not have a property $propertyName and returned null.")
                null
            } else {
                jsonObject.asString
            }
        }

        /**
         * Returns a list of [String]s from provided resultJsonObject's propertyName or null
         * if not found.
         */
        private fun getNullableStringList(
            resultJsonObject: JsonObject, propertyName: String
        ): List<String>? {
            val jsonObject = resultJsonObject.get(propertyName)
            return if (jsonObject.isJsonNull) {
                Log.d(TAG, "getNullableStringList. JsonObject: $resultJsonObject " +
                        "did not have a property $propertyName and returned null.")
                null
            } else {
                return jsonObject.asJsonArray.map { it.asString }
            }
        }

        /**
         * Returns an [Int] value from provided [resultJsonObject]'s [propertyName]
         * or null if not found.
         */
        private fun getNullableIntProperty(
            resultJsonObject: JsonObject,
            propertyName: String
        ): Int? {
            val jsonObject = resultJsonObject.get(propertyName)
            Log.d(TAG, "getNullableIntProperty. JsonObject: $resultJsonObject " +
                    "did not have a property $propertyName and returned null.")
            return if (jsonObject.isJsonNull) {
                null
            } else {
                jsonObject.asInt
            }
        }

        /**
         * @return [String] representing the name of the TV show or Movie
         */
        private fun getName(resultJsonObject: JsonObject): String {
            // If it's a movie, the property is "title", if TV then it's "name"
            val nameJson = resultJsonObject.get("title") ?: resultJsonObject.get("name")
            return nameJson.asString
        }

        /**
         * @return [TitleTypeApiModel] based on the media_type property of the JsonObject inside
         * the "results" object of the root JsonObject.
         */
        private fun getMediaType(resultJsonObject: JsonObject): TitleTypeApiModel {
            val typeString = resultJsonObject.get("media_type").asString
            return when (typeString) {
                TitleTypeApiModel.TV.propertyName -> TitleTypeApiModel.TV
                TitleTypeApiModel.MOVIE.propertyName -> TitleTypeApiModel.MOVIE
                else -> {
                    throw Exception("$TAG: getMediaType: unknown media type received.")
                }
            }
        }

        /**
         * Creates a link from provided JsonObject's property named [propertyName]. This works
         * because all images are stored on the same base url of the api.
         * @return [String] or null based on whether the [propertyName] is set for the
         * JsonObject provided.
         */
        private fun getImageLink(resultJsonObject: JsonObject, propertyName: String): String? {
            val linkEnding = resultJsonObject.get(propertyName)
            //Log.d(TAG, "getPosterLink: $linkEnding")
            return if (linkEnding.isJsonNull) {
                null
            } else {
                linkEnding.asString
            }
        }

        /**
         * Creates a list of Youtube links if the provided [resultJsonObject] has a property 
         * "videos" and it has a property "results" that is not empty JsonArray.
         * @return list of [String] of Youtube links, or null if no links available. 
         */
        private fun getYoutubeVideoLinks(resultJsonObject: JsonObject): List<YtVideo>? {
            val linksArray = try {
                resultJsonObject.get("videos").asJsonObject.get("results").asJsonArray
            } catch (e: Exception) {
                val error = "Could not extract an array of videos from json Object. " +
                        "Reason = $e. JsonObject = $resultJsonObject"
                Log.d(TAG, "getYoutubeVideoLinks: $error", e)
                null
            }
            if (linksArray != null) {
                val videos = mutableListOf<YtVideo>()
                linksArray.forEach { jsonElement ->
                    try {
                        /* We don't videos that are not from YouTube since we are not implementing
                           their playback functionality */
                        if (jsonElement.asJsonObject.get("site").asString == "YouTube") {
                            val type = when(jsonElement.asJsonObject.get("type").asString) {
                                "Featurette" -> YtVideoType.Featurette
                                "Teaser" -> YtVideoType.Teaser
                                "Behind the Scenes" -> YtVideoType.BehindTheScenes
                                "Trailer" -> YtVideoType.Trailer
                                else -> YtVideoType.Other
                            }
                            val video = YtVideo(
                                link = Constants.YOUTUBE_WATCH_URL +
                                        jsonElement.asJsonObject.get("key").asString,
                                videoId = jsonElement.asJsonObject.get("key").asString,
                                name = jsonElement.asJsonObject.get("name").asString,
                                type = type
                            )
                            videos.add(video)
                        }
                    } catch (e: Exception) {
                        val error = "Failed to parse jsonElement into a YtVideoApiModel. " +
                                "Reason: $e. JsonElement = $jsonElement"
                        Log.d(TAG, "getYoutubeVideoLinks: $error", e)
                    }
                }
                return if (videos.isEmpty()){
                    null
                } else {
                    videos
                }
            } else {
                return null
            }
        }

        /**
         * @return the "overview" property's value if it's not empty as [String] or null if empty.
         */
        private fun getOverview(resultJsonObject: JsonObject): String? {
            return resultJsonObject.get("overview").asString.ifEmpty {
                Log.d(TAG, "getOverview: JsonObject $resultJsonObject " +
                        "did not have a 'overview' property?")
                null
            }
        }

        /**
         * @return [LocalDate] representing the first air date for a tv show or release date for a
         * movie, or null if it's not available in the [resultJsonObject]
         */
        private fun getReleaseDate(resultJsonObject: JsonObject): LocalDate? {
            // if it's a TV show it will "first_air_date" and "release_date) if it's a movie
            val movieReleaseDate = try {
                val dateString = resultJsonObject.get("release_date").asString
                LocalDate.parse(dateString)
            } catch (e: Exception) {
                null
            }
            val tvReleaseDate = try {
                val dateString = resultJsonObject.get("first_air_date").asString
                LocalDate.parse(dateString)
            } catch (e: Exception) {
                null
            }
            if (tvReleaseDate == null && movieReleaseDate == null){
                Log.e(TAG, "getReleaseDate: Could not get and parse value from neither " +
                        "the 'release_date' nor the 'first_air_date' properties")
            }
            /* Return movie's release date or tv's release date (which could be null if it's also
            not available) */
            return movieReleaseDate ?: tvReleaseDate
        }

        /**
         * @return [LocalDate] of last aired tv episode, or null if not available
         */
        private fun getLastAirDate(resultJsonObject: JsonObject): LocalDate? {
            return try {
                val dateString = resultJsonObject.get("last_air_date").asString
                LocalDate.parse(dateString)
            } catch (e: Exception) {
                Log.e(TAG, "getLastAirDate: Could not parse value from 'last_air_date' property", e)
                null
            }
        }

        /**
         * @return [MovieStatus] based "status" property in the response json from api.
         * If any other value than the expected ones - will use [MovieStatus.Unknown]
         */
        private fun getMovieStatus(statusString: String): MovieStatus {
            return when (statusString) {
                "Rumored" -> MovieStatus.Rumored
                "Planned" -> MovieStatus.Planned
                "In Production" -> MovieStatus.InProduction
                "Post Production" -> MovieStatus.PostProduction
                "Released" -> MovieStatus.Released
                "Canceled" -> MovieStatus.Cancelled
                else -> {
                    Log.e(TAG, "getMovieStatus: Api returned unexpected status: $statusString. " +
                            "Defaulting tu MovieStatus.Unknown")
                    MovieStatus.Unknown
                }
            }
        }

        /**
         * @return [TvStatus] based "status" property in the response json from api.
         * If any other value than the expected ones - will use [TvStatus.Unknown]
         */
        private fun getTvStatus(statusString: String): TvStatus {
            return when (statusString) {
                "Ended" -> TvStatus.Ended
                "Returning Series" -> TvStatus.ReturningSeries
                "In Production" -> TvStatus.InProduction
                "Pilot" -> TvStatus.Pilot
                "Planned" -> TvStatus.Planned
                "Canceled" -> TvStatus.Cancelled
                else -> {
                    Log.e(TAG, "getTvStatus: Api returned unexpected status: $statusString. " +
                            "Defaulting tu TvStatus.Unknown")
                    TvStatus.Unknown
                }
            }
        }

        /**
         * Returns an empty [ApiResponse.TitlesListResponse] which indicates a successful api request, but with
         * nothing found by the query.
         * @return [ApiResponse.TitlesListResponse] with page, totalPages and totalResults equal to 0 and
         * titleItems set as null.
         */
        private fun emptyResponse(): ApiResponse.TitlesListResponse {
            return ApiResponse.TitlesListResponse(
                page = 0,
                titleItems = null,
                totalPages = 0,
                totalResults = 0
            )
        }
    }
}