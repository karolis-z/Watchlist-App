package com.myapplications.mywatchlist.data.remote.api

import android.util.Log
import com.google.gson.*
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.data.entities.MovieApiModel
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.entities.TitleTypeApiModel
import com.myapplications.mywatchlist.domain.entities.CastMember
import com.myapplications.mywatchlist.domain.entities.Genre
import com.myapplications.mywatchlist.domain.entities.Status
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

            Log.d(TAG, "deserialize: STARTING")

            val mJson = try {
                json?.asJsonObject as JsonObject
            } catch (e: Exception) {
                val error = "Could not convert json to a JsonObject. Reason: $e"
                Log.e(TAG, "deserialize: $error", e)
                // Returning a response which should be checked for totalResults before proceeding
                return emptyResponse()
            }

            Log.d(TAG, "deserialize: mJson.get(\"genres\") ${mJson.get("genres")}")

            // Check if this is deserialization of Genres, Movie, Tv or Search/Trending
            return if (mJson.get("title") != null){
                Log.d(TAG, "deserialize: Handling MOVIE response")
                handleMovieResponse(mJson)
//            } else if (mJson.get("name") != null) {
//                Log.d(TAG, "deserialize: Handling TV response")
//                handleTvResponse(mJson)
            } else if (mJson.get("genres") != null) {
                Log.d(TAG, "deserialize: Handling GENRES response")
                handleGenresResponse(mJson)
            } else {
                Log.d(TAG, "deserialize: Handling SEARCH or TRENDING response")
                handleSearchOrTrendingResponse(mJson)
            }
        }

        private fun handleMovieResponse(mJson: JsonObject): ApiResponse {
            val response = try {
                // CAST
                val castJsonArray = mJson.get("credits").asJsonObject.get("cast").asJsonArray
                val castList = mutableListOf<CastMember>()
                castJsonArray.forEachIndexed { index, jsonElement ->
                    val castMember = try {
                        CastMember(
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

                // GENRES
                val genres = try {
                    mJson.get("genres").asJsonArray.map {
                        it.asJsonObject.get("id").asInt
                    }
                } catch (e: Exception) {
                    val error = "Could not parse array of genres. The list of genres will be empty."
                    Log.e(TAG, "handleMovieResponse: $error", e)
                    emptyList()
                }

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
                        posterLink = getImageLink(mJson, "poster_path"),
                        backdropLink = getImageLink(mJson, "backdrop_path"),
                        genres = genres,
                        cast = if (castList.isEmpty()) null else castList,
                        videos = getYoutubeVideoLinks(mJson),
                        status = getStatus(mJson.get("status").asString),
                        releaseDate = getReleaseDate(mJson),
                        revenue = mJson.get("revenue").asLong,
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

//        private fun handleTvResponse(mJson: JsonObject): ApiResponse {
//            // TODO
//            return
//        }

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
                            posterLink = getImageLink(
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
         * Returns a [String]value from provided [resultJsonObject]'s [propertyName]
         * or null if not found.
         */
        private fun getNullableStringProperty(
            resultJsonObject: JsonObject,
            propertyName: String
        ): String? {
            val jsonObject = resultJsonObject.get(propertyName)
            Log.e(TAG, "getNullableStringProperty. JsonObject: $resultJsonObject " +
                    "did not have a property $propertyName and returned null.")
            return if (jsonObject.isJsonNull) {
                null
            } else {
                jsonObject.asString
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
                val fullImageLink =
                    Constants.TMDB_IMAGES_BASE_URL + Constants.TMDB_IMAGES_SIZE_W500 + linkEnding.asString
                fullImageLink
            }
        }

        /**
         * Creates a list of Youtube links if the provided [resultJsonObject] has a property 
         * "videos" and it has a property "results" that is not empty JsonArray.
         * @return list of [String] of Youtube links, or null if no links available. 
         */
        private fun getYoutubeVideoLinks(resultJsonObject: JsonObject): List<String>? {
            val linksArray = resultJsonObject.get("videos").asJsonObject.get("results").asJsonArray
            val links = mutableListOf<String>()
            linksArray.forEach { jsonElement ->
                val youtubeKey = jsonElement.asJsonObject.get("key").asString
                links.add(Constants.YOUTUBE_WATCH_URL + youtubeKey)
            }
            return if (links.isEmpty()){
                null
            } else {
                links
            }
        }

        /**
         * @return the "overview" property's value if it's not empty as [String] or null if empty.
         */
        private fun getOverview(resultJsonObject: JsonObject): String? {
            return resultJsonObject.get("overview").asString.ifEmpty {
                Log.e(TAG, "getOverview: JsonObject $resultJsonObject " +
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
                Log.e(TAG, "getReleaseDate: Could not get and parse value from 'release_date' property")
                null
            }
            val tvReleaseDate = try {
                val dateString = resultJsonObject.get("first_air_date").asString
                LocalDate.parse(dateString)
            } catch (e: Exception) {
                Log.e(TAG, "getReleaseDate: Could not get and parse value from 'first_air_date' property")
                null
            }
            /* Return movie's release date or tv's release date (which could be null if it's also
            not available) */
            return movieReleaseDate ?: tvReleaseDate
        }

        /**
         * @return [Status] based "status" property in the response json from api. If any other value
         * than the expected ones - will use [Status.Unknown]
         */
        private fun getStatus(statusString: String): Status {
            return when (statusString) {
                "Rumored" -> Status.Rumored
                "Planned" -> Status.Planned
                "In Production" -> Status.InProduction
                "Post Production" -> Status.PostProduction
                "Released" -> Status.Released
                "Canceled" -> Status.Cancelled
                else -> {
                    Log.e(TAG, "getStatus: Api returned unexpected status: $statusString. " +
                            "Defaulting tu Status.Unknown")
                    Status.Unknown
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