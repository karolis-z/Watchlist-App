package com.myapplications.mywatchlist.data.remote.api

import android.util.Log
import com.google.gson.*
import com.myapplications.mywatchlist.core.util.Constants
import com.myapplications.mywatchlist.data.entities.TitleItemApiModel
import com.myapplications.mywatchlist.data.entities.TitleTypeApiModel
import java.lang.reflect.Type
import java.time.LocalDate

private const val TAG = "GSON_CONVERTER"

object MyGsonConverter {

    fun create() : Gson = GsonBuilder().apply {
        registerTypeAdapter(SearchApiResponse::class.java, MyJsonDeserializer())
        setLenient()
    }.create()

    private class MyJsonDeserializer : JsonDeserializer<SearchApiResponse> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): SearchApiResponse {
            val mJson = try {
                json?.asJsonObject as JsonObject
            } catch (e: Exception) {
                val error = "Could not convert json to a JsonObject. Reason: $e"
                Log.e(TAG, "deserialize: $error", e)
                // Returning a response which should be checked for totalResults before proceeding
                return emptyResponse()
            }
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
                            posterLink = getPosterLink(resultJson),
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
                SearchApiResponse(
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
         * @return [String] or null based on whether the "poster_path" property is set for
         * JsonObject provided.
         */
        private fun getPosterLink(resultJsonObject: JsonObject): String? {
            val linkEnding = resultJsonObject.get("poster_path")
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
         * @return the "overview" property's value if it's not empty as [String] or null if empty.
         */
        private fun getOverview(resultJsonObject: JsonObject): String? {
            return resultJsonObject.get("overview").asString.ifEmpty {
                null
            }
        }

        /**
         * @return [LocalDate] representing the first air date for a tv show or release date for a
         * movie
         */
        private fun getReleaseDate(resultJsonObject: JsonObject): LocalDate {
            // if it's a TV show it will "first_air_date" and "release_date) if it's a movie
            val dateJson =
                resultJsonObject.get("release_date") ?: resultJsonObject.get("first_air_date")
            return LocalDate.parse(dateJson.asString)
        }

        /**
         * Returns an empty [SearchApiResponse] which indicates a successful api request, but with
         * nothing found by the query.
         * @return [SearchApiResponse] with page, totalPages and totalResults equal to 0 and
         * titleItems set as null.
         */
        private fun emptyResponse(): SearchApiResponse {
            return SearchApiResponse(page = 0, titleItems = null, totalPages = 0, totalResults = 0)
        }
    }



}