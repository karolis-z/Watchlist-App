package com.myapplications.mywatchlist.data.remote.api

import com.google.gson.*
import java.lang.reflect.Type

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
            TODO("Not yet implemented")
        }
    }

}