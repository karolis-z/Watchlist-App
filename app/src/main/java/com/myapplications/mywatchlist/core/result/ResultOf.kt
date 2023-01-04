package com.myapplications.mywatchlist.core.result

sealed class ResultOf<out T> {
    data class Success<out R>(val data: R): ResultOf<R>()
    data class Failure(
        val message: String?,
        val throwable: Throwable?
    ): ResultOf<Nothing>()
}