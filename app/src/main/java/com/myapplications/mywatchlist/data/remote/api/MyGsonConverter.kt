package com.myapplications.mywatchlist.data.remote.api

import android.util.Log
import com.google.gson.*
import java.lang.reflect.Type

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
            
            val results = mJson.get("results").asJsonArray
            results.forEach {
                Log.d(TAG, "deserialize: element overview = ${it.asJsonObject.get("overview")}")
            }
            TODO("Finish logic for deserialization.")

            return emptyResponse()

        }

        /**
         * Returns an empty [SearchApiResponse] which indicates a successful api request, but with
         * nothing found by the query.
         * @return [SearchApiResponse] with page, totalPages and totalResults equal to 0 and
         * titleItems set as null
         */
        private fun emptyResponse(): SearchApiResponse {
            return SearchApiResponse(page = 0, titleItems = null, totalPages = 0, totalResults = 0)
        }
    }



}